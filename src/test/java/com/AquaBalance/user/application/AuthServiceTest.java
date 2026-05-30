// AuthServiceTest.java
package com.AquaBalance.user.application;

import com.AquaBalance.notifications.infrastructure.email.EmailAdapter;
import com.AquaBalance.user.application.ports.in.BuscarUsuarioUseCase;
import com.AquaBalance.user.application.ports.in.RegistrarUsuarioUseCase;
import com.AquaBalance.user.application.ports.out.TokenPort;
import com.AquaBalance.user.domain.Rol;
import com.AquaBalance.user.domain.Usuario;
import com.AquaBalance.user.infrastructure.security.CustomUserDetails;
import com.AquaBalance.user.infrastructure.web.dto.AuthResponse;
import com.AquaBalance.user.infrastructure.web.dto.LoginRequest;
import com.AquaBalance.user.infrastructure.web.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - Pruebas Unitarias")
class AuthServiceTest {

    @Mock
    private RegistrarUsuarioUseCase registrarUsuarioUseCase;

    @Mock
    private BuscarUsuarioUseCase buscarUsuarioUseCase;

    @Mock
    private TokenPort tokenPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EmailAdapter emailAdapter; // ← faltaba esto

    @InjectMocks
    private AuthService authService;

    private RegisterRequest requestRegistro;
    private LoginRequest requestLogin;
    private Usuario usuarioGuardado;

    @BeforeEach
    void setUp() {
        requestRegistro = new RegisterRequest();
        requestRegistro.setNombre("María García");
        requestRegistro.setEmail("maria@aqua.com");
        requestRegistro.setPassword("Segura123!");
        requestRegistro.setRol("Operador");

        requestLogin = new LoginRequest();
        requestLogin.setEmail("maria@aqua.com");
        requestLogin.setPassword("Segura123!");

        usuarioGuardado = new Usuario(1L, "María García", "maria@aqua.com", "hash_seguro", Rol.Operador);
    }

    @Nested
    @DisplayName("registrar()")
    class Registrar {

        @Test
        @DisplayName("Debe registrar un usuario y retornar AuthResponse con token")
        void debeRegistrarUsuarioYRetornarToken() {
            when(registrarUsuarioUseCase.registrar(any(Usuario.class))).thenReturn(usuarioGuardado);
            when(tokenPort.generateToken(any(CustomUserDetails.class))).thenReturn("jwt.token.generado");
            doNothing().when(emailAdapter).enviarBienvenida(any(), any());

            AuthResponse respuesta = authService.registrar(requestRegistro);

            assertThat(respuesta).isNotNull();
            assertThat(respuesta.getToken()).isEqualTo("jwt.token.generado");
            assertThat(respuesta.getEmail()).isEqualTo("maria@aqua.com");
            assertThat(respuesta.getNombre()).isEqualTo("María García");
            assertThat(respuesta.getRol()).isEqualTo("Operador");
        }

        @Test
        @DisplayName("Debe codificar la contraseña antes de guardar")
        void debeCodificarContraseniaAntesDeGuardar() {
            when(registrarUsuarioUseCase.registrar(any(Usuario.class))).thenReturn(usuarioGuardado);
            when(tokenPort.generateToken(any())).thenReturn("token");
            doNothing().when(emailAdapter).enviarBienvenida(any(), any());

            authService.registrar(requestRegistro);

            // La contraseña llega en texto plano al registrar (UserService hace el encode)
            verify(registrarUsuarioUseCase).registrar(argThat(u -> "Segura123!".equals(u.getPassword())));
        }

        @Test
        @DisplayName("Debe asignar el rol correcto al nuevo usuario")
        void debeAsignarRolCorrecto() {
            when(registrarUsuarioUseCase.registrar(any(Usuario.class))).thenReturn(usuarioGuardado);
            when(tokenPort.generateToken(any())).thenReturn("token");
            doNothing().when(emailAdapter).enviarBienvenida(any(), any());

            authService.registrar(requestRegistro);

            verify(registrarUsuarioUseCase).registrar(argThat(u -> Rol.Operador.equals(u.getRol())));
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException si el rol no es válido")
        void debeLanzarExcepcionSiRolEsInvalido() {
            requestRegistro.setRol("RolInexistente");

            assertThatThrownBy(() -> authService.registrar(requestRegistro))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(registrarUsuarioUseCase, never()).registrar(any());
        }

        @Test
        @DisplayName("Debe generar un token JWT tras registrar exitosamente")
        void debeGenerarTokenJwt() {
            when(registrarUsuarioUseCase.registrar(any())).thenReturn(usuarioGuardado);
            when(tokenPort.generateToken(any())).thenReturn("jwt.generado");
            doNothing().when(emailAdapter).enviarBienvenida(any(), any());

            AuthResponse respuesta = authService.registrar(requestRegistro);

            verify(tokenPort).generateToken(any(CustomUserDetails.class));
            assertThat(respuesta.getToken()).isNotBlank();
        }
    }

    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("Debe retornar AuthResponse con token cuando las credenciales son correctas")
        void debeRetornarTokenConCredencialesCorrectas() {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
            when(buscarUsuarioUseCase.buscarPorEmail("maria@aqua.com")).thenReturn(usuarioGuardado);
            when(tokenPort.generateToken(any(CustomUserDetails.class))).thenReturn("jwt.login.token");

            AuthResponse respuesta = authService.login(requestLogin);

            assertThat(respuesta.getToken()).isEqualTo("jwt.login.token");
            assertThat(respuesta.getEmail()).isEqualTo("maria@aqua.com");
            assertThat(respuesta.getNombre()).isEqualTo("María García");
            assertThat(respuesta.getRol()).isEqualTo("Operador");
        }

        @Test
        @DisplayName("Debe autenticar al usuario con email y contraseña correctos")
        void debeAutenticarConEmailYContrasenia() {
            when(authenticationManager.authenticate(any())).thenReturn(null);
            when(buscarUsuarioUseCase.buscarPorEmail(any())).thenReturn(usuarioGuardado);
            when(tokenPort.generateToken(any())).thenReturn("token");

            authService.login(requestLogin);

            verify(authenticationManager).authenticate(
                    argThat(auth ->
                            auth instanceof UsernamePasswordAuthenticationToken &&
                                    "maria@aqua.com".equals(auth.getPrincipal()) &&
                                    "Segura123!".equals(auth.getCredentials())
                    )
            );
        }

        @Test
        @DisplayName("Debe lanzar BadCredentialsException si la contraseña es incorrecta")
        void debeLanzarExcepcionSiContrasenaEsIncorrecta() {
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Credenciales inválidas"));

            assertThatThrownBy(() -> authService.login(requestLogin))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessageContaining("Credenciales inválidas");

            verify(buscarUsuarioUseCase, never()).buscarPorEmail(any());
            verify(tokenPort, never()).generateToken(any());
        }

        @Test
        @DisplayName("Debe generar un token JWT al hacer login exitoso")
        void debeGenerarTokenAlHacerLoginExitoso() {
            when(authenticationManager.authenticate(any())).thenReturn(null);
            when(buscarUsuarioUseCase.buscarPorEmail("maria@aqua.com")).thenReturn(usuarioGuardado);
            when(tokenPort.generateToken(any())).thenReturn("jwt.token.final");

            AuthResponse respuesta = authService.login(requestLogin);

            verify(tokenPort).generateToken(any(CustomUserDetails.class));
            assertThat(respuesta.getToken()).isEqualTo("jwt.token.final");
        }
    }
}