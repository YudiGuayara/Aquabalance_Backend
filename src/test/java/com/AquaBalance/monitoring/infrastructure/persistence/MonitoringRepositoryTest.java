package com.AquaBalance.monitoring.infrastructure.persistence;

import com.AquaBalance.monitoring.domain.Contaminante;
import com.AquaBalance.monitoring.domain.Medicion;
import com.AquaBalance.monitoring.domain.NivelContaminante;
import com.AquaBalance.monitoring.domain.Recurso;
import com.AquaBalance.monitoring.domain.TipoRecurso;
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
// ContaminanteRepositoryImpl
// ════════════════════════════════════════════════════════════════════

@ExtendWith(MockitoExtension.class)
@DisplayName("ContaminanteRepositoryImpl - Pruebas Unitarias")
class ContaminanteRepositoryImplTest {

    @Mock  private JpaContaminanteRepository jpa;
    @InjectMocks private ContaminanteRepositoryImpl repository;

    private ContaminanteEntity entity;

    @BeforeEach
    void setUp() {
        entity = new ContaminanteEntity();
        entity.setId(1L);
        entity.setNombre("Mercurio");
        entity.setCarga(5.0);
        entity.setNivel(NivelContaminante.Alto);
        entity.setFuenteOrigen("Minería");
    }

    @Test
    @DisplayName("guardar() — debe persistir y retornar dominio correctamente")
    void debeGuardar() {
        when(jpa.save(any())).thenReturn(entity);

        Contaminante input = new Contaminante(null, "Mercurio", 5.0, NivelContaminante.Alto, "Minería");
        Contaminante result = repository.guardar(input);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Mercurio");
        assertThat(result.getNivel()).isEqualTo(NivelContaminante.Alto);
        assertThat(result.getFuenteOrigen()).isEqualTo("Minería");
    }

    @Test
    @DisplayName("buscarPorId() — debe retornar Optional presente cuando existe")
    void debeBuscarPorId() {
        when(jpa.findById(1L)).thenReturn(Optional.of(entity));

        Optional<Contaminante> result = repository.buscarPorId(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getNombre()).isEqualTo("Mercurio");
    }

    @Test
    @DisplayName("buscarPorId() — debe retornar Optional vacío cuando no existe")
    void debeRetornarVacioSiNoExiste() {
        when(jpa.findById(99L)).thenReturn(Optional.empty());
        assertThat(repository.buscarPorId(99L)).isEmpty();
    }

    @Test
    @DisplayName("listarTodos() — debe retornar todos los contaminantes")
    void debeListarTodos() {
        ContaminanteEntity e2 = new ContaminanteEntity();
        e2.setId(2L); e2.setNombre("Arsénico");
        when(jpa.findAll()).thenReturn(List.of(entity, e2));

        List<Contaminante> result = repository.listarTodos();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getNombre()).isEqualTo("Mercurio");
        assertThat(result.get(1).getNombre()).isEqualTo("Arsénico");
    }

    @Test
    @DisplayName("listarTodos() — debe retornar lista vacía si no hay registros")
    void debeRetornarListaVacia() {
        when(jpa.findAll()).thenReturn(List.of());
        assertThat(repository.listarTodos()).isEmpty();
    }

    @Test
    @DisplayName("eliminar() — debe invocar deleteById")
    void debeEliminar() {
        doNothing().when(jpa).deleteById(1L);
        repository.eliminar(1L);
        verify(jpa).deleteById(1L);
    }
}


// ════════════════════════════════════════════════════════════════════
// RecursoRepositoryImpl
// ════════════════════════════════════════════════════════════════════

@ExtendWith(MockitoExtension.class)
@DisplayName("RecursoRepositoryImpl - Pruebas Unitarias")
class RecursoRepositoryImplTest {

    @Mock  private JpaRecursoRepository jpa;
    @InjectMocks private RecursoRepositoryImpl repository;

    private RecursoEntity entity;

    @BeforeEach
    void setUp() {
        entity = new RecursoEntity();
        entity.setId(1L);
        entity.setNombre("Río Bogotá");
        entity.setTipo(TipoRecurso.Rio);
        entity.setUbicacion("Cundinamarca");
        entity.setLatitud(4.61);
        entity.setLongitud(-74.08);
    }

