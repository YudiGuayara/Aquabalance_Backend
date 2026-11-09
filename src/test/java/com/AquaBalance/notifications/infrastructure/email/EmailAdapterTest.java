// EmailAdapterTest.java
package com.AquaBalance.notifications.infrastructure.email;

import com.AquaBalance.notifications.domain.Notificacion;
import jakarta.mail.Multipart;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import com.AquaBalance.notifications.application.ports.out.DestinatariosAlertaPort;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Properties;
import jakarta.mail.Session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailAdapter - Pruebas Unitarias")
class EmailAdapterTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private DestinatariosAlertaPort destinatariosAlerta;

    @InjectMocks
    private EmailAdapter emailAdapter;

    @Test
    @DisplayName("No debe enviar nada cuando el correo está deshabilitado")
    void noDebeEnviarCuandoEstaDeshabilitado() {
        ReflectionTestUtils.setField(emailAdapter, "remitente", "origen@aqua.com");
        ReflectionTestUtils.setField(emailAdapter, "destinatario", "destino@aqua.com");
        ReflectionTestUtils.setField(emailAdapter, "habilitado", false);

        Notificacion notificacion = new Notificacion(
                "Sistema",
                "Alerta deshabilitada",
                "Este correo no debe enviarse",
                "ALTA"
        );

        emailAdapter.enviar(notificacion);

        verifyNoInteractions(mailSender);
    }

    @Test
    @DisplayName("Debe construir y enviar el correo cuando está habilitado")
    void debeConstruirYEnviarCorreo() throws Exception {
        ReflectionTestUtils.setField(emailAdapter, "remitente", "origen@aqua.com");
        ReflectionTestUtils.setField(emailAdapter, "destinatario", "destino@aqua.com");
        ReflectionTestUtils.setField(emailAdapter, "habilitado", true);

        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        Notificacion notificacion = new Notificacion(
                "Sistema",
                "Nivel de agua crítico",
                "El nivel del tanque está por debajo del umbral.",
                "ALTA"
        );

        emailAdapter.enviar(notificacion);

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);

        // ← El subject incluye el emoji que agrega EmailAdapter
        assertThat(mimeMessage.getSubject()).isEqualTo("🚨 AquaBalance — Nivel de agua crítico");
        assertThat(mimeMessage.getFrom()).hasSize(1);
        assertThat(mimeMessage.getFrom()[0].toString()).contains("origen@aqua.com");
        assertThat(mimeMessage.getAllRecipients()).hasSize(1);
        assertThat(mimeMessage.getAllRecipients()[0].toString()).contains("destino@aqua.com");

        Object content = mimeMessage.getContent();
        assertThat(content).isInstanceOf(Multipart.class);
    }

    @Test
    @DisplayName("Debe usar el mismo mensaje aunque el nivel sea nulo")
    void debeManejarNivelNulo() throws Exception {
        ReflectionTestUtils.setField(emailAdapter, "remitente", "origen@aqua.com");
        ReflectionTestUtils.setField(emailAdapter, "destinatario", "destino@aqua.com");
        ReflectionTestUtils.setField(emailAdapter, "habilitado", true);

        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        Notificacion notificacion = new Notificacion(
                "Sistema",
                "Notificación sin nivel",
                "Prueba de nivel nulo",
                null
        );

        emailAdapter.enviar(notificacion);

        verify(mailSender).send(mimeMessage);
        // ← nivel null → sin emoji
        assertThat(mimeMessage.getSubject()).isEqualTo("🚨 AquaBalance — Notificación sin nivel");
    }

    @Test
    @DisplayName("Debe enviar un correo separado a cada operador")
    void debeEnviarUnCorreoPorOperador() throws Exception {
        ReflectionTestUtils.setField(emailAdapter, "remitente", "origen@aqua.com");
        ReflectionTestUtils.setField(emailAdapter, "destinatario", "");
        ReflectionTestUtils.setField(emailAdapter, "habilitado", true);

        when(destinatariosAlerta.emailsDeOperadores())
                .thenReturn(List.of("op1@aqua.com", "op2@aqua.com"));
        when(mailSender.createMimeMessage())
                .thenAnswer(inv -> new MimeMessage(Session.getInstance(new Properties())));

        emailAdapter.enviar(new Notificacion("Sistema", "Nivel crítico", "Detalle", "ALTA"));

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender, times(2)).send(captor.capture());

        List<MimeMessage> enviados = captor.getAllValues();
        for (MimeMessage m : enviados) {
            assertThat(m.getAllRecipients()).hasSize(1);
        }
        assertThat(enviados.stream().map(m -> {
            try { return m.getAllRecipients()[0].toString(); }
            catch (Exception e) { throw new IllegalStateException(e); }
        }).toList()).containsExactlyInAnyOrder("op1@aqua.com", "op2@aqua.com");
    }

    @Test
    @DisplayName("Debe sumar la dirección fija a los operadores sin repetirla")
    void debeSumarDireccionFijaSinRepetir() throws Exception {
        ReflectionTestUtils.setField(emailAdapter, "remitente", "origen@aqua.com");
        ReflectionTestUtils.setField(emailAdapter, "destinatario", "jefe@aqua.com");
        ReflectionTestUtils.setField(emailAdapter, "habilitado", true);

        when(destinatariosAlerta.emailsDeOperadores())
                .thenReturn(List.of("op1@aqua.com", "jefe@aqua.com"));
        when(mailSender.createMimeMessage())
                .thenAnswer(inv -> new MimeMessage(Session.getInstance(new Properties())));

        emailAdapter.enviar(new Notificacion("Sistema", "Nivel crítico", "Detalle", "ALTA"));

        verify(mailSender, times(2)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("No debe enviar nada si no hay operadores ni dirección fija")
    void noDebeEnviarSinDestinatarios() {
        ReflectionTestUtils.setField(emailAdapter, "remitente", "origen@aqua.com");
        ReflectionTestUtils.setField(emailAdapter, "destinatario", "");
        ReflectionTestUtils.setField(emailAdapter, "habilitado", true);

        when(destinatariosAlerta.emailsDeOperadores()).thenReturn(List.of());

        emailAdapter.enviar(new Notificacion("Sistema", "Nivel crítico", "Detalle", "ALTA"));

        verifyNoInteractions(mailSender);
    }
}
