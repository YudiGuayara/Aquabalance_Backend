package com.AquaBalance.notifications.application;

import com.AquaBalance.notifications.application.ports.out.NotificacionEmailPort;
import com.AquaBalance.notifications.application.ports.out.NotificacionMessagingPort;
import com.AquaBalance.notifications.domain.Notificacion;
import com.AquaBalance.notifications.infrastructure.persistence.NotificacionEntity;
import com.AquaBalance.notifications.infrastructure.persistence.NotificacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("NotificacionService - Pruebas Unitarias")
class NotificacionServiceTest {

    @Mock private NotificacionMessagingPort messagingPort;
    @Mock private NotificacionEmailPort     emailPort;
    @Mock private NotificacionRepository    repository;

    @InjectMocks
    private NotificacionService service;

    /** Devuelve una entidad guardada con ID asignado */
    private NotificacionEntity entityConId(Long id) {
        NotificacionEntity e = new NotificacionEntity();
        e.setId(id);
        e.setLeida(false);
        e.setFecha(LocalDateTime.now());
        return e;
    }

    @BeforeEach
    void setUp() {
        // Por defecto, repository.save() devuelve entidad con id=1
        when(repository.save(any())).thenReturn(entityConId(1L));
    }

    // ================================================================
    // NOTIFICAR
    // ================================================================
    @Nested
    @DisplayName("notificar()")
    class Notificar {

        @Test
        @DisplayName("Debe guardar en BD y asignar el ID retornado por el repositorio")
        void debeGuardarEnBDYAsignarId() {
            when(repository.save(any())).thenReturn(entityConId(42L));
            doNothing().when(messagingPort).enviar(any());

            Notificacion n = new Notificacion("ALERTA", "Título", "Mensaje", "BAJA");
            service.notificar(n);

            assertThat(n.getId()).isEqualTo(42L);
            verify(repository).save(any(NotificacionEntity.class));
        }

        @Test
        @DisplayName("Debe enviar siempre por WebSocket (messagingPort)")
        void debeEnviarSiemprePorWebSocket() {
            doNothing().when(messagingPort).enviar(any());

            service.notificar(new Notificacion("TIPO", "T", "M", "BAJA"));

            verify(messagingPort).enviar(any());
        }

        @Test
        @DisplayName("Debe enviar email si el nivel es ALTA")
        void debeEnviarEmailSiNivelEsAlta() {
            doNothing().when(messagingPort).enviar(any());
            doNothing().when(emailPort).enviar(any());

            service.notificar(new Notificacion("ALERTA", "T", "M", "ALTA"));

            verify(emailPort).enviar(any());
        }

        @Test
        @DisplayName("Debe enviar email si el nivel es MEDIA")
        void debeEnviarEmailSiNivelEsMedia() {
            doNothing().when(messagingPort).enviar(any());
            doNothing().when(emailPort).enviar(any());

            service.notificar(new Notificacion("INFORME", "T", "M", "MEDIA"));

            verify(emailPort).enviar(any());
        }

        @Test
        @DisplayName("NO debe enviar email si el nivel es BAJA")
        void noDebeEnviarEmailSiNivelEsBaja() {
            doNothing().when(messagingPort).enviar(any());

            service.notificar(new Notificacion("MEDICION", "T", "M", "BAJA"));

            verify(emailPort, never()).enviar(any());
        }

        @Test
        @DisplayName("NO debe enviar email si el nivel es nulo")
        void noDebeEnviarEmailSiNivelEsNulo() {
            doNothing().when(messagingPort).enviar(any());

            service.notificar(new Notificacion("INFO", "T", "M", null));

            verify(emailPort, never()).enviar(any());
        }

        @Test
        @DisplayName("Debe comparar nivel sin importar mayúsculas/minúsculas")
        void debeCompararNivelCaseInsensitive() {
            doNothing().when(messagingPort).enviar(any());
            doNothing().when(emailPort).enviar(any());

            service.notificar(new Notificacion("ALERTA", "T", "M", "alta"));

            verify(emailPort).enviar(any());
        }
    }

    // ================================================================
    // NOTIFICAR ALERTA (método de conveniencia)
    // ================================================================
    @Nested
    @DisplayName("notificarAlerta()")
    class NotificarAlerta {

        @Test
        @DisplayName("Debe guardar y enviar por WebSocket con tipo ALERTA y nivel correcto")
        void debeCrearNotificacionDeAlerta() {
            doNothing().when(messagingPort).enviar(any());
            doNothing().when(emailPort).enviar(any());

            service.notificarAlerta("pH fuera de rango", "ALTA");

            verify(repository).save(argThat(e ->
                    "ALERTA".equals(e.getTipo()) &&
                    "ALTA".equals(e.getNivel()) &&
                    "pH fuera de rango".equals(e.getMensaje())
            ));
            verify(messagingPort).enviar(any());
        }
    }

