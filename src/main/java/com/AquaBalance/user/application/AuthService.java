package com.AquaBalance.user.application;

import com.AquaBalance.notifications.infrastructure.email.EmailAdapter;
import com.AquaBalance.shared.exception.BusinessException;
import com.AquaBalance.shared.exception.TooManyRequestsException;
import com.AquaBalance.shared.security.RateLimitService;
import com.AquaBalance.user.application.ports.in.BuscarUsuarioUseCase;
import com.AquaBalance.user.application.ports.in.RegistrarUsuarioUseCase;
import com.AquaBalance.user.application.ports.out.TokenCuentaPort;
import com.AquaBalance.user.application.ports.out.TokenPort;
import com.AquaBalance.user.domain.Rol;
import com.AquaBalance.user.domain.TipoToken;
import com.AquaBalance.user.domain.TokenCuenta;
import com.AquaBalance.user.domain.Usuario;
import com.AquaBalance.user.domain.UsuarioRepository;
import com.AquaBalance.user.infrastructure.security.CustomUserDetails;
import com.AquaBalance.user.infrastructure.web.dto.AuthResponse;
import com.AquaBalance.user.infrastructure.web.dto.LoginRequest;
import com.AquaBalance.user.infrastructure.web.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class AuthService {

    public static final String MSG_REGISTRO =
            "Cuenta creada. Te enviamos un correo para activarla: revisa tu bandeja de entrada (y el spam).";
    public static final String MSG_GENERICO_CORREO =
            "Si el correo corresponde a una cuenta, te enviaremos las instrucciones en unos minutos.";

    private static final Duration VIGENCIA_VERIFICACION = Duration.ofHours(24);
    private static final Duration VIGENCIA_RESET        = Duration.ofHours(1);

    private static final int      LOGIN_MAX_FALLOS   = 5;
    private static final Duration LOGIN_VENTANA      = Duration.ofMinutes(15);
    private static final int      CORREOS_MAX        = 3;
    private static final Duration CORREOS_VENTANA    = Duration.ofMinutes(15);

    private final SecureRandom secureRandom = new SecureRandom();

    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;
    private final BuscarUsuarioUseCase    buscarUsuarioUseCase;
    private final UsuarioRepository       usuarioRepository;
    private final TokenPort               tokenPort;
    private final TokenCuentaPort         tokenCuentaPort;
    private final PasswordEncoder         passwordEncoder;
    private final AuthenticationManager   authenticationManager;
    private final EmailAdapter            emailAdapter;
    private final RateLimitService        rateLimitService;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    public AuthService(RegistrarUsuarioUseCase registrarUsuarioUseCase,
                       BuscarUsuarioUseCase buscarUsuarioUseCase,
                       UsuarioRepository usuarioRepository,
                       TokenPort tokenPort,
                       TokenCuentaPort tokenCuentaPort,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       EmailAdapter emailAdapter,
                       RateLimitService rateLimitService) {
        this.registrarUsuarioUseCase = registrarUsuarioUseCase;
        this.buscarUsuarioUseCase    = buscarUsuarioUseCase;
        this.usuarioRepository       = usuarioRepository;
        this.tokenPort               = tokenPort;
        this.tokenCuentaPort         = tokenCuentaPort;
        this.passwordEncoder         = passwordEncoder;
        this.authenticationManager   = authenticationManager;
        this.emailAdapter            = emailAdapter;
        this.rateLimitService        = rateLimitService;
    }

    // ── Registro ──────────────────────────────────────────────

    /**
     * Registro publico: SIEMPRE crea un UsuarioPublico con el correo sin verificar.
     * No devuelve JWT; la persona debe activar la cuenta desde el enlace que llega al correo.
     */
    public String registrar(RegisterRequest request) {

        Usuario usuario = new Usuario(
                null,
                request.getNombre().trim(),
                normalizar(request.getEmail()),
                request.getPassword(),          // UserService la encripta
                Rol.UsuarioPublico              // el rol NUNCA viene del cliente
        );
        usuario.setEmailVerificado(false);

        Usuario guardado = registrarUsuarioUseCase.registrar(usuario);

        enviarVerificacion(guardado);
        return MSG_REGISTRO;
    }

    // ── Verificacion de correo ────────────────────────────────

    public String verificarEmail(String token) {
        TokenCuenta tokenCuenta = obtenerTokenVigente(token, TipoToken.VERIFICACION_EMAIL);

        Usuario usuario = usuarioRepository.findById(tokenCuenta.getUsuarioId())
                .orElseThrow(() -> new BusinessException("El enlace no es válido o ya expiró."));

        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);

        tokenCuenta.marcarUsado();
        tokenCuentaPort.save(tokenCuenta);

        return "Correo verificado. Ya puedes iniciar sesión.";
    }

    public String reenviarVerificacion(String email) {
        String correo = normalizar(email);
        if (rateLimitService.permitir("correo:" + correo, CORREOS_MAX, CORREOS_VENTANA)) {
            usuarioRepository.findByEmail(correo)
                    .filter(u -> u.isActivo() && !u.isEmailVerificado())
                    .ifPresent(this::enviarVerificacion);
        }
        return MSG_GENERICO_CORREO; // misma respuesta exista o no la cuenta
    }

    // ── Recuperacion de contraseña ────────────────────────────

    public String olvidePassword(String email) {
        String correo = normalizar(email);
        if (rateLimitService.permitir("correo:" + correo, CORREOS_MAX, CORREOS_VENTANA)) {
            usuarioRepository.findByEmail(correo)
                    .filter(Usuario::isActivo)
                    .ifPresent(u -> {
                        if (!u.isEmailVerificado()) {
                            enviarVerificacion(u);   // primero debe activar la cuenta
                        } else {
                            String token = emitirToken(u, TipoToken.RESET_PASSWORD, VIGENCIA_RESET);
                            emailAdapter.enviarRecuperacionPassword(
                                    u.getEmail(), u.getNombre(),
                                    enlace("/auth/restablecer-password", token));
                        }
                    });
        }
        return MSG_GENERICO_CORREO; // misma respuesta exista o no la cuenta
    }

    public String restablecerPassword(String token, String nuevaPassword) {
        TokenCuenta tokenCuenta = obtenerTokenVigente(token, TipoToken.RESET_PASSWORD);

        Usuario usuario = usuarioRepository.findById(tokenCuenta.getUsuarioId())
                .orElseThrow(() -> new BusinessException("El enlace no es válido o ya expiró."));

        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuario.setEmailVerificado(true); // recibir el correo demuestra que el correo es suyo
        usuarioRepository.save(usuario);

        tokenCuenta.marcarUsado();
        tokenCuentaPort.save(tokenCuenta);

        // Cualquier intento fallido previo deja de contar
        rateLimitService.limpiar("login:" + usuario.getEmail());

        return "Contraseña actualizada. Ya puedes iniciar sesión con la nueva.";
    }

    // ── Login ─────────────────────────────────────────────────

    public AuthResponse login(LoginRequest request) {

        String correo = normalizar(request.getEmail());
        String clave  = "login:" + correo;

        if (rateLimitService.bloqueado(clave, LOGIN_MAX_FALLOS, LOGIN_VENTANA)) {
            throw new TooManyRequestsException(
                    "Demasiados intentos fallidos. Intenta de nuevo en unos minutos.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(correo, request.getPassword()));
        } catch (BadCredentialsException e) {
            rateLimitService.permitir(clave, LOGIN_MAX_FALLOS, LOGIN_VENTANA); // cuenta el fallo
            throw e;
        }

        rateLimitService.limpiar(clave);

        Usuario usuario = buscarUsuarioUseCase.buscarPorEmail(correo);
        CustomUserDetails userDetails = new CustomUserDetails(usuario);
        String token = tokenPort.generateToken(userDetails);

        return new AuthResponse(
                usuario.getId(),
                token,
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getRol().name()
        );
    }

    // ── Helpers ───────────────────────────────────────────────

    private void enviarVerificacion(Usuario usuario) {
        String token = emitirToken(usuario, TipoToken.VERIFICACION_EMAIL, VIGENCIA_VERIFICACION);
        emailAdapter.enviarVerificacion(
                usuario.getEmail(), usuario.getNombre(),
                enlace("/auth/verificar-email", token));
    }

    /** Crea un token aleatorio de 256 bits; en la BD solo queda su hash SHA-256. */
    private String emitirToken(Usuario usuario, TipoToken tipo, Duration vigencia) {
        tokenCuentaPort.deleteByUsuarioIdAndTipo(usuario.getId(), tipo); // invalida los anteriores

        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        tokenCuentaPort.save(new TokenCuenta(
                usuario.getId(), hash(token), tipo, LocalDateTime.now().plus(vigencia)));
        return token;
    }

    private TokenCuenta obtenerTokenVigente(String token, TipoToken tipo) {
        Optional<TokenCuenta> encontrado = tokenCuentaPort.findByHashAndTipo(hash(token), tipo);
        return encontrado
                .filter(TokenCuenta::estaVigente)
                .orElseThrow(() -> new BusinessException("El enlace no es válido o ya expiró."));
    }

    private String enlace(String ruta, String token) {
        String base = frontendUrl.endsWith("/")
                ? frontendUrl.substring(0, frontendUrl.length() - 1) : frontendUrl;
        return base + ruta + "?token=" + token;
    }

    private String normalizar(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private static String hash(String valor) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(sha.digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
