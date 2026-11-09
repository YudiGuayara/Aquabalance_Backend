package com.AquaBalance.notifications.infrastructure.email;

import com.AquaBalance.user.domain.Rol;
import com.AquaBalance.user.domain.Usuario;
import com.AquaBalance.user.domain.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OperadoresAlertaAdapter - Pruebas Unitarias")
class OperadoresAlertaAdapterTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private OperadoresAlertaAdapter adapter;

    private Usuario usuario(long id, String email, Rol rol) {
        return new Usuario(id, "Nombre " + id, email, "hash", rol);
    }

    @Test
    @DisplayName("Debe devolver solo operadores con la cuenta verificada, sin repetir correos")
    void debeFiltrarOperadores() {
        Usuario noVerificado = usuario(4L, "nuevo@aqua.com", Rol.Operador);
        noVerificado.setEmailVerificado(false);

        when(usuarioRepository.findAllActivos()).thenReturn(List.of(
                usuario(1L, "op1@aqua.com", Rol.Operador),
                usuario(2L, "op2@aqua.com", Rol.Operador),
                usuario(3L, "admin@aqua.com", Rol.Administrador),
                noVerificado,
                usuario(5L, "publico@aqua.com", Rol.UsuarioPublico),
                usuario(6L, "op1@aqua.com", Rol.Operador)
        ));

        assertThat(adapter.emailsDeOperadores())
                .containsExactly("op1@aqua.com", "op2@aqua.com");
    }

    @Test
    @DisplayName("Debe devolver lista vacía si no hay operadores")
    void debeDevolverVacia() {
        when(usuarioRepository.findAllActivos()).thenReturn(List.of(
                usuario(1L, "admin@aqua.com", Rol.Administrador)));

        assertThat(adapter.emailsDeOperadores()).isEmpty();
    }
}