    // ================================================================
    // NOTIFICAR INFORME
    // ================================================================
    @Nested
    @DisplayName("notificarInforme()")
    class NotificarInforme {

        @Test
        @DisplayName("Debe crear notificación de tipo INFORME con nivel INFO (sin email)")
        void debeCrearNotificacionDeInforme() {
            doNothing().when(messagingPort).enviar(any());

            service.notificarInforme("Informe Trimestral Q1");

            verify(repository).save(argThat(e ->
                    "INFORME".equals(e.getTipo()) &&
                    "INFO".equals(e.getNivel()) &&
                    e.getMensaje().contains("Informe Trimestral Q1")
            ));
            verify(emailPort, never()).enviar(any()); // INFO no dispara email
        }
    }

    // ================================================================
    // NOTIFICAR MEDICION
    // ================================================================
    @Nested
    @DisplayName("notificarMedicion()")
    class NotificarMedicion {

        @Test
        @DisplayName("Debe crear notificación de tipo MEDICION con nivel BAJA (sin email)")
        void debeCrearNotificacionDeMedicion() {
            doNothing().when(messagingPort).enviar(any());

            service.notificarMedicion("Río Bogotá", "Mercurio", 5.2);

            verify(repository).save(argThat(e ->
                    "MEDICION".equals(e.getTipo()) &&
                    "BAJA".equals(e.getNivel()) &&
                    e.getMensaje().contains("Mercurio") &&
                    e.getMensaje().contains("5.2")
            ));
            verify(emailPort, never()).enviar(any());
        }
    }

    // ================================================================
    // OBTENER HISTORIAL
    // ================================================================
    @Nested
    @DisplayName("obtenerHistorial()")
    class ObtenerHistorial {

        @Test
        @DisplayName("Debe retornar las notificaciones mapeadas del repositorio")
        void debeRetornarHistorialDesdeRepositorio() {
            NotificacionEntity e1 = entityConId(1L);
            e1.setTipo("ALERTA"); e1.setTitulo("T1"); e1.setMensaje("M1"); e1.setNivel("ALTA");
            NotificacionEntity e2 = entityConId(2L);
            e2.setTipo("INFORME"); e2.setTitulo("T2"); e2.setMensaje("M2"); e2.setNivel("INFO");

            when(repository.findTop50ByOrderByFechaDesc()).thenReturn(List.of(e1, e2));

            List<Notificacion> historial = service.obtenerHistorial();

            assertThat(historial).hasSize(2);
            assertThat(historial.get(0).getTipo()).isEqualTo("ALERTA");
            assertThat(historial.get(1).getTipo()).isEqualTo("INFORME");
        }

        @Test
        @DisplayName("Debe retornar lista vacía si no hay notificaciones")
        void debeRetornarListaVaciaSiNoHayNotificaciones() {
            when(repository.findTop50ByOrderByFechaDesc()).thenReturn(List.of());

            assertThat(service.obtenerHistorial()).isEmpty();
        }
    }

    // ================================================================
    // CONTAR NO LEIDAS
    // ================================================================
    @Nested
    @DisplayName("contarNoLeidas()")
    class ContarNoLeidas {

        @Test
        @DisplayName("Debe delegar en el repositorio y retornar el conteo correcto")
        void debeDelegarEnRepositorio() {
            when(repository.countByLeidaFalse()).thenReturn(7L);

            assertThat(service.contarNoLeidas()).isEqualTo(7L);
            verify(repository).countByLeidaFalse();
        }
    }

    // ================================================================
    // MARCAR TODAS LEIDAS
    // ================================================================
    @Nested
    @DisplayName("marcarTodasLeidas()")
    class MarcarTodasLeidas {

        @Test
        @DisplayName("Debe llamar a marcarTodasLeidas() del repositorio")
        void debeLlamarAlRepositorio() {
            doNothing().when(repository).marcarTodasLeidas();

            service.marcarTodasLeidas();

            verify(repository).marcarTodasLeidas();
        }
    }

    // ================================================================
    // MARCAR UNA LEIDA
    // ================================================================
    @Nested
    @DisplayName("marcarLeida(id)")
    class MarcarLeida {

        @Test
        @DisplayName("Debe marcar como leída la notificación si existe")
        void debeMarcarComoLeidaSiExiste() {
            NotificacionEntity entidad = entityConId(5L);
            entidad.setLeida(false);
            when(repository.findById(5L)).thenReturn(Optional.of(entidad));
            when(repository.save(any())).thenReturn(entidad);

            service.marcarLeida(5L);

            assertThat(entidad.isLeida()).isTrue();
            verify(repository).save(entidad);
        }

        @Test
        @DisplayName("NO debe lanzar excepción si el ID no existe")
        void noDebeLanzarExcepcionSiIdNoExiste() {
            when(repository.findById(999L)).thenReturn(Optional.empty());

            assertThatNoException().isThrownBy(() -> service.marcarLeida(999L));
            verify(repository, never()).save(any());
        }
    }
}
