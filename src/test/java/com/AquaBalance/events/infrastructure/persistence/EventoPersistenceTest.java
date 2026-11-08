package com.AquaBalance.events.infrastructure.persistence;

import com.AquaBalance.events.domain.Alerta;
import com.AquaBalance.events.domain.Evento;
import com.AquaBalance.events.domain.NivelAlerta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// ════════════════════════════════════════════════════════════════════
// AlertaRepositoryImpl
// ════════════════════════════════════════════════════════════════════

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertaRepositoryImpl - Pruebas Unitarias")
class AlertaRepositoryImplTest {

    @Mock  private JpaAlertaRepository jpa;
    @InjectMocks private AlertaRepositoryImpl repository;

    private AlertaEntity entity;

    @BeforeEach
    void setUp() {
        entity = new AlertaEntity(1L, LocalDateTime.of(2025, 4, 20, 8, 0),
                NivelAlerta.Roja, "Nivel crítico", 1L, 1L);
    }

    @Nested
    @DisplayName("guardar()")
    class Guardar {

        @Test
        @DisplayName("Debe persistir y retornar el dominio mapeado")
        void debeGuardarYRetornarDominio() {
            when(jpa.save(any())).thenReturn(entity);

            Alerta input = new Alerta(null, LocalDateTime.now(), NivelAlerta.Roja, "Nivel crítico", 1L, 1L);
            Alerta result = repository.guardar(input);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getNivel()).isEqualTo(NivelAlerta.Roja);
            assertThat(result.getMensaje()).isEqualTo("Nivel crítico");
            verify(jpa).save(any());
        }
    }

    @Nested
    @DisplayName("buscarPorId()")
    class BuscarPorId {

        @Test
        @DisplayName("Debe retornar Optional con la alerta cuando existe")
        void debeRetornarCuandoExiste() {
            when(jpa.findById(1L)).thenReturn(Optional.of(entity));

            Optional<Alerta> result = repository.buscarPorId(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Debe retornar Optional vacío cuando no existe")
        void debeRetornarVacioCuandoNoExiste() {
            when(jpa.findById(99L)).thenReturn(Optional.empty());

            assertThat(repository.buscarPorId(99L)).isEmpty();
        }
    }

    @Nested
    @DisplayName("listarTodos()")
    class ListarTodos {

        @Test
        @DisplayName("Debe retornar todos los registros mapeados")
        void debeRetornarTodos() {
            AlertaEntity e2 = new AlertaEntity(2L, LocalDateTime.now(), NivelAlerta.Amarilla, "Aviso", 2L, 2L);
            when(jpa.findAll()).thenReturn(List.of(entity, e2));

            List<Alerta> result = repository.listarTodos();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            assertThat(result.get(1).getNivel()).isEqualTo(NivelAlerta.Amarilla);
        }

        @Test
        @DisplayName("Debe retornar lista vacía si no hay registros")
        void debeRetornarVacio() {
            when(jpa.findAll()).thenReturn(List.of());
            assertThat(repository.listarTodos()).isEmpty();
        }
    }

    @Nested
    @DisplayName("buscarPorNivel()")
    class BuscarPorNivel {

        @Test
        @DisplayName("Debe filtrar por nivel y retornar la lista mapeada")
        void debeFiltrarPorNivel() {
            when(jpa.findByNivel(NivelAlerta.Roja)).thenReturn(List.of(entity));

            List<Alerta> result = repository.buscarPorNivel(NivelAlerta.Roja);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getNivel()).isEqualTo(NivelAlerta.Roja);
        }
    }

    @Nested
    @DisplayName("buscarPorEvento()")
    class BuscarPorEvento {

        @Test
        @DisplayName("Debe retornar alertas del evento indicado")
        void debeRetornarPorEvento() {
            when(jpa.findByIdEvento(1L)).thenReturn(List.of(entity));

            List<Alerta> result = repository.buscarPorEvento(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getIdEvento()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("eliminar()")
    class Eliminar {

        @Test
        @DisplayName("Debe invocar deleteById en el repositorio JPA")
        void debeInvocarDeleteById() {
            doNothing().when(jpa).deleteById(1L);
            repository.eliminar(1L);
            verify(jpa).deleteById(1L);
        }
    }
}


// ════════════════════════════════════════════════════════════════════
// EventoRepositoryImpl
// ════════════════════════════════════════════════════════════════════

@ExtendWith(MockitoExtension.class)
@DisplayName("EventoRepositoryImpl - Pruebas Unitarias")
class EventoRepositoryImplTest {

    @Mock  private JpaEventoRepository jpa;
    @InjectMocks private EventoRepositoryImpl repository;

    private EventoEntity entity;

    @BeforeEach
    void setUp() {
        entity = new EventoEntity(1L, "Desbordamiento", "Alta",
                LocalDateTime.of(2025, 3, 5, 14, 0), 1L, 1L);
    }

    @Test
    @DisplayName("guardar() — debe persistir y retornar dominio")
    void debeGuardar() {
        when(jpa.save(any())).thenReturn(entity);

        Evento result = repository.guardar(new Evento(null, "Desbordamiento", "Alta", null, 1L, 1L));

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescripcion()).isEqualTo("Desbordamiento");
    }

    @Test
    @DisplayName("buscarPorId() — debe retornar Optional presente")
    void debeBuscarPorId() {
        when(jpa.findById(1L)).thenReturn(Optional.of(entity));
        assertThat(repository.buscarPorId(1L)).isPresent();
    }

    @Test
    @DisplayName("buscarPorId() — debe retornar Optional vacío si no existe")
    void debeDevolverVacioSiNoExiste() {
        when(jpa.findById(99L)).thenReturn(Optional.empty());
        assertThat(repository.buscarPorId(99L)).isEmpty();
    }

    @Test
    @DisplayName("listarTodos() — debe retornar todos los eventos")
    void debeListarTodos() {
        EventoEntity e2 = new EventoEntity(2L, "Contaminación", "Media", LocalDateTime.now(), 2L, 2L);
        when(jpa.findAll()).thenReturn(List.of(entity, e2));

        assertThat(repository.listarTodos()).hasSize(2);
    }

    @Test
    @DisplayName("buscarPorRecurso() — debe filtrar por recurso")
    void debeBuscarPorRecurso() {
        when(jpa.findByIdRecurso(1L)).thenReturn(List.of(entity));

        List<Evento> result = repository.buscarPorRecurso(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIdRecurso()).isEqualTo(1L);
    }

    @Test
    @DisplayName("eliminar() — debe invocar deleteById")
    void debeEliminar() {
        doNothing().when(jpa).deleteById(1L);
        repository.eliminar(1L);
        verify(jpa).deleteById(1L);
    }
}