    @Test
    @DisplayName("guardar() — debe persistir y retornar dominio")
    void debeGuardar() {
        when(jpa.save(any())).thenReturn(entity);

        Recurso result = repository.guardar(new Recurso(null, "Río Bogotá", TipoRecurso.Rio, "Cundinamarca", 4.61, -74.08));

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Río Bogotá");
        assertThat(result.getTipo()).isEqualTo(TipoRecurso.Rio);
        assertThat(result.getLatitud()).isEqualTo(4.61);
    }

    @Test
    @DisplayName("buscarPorId() — debe retornar Optional presente")
    void debeBuscarPorId() {
        when(jpa.findById(1L)).thenReturn(Optional.of(entity));

        Optional<Recurso> result = repository.buscarPorId(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getNombre()).isEqualTo("Río Bogotá");
    }

    @Test
    @DisplayName("buscarPorId() — debe retornar vacío si no existe")
    void debeRetornarVacioSiNoExiste() {
        when(jpa.findById(99L)).thenReturn(Optional.empty());
        assertThat(repository.buscarPorId(99L)).isEmpty();
    }

    @Test
    @DisplayName("listarTodos() — debe retornar todos los recursos")
    void debeListarTodos() {
        RecursoEntity e2 = new RecursoEntity();
        e2.setId(2L); e2.setNombre("Lago Tota"); e2.setTipo(TipoRecurso.Lago);
        when(jpa.findAll()).thenReturn(List.of(entity, e2));

        List<Recurso> result = repository.listarTodos();

        assertThat(result).hasSize(2);
        assertThat(result.get(1).getNombre()).isEqualTo("Lago Tota");
    }

    @Test
    @DisplayName("eliminar() — debe invocar deleteById")
    void debeEliminar() {
        doNothing().when(jpa).deleteById(1L);
        repository.eliminar(1L);
        verify(jpa).deleteById(1L);
    }
}


// ════════════════════════════════════════════════════════════════════
// MedicionRepositoryImpl
// ════════════════════════════════════════════════════════════════════

@ExtendWith(MockitoExtension.class)
@DisplayName("MedicionRepositoryImpl - Pruebas Unitarias")
class MedicionRepositoryImplTest {

    @Mock  private JpaMedicionRepository jpa;
    @InjectMocks private MedicionRepositoryImpl repository;

    private MedicionEntity entity;

    @BeforeEach
    void setUp() {
        entity = new MedicionEntity(1L, 7.0, 22.5,
                LocalDateTime.of(2025, 4, 20, 8, 0), 1L, 1L, 1L);
    }

    @Test
    @DisplayName("guardar() — debe persistir y retornar dominio")
    void debeGuardar() {
        when(jpa.save(any())).thenReturn(entity);

        Medicion result = repository.guardar(
                new Medicion(null, 7.0, 22.5, LocalDateTime.now(), 1L, 1L, 1L));

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPh()).isEqualTo(7.0);
        assertThat(result.getTemperatura()).isEqualTo(22.5);
    }

    @Test
    @DisplayName("buscarPorId() — debe retornar Optional presente")
    void debeBuscarPorId() {
        when(jpa.findById(1L)).thenReturn(Optional.of(entity));

        Optional<Medicion> result = repository.buscarPorId(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getPh()).isEqualTo(7.0);
    }

    @Test
    @DisplayName("buscarPorId() — debe retornar vacío si no existe")
    void debeRetornarVacioSiNoExiste() {
        when(jpa.findById(99L)).thenReturn(Optional.empty());
        assertThat(repository.buscarPorId(99L)).isEmpty();
    }

    @Test
    @DisplayName("listarTodos() — debe retornar todas las mediciones")
    void debeListarTodos() {
        MedicionEntity e2 = new MedicionEntity(2L, 6.5, 18.0, LocalDateTime.now(), 2L, 1L, 1L);
        when(jpa.findAll()).thenReturn(List.of(entity, e2));

        List<Medicion> result = repository.listarTodos();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPh()).isEqualTo(7.0);
        assertThat(result.get(1).getPh()).isEqualTo(6.5);
    }

    @Test
    @DisplayName("buscarPorRecurso() — debe filtrar por recurso")
    void debeBuscarPorRecurso() {
        when(jpa.findByIdRecurso(1L)).thenReturn(List.of(entity));

        List<Medicion> result = repository.buscarPorRecurso(1L);

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
