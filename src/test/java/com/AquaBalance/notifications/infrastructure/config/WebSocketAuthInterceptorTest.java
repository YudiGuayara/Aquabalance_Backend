package com.AquaBalance.notifications.infrastructure.config;

import com.AquaBalance.shared.security.JwtService;
import com.AquaBalance.user.domain.Rol;
import com.AquaBalance.user.domain.Usuario;
import com.AquaBalance.user.infrastructure.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocketAuthInterceptor - Pruebas Unitarias")
class WebSocketAuthInterceptorTest {

    @Mock private JwtService jwtService;
    @Mock private UserDetailsService userDetailsService;
    @Mock private MessageChannel channel;

    private WebSocketAuthInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new WebSocketAuthInterceptor(jwtService, userDetailsService);
    }

    private Message<byte[]> mensaje(StompCommand comando, String authorization) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(comando);
        if (authorization != null) {
            accessor.addNativeHeader("Authorization", authorization);
        }
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private CustomUserDetails usuario(Rol rol) {
        return new CustomUserDetails(new Usuario(1L, "Laura", "laura@aqua.com", "hash", rol));
    }

    @Test
    @DisplayName("Debe rechazar un CONNECT sin cabecera Authorization")
    void debeRechazarSinToken() {
        assertThatThrownBy(() -> interceptor.preSend(mensaje(StompCommand.CONNECT, null), channel))
                .isInstanceOf(MessageDeliveryException.class);
    }

    @Test
    @DisplayName("Debe rechazar un CONNECT con token corrupto")
    void debeRechazarTokenCorrupto() {
        when(jwtService.extractUsername("malo")).thenThrow(new RuntimeException("malformado"));

        assertThatThrownBy(() -> interceptor.preSend(mensaje(StompCommand.CONNECT, "Bearer malo"), channel))
                .isInstanceOf(MessageDeliveryException.class);
    }

    @Test
    @DisplayName("Debe rechazar un token que no pasa la validación")
    void debeRechazarTokenInvalido() {
        CustomUserDetails u = usuario(Rol.Operador);
        when(jwtService.extractUsername("tok")).thenReturn("laura@aqua.com");
        when(userDetailsService.loadUserByUsername("laura@aqua.com")).thenReturn(u);
        when(jwtService.isTokenValid("tok", u)).thenReturn(false);

        assertThatThrownBy(() -> interceptor.preSend(mensaje(StompCommand.CONNECT, "Bearer tok"), channel))
                .isInstanceOf(MessageDeliveryException.class);
    }

    @Test
    @DisplayName("Debe rechazar al rol UsuarioPublico (no puede leer notificaciones)")
    void debeRechazarUsuarioPublico() {
        CustomUserDetails u = usuario(Rol.UsuarioPublico);
        when(jwtService.extractUsername("tok")).thenReturn("laura@aqua.com");
        when(userDetailsService.loadUserByUsername("laura@aqua.com")).thenReturn(u);
        when(jwtService.isTokenValid("tok", u)).thenReturn(true);

        assertThatThrownBy(() -> interceptor.preSend(mensaje(StompCommand.CONNECT, "Bearer tok"), channel))
                .isInstanceOf(MessageDeliveryException.class);
    }

    @Test
    @DisplayName("Debe dejar pasar un CONNECT con token válido de Operador y fijar el usuario")
    void debePermitirOperador() {
        CustomUserDetails u = usuario(Rol.Operador);
        when(jwtService.extractUsername("tok")).thenReturn("laura@aqua.com");
        when(userDetailsService.loadUserByUsername("laura@aqua.com")).thenReturn(u);
        when(jwtService.isTokenValid("tok", u)).thenReturn(true);

        Message<?> resultado = interceptor.preSend(mensaje(StompCommand.CONNECT, "Bearer tok"), channel);

        assertThat(resultado).isNotNull();
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(resultado);
        assertThat(accessor.getUser()).isNotNull();
        assertThat(accessor.getUser().getName()).isEqualTo("laura@aqua.com");
    }

    @Test
    @DisplayName("No debe exigir token en frames que no son CONNECT")
    void noDebeValidarOtrosFrames() {
        Message<byte[]> sub = mensaje(StompCommand.SUBSCRIBE, null);

        assertThat(interceptor.preSend(sub, channel)).isSameAs(sub);
        verifyNoInteractions(jwtService, userDetailsService);
    }
}
