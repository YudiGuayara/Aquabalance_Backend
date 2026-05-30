package com.AquaBalance.user.infrastructure.persistence;

import com.AquaBalance.user.domain.Rol;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UsuarioEntity - Pruebas Unitarias")
class UsuarioEntityTest {

    @Test
    @DisplayName("Debe crear la entidad con el constructor completo")
    void debeCrearEntidadConConstructorCompleto() {
        LocalDateTime fecha = LocalDateTime.of(2025, 2, 10, 8, 15);

        UsuarioEntity entity = new UsuarioEntity(
                1L,
                "Juan Pérez",
                "juan@aqua.com",
                "hash123",
                Rol.Operador,
                true,
                fecha
        );

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getNombre()).isEqualTo("Juan Pérez");
        assertThat(entity.getEmail()).isEqualTo("juan@aqua.com");
        assertThat(entity.getPassword()).isEqualTo("hash123");
        assertThat(entity.getRol()).isEqualTo(Rol.Operador);
        assertThat(entity.isActivo()).isTrue();
        assertThat(entity.getFechaCreacion()).isEqualTo(fecha);
    }

    @Test
    @DisplayName("Debe permitir modificar campos con setters")
    void debePermitirModificarCampos() {
        LocalDateTime fecha = LocalDateTime.now();

        UsuarioEntity entity = new UsuarioEntity();
        entity.setId(5L);
        entity.setNombre("Ana López");
        entity.setEmail("ana@aqua.com");
        entity.setPassword("pwd");
        entity.setRol(Rol.Administrador);
        entity.setActivo(false);
        entity.setFechaCreacion(fecha);

        assertThat(entity.getId()).isEqualTo(5L);
        assertThat(entity.getNombre()).isEqualTo("Ana López");
        assertThat(entity.getEmail()).isEqualTo("ana@aqua.com");
        assertThat(entity.getPassword()).isEqualTo("pwd");
        assertThat(entity.getRol()).isEqualTo(Rol.Administrador);
        assertThat(entity.isActivo()).isFalse();
        assertThat(entity.getFechaCreacion()).isEqualTo(fecha);
    }
}