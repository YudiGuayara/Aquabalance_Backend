package com.AquaBalance.notifications.infrastructure.email;

import com.AquaBalance.notifications.application.ports.out.DestinatariosAlertaPort;
import com.AquaBalance.notifications.application.ports.out.NotificacionEmailPort;
import com.AquaBalance.notifications.domain.Notificacion;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class EmailAdapter implements NotificacionEmailPort {

    private static final Logger log = LoggerFactory.getLogger(EmailAdapter.class);

    private final JavaMailSender mailSender;
    private final DestinatariosAlertaPort destinatariosAlerta;

    @Value("${spring.mail.username}")
    private String remitente;

    @Value("${notificacion.email.destinatario}")
    private String destinatario;

    @Value("${notificacion.email.habilitado:true}")
    private boolean habilitado;

    // Si brevo.api-key tiene valor, los correos salen por HTTPS (funciona en Railway Free/Hobby).
    // Si esta vacia, se usa SMTP (Gmail) como antes.
    @Value("${brevo.api-key:}")
    private String brevoApiKey;

    @Value("${brevo.sender-email:}")
    private String brevoSenderEmail;

    @Value("${brevo.sender-name:AquaBalance}")
    private String brevoSenderName;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public EmailAdapter(JavaMailSender mailSender, DestinatariosAlertaPort destinatariosAlerta) {
        this.mailSender = mailSender;
        this.destinatariosAlerta = destinatariosAlerta;
    }


    @Override
    @Async
    public void enviar(Notificacion notificacion) {

        if (!habilitado) {
            log.warn("Email deshabilitado en configuración (notificacion.email.habilitado=false).");
            return;
        }

        List<String> destinos = resolverDestinatarios();
        log.debug("EmailAdapter.enviar() — destinatarios={} | nivel={}", destinos.size(), notificacion.getNivel());

        if (destinos.isEmpty()) {
            log.warn("La alerta '{}' no se envió por correo: no hay operadores activos con cuenta verificada "
                    + "ni NOTIFICACION_EMAIL_DESTINATARIO configurado.", notificacion.getTitulo());
            return;
        }

        String asunto = "🚨 AquaBalance — " + notificacion.getTitulo();
        String html   = construirHtml(notificacion);

        // Un correo por persona: así nadie ve las direcciones de los demás operadores
        for (String destino : destinos) {
            try {
                despachar(destino, asunto, html);
                log.info("Email enviado a {}: {}", destino, notificacion.getTitulo());
            } catch (MessagingException | org.springframework.mail.MailException e) {
                log.error("Error enviando email '{}' a {}: {}", notificacion.getTitulo(), destino, e.getMessage(), e);
            }
        }
    }

    /** Operadores activos y verificados, más la dirección fija opcional de NOTIFICACION_EMAIL_DESTINATARIO. */
    private List<String> resolverDestinatarios() {
        Set<String> destinos = new LinkedHashSet<>();

        if (destinatariosAlerta != null) {
            try {
                List<String> operadores = destinatariosAlerta.emailsDeOperadores();
                if (operadores != null) {
                    for (String email : operadores) {
                        if (email != null && !email.isBlank()) destinos.add(email.trim());
                    }
                }
            } catch (RuntimeException e) {
                log.error("No se pudo consultar a los operadores para enviar la alerta: {}", e.getMessage());
            }
        }

        if (destinatario != null && !destinatario.isBlank()) {
            destinos.add(destinatario.trim());
        }
        return new ArrayList<>(destinos);
    }


    @Async
    public void enviarBienvenida(String emailDestino, String nombre) {

        log.debug("EmailAdapter.enviarBienvenida() — destinatario={}", emailDestino);

        if (!habilitado) {
            log.warn("Email deshabilitado en configuración (notificacion.email.habilitado=false).");
            return;
        }

        try {
            despachar(emailDestino,
                    " Bienvenido a AquaBalance, " + nombre + "!",
                    construirHtmlBienvenida(nombre, emailDestino));
            log.info("Correo de bienvenida enviado a {}", emailDestino);

        } catch (MessagingException | org.springframework.mail.MailException e) {
            log.error("Error enviando bienvenida a {}: {}", emailDestino, e.getMessage(), e);
        }
    }


    // ── Correos de cuenta (verificacion y recuperacion) ──────────────────

    @Async
    public void enviarVerificacion(String emailDestino, String nombre, String enlace) {
        enviarCorreoCuenta(
                emailDestino,
                "Activa tu cuenta de AquaBalance",
                "Confirma tu correo",
                "Hola " + esc(nombre) + ", gracias por registrarte en <strong>AquaBalance</strong>. "
                        + "Para activar tu cuenta confirma tu correo electrónico con el siguiente botón. "
                        + "El enlace es válido por 24 horas.",
                "Activar mi cuenta",
                enlace,
                "Si no creaste esta cuenta, ignora este mensaje."
        );
    }

    @Async
    public void enviarRecuperacionPassword(String emailDestino, String nombre, String enlace) {
        enviarCorreoCuenta(
                emailDestino,
                "Restablece tu contraseña de AquaBalance",
                "Restablecer contraseña",
                "Hola " + esc(nombre) + ", recibimos una solicitud para restablecer la contraseña de tu cuenta. "
                        + "Usa el siguiente botón para elegir una nueva. El enlace es válido por 1 hora y solo se puede usar una vez.",
                "Crear nueva contraseña",
                enlace,
                "Si no fuiste tú, ignora este mensaje: tu contraseña actual no cambiará."
        );
    }

    private void enviarCorreoCuenta(String destino, String asunto, String titulo,
                                    String textoHtml, String textoBoton,
                                    String enlace, String pie) {
        if (!habilitado) {
            log.warn("Email deshabilitado (notificacion.email.habilitado=false); no se envía '{}' a {}", asunto, destino);
            return;
        }
        try {
            despachar(destino, asunto,
                    construirHtmlCuenta(titulo, textoHtml, textoBoton, enlace, pie));
            log.info("Correo de cuenta '{}' enviado a {}", asunto, destino);
        } catch (MessagingException | org.springframework.mail.MailException e) {
            // No se registra el enlace ni el token en el log
            log.error("Error enviando correo de cuenta '{}' a {}: {}", asunto, destino, e.getMessage());
        }
    }

    private String construirHtmlCuenta(String titulo, String textoHtml, String textoBoton,
                                       String enlace, String pie) {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="font-family:Arial,sans-serif;background:#f7fafc;padding:0;margin:0">
              <div style="max-width:560px;margin:32px auto;background:white;border-radius:12px;
                          overflow:hidden;box-shadow:0 4px 12px rgba(0,0,0,0.1)">
                <div style="background:linear-gradient(135deg,#0077b6,#023e8a);padding:28px 32px">
                  <h1 style="color:white;margin:0;font-size:22px">AquaBalance</h1>
                  <p style="color:rgba(255,255,255,0.8);margin:6px 0 0;font-size:13px">
                    Sistema de Monitoreo Hídrico
                  </p>
                </div>
                <div style="padding:28px 32px">
                  <h2 style="color:#1a3c5e;margin:0 0 12px;font-size:20px">%s</h2>
                  <p style="color:#4a5568;line-height:1.6;margin:0 0 24px">%s</p>
                  <p style="margin:0 0 24px">
                    <a href="%s" style="background:#0077b6;color:white;text-decoration:none;
                       padding:12px 24px;border-radius:8px;font-weight:700;display:inline-block">%s</a>
                  </p>
                  <p style="color:#718096;font-size:12px;line-height:1.5;margin:0 0 8px">
                    Si el botón no funciona, copia y pega este enlace en tu navegador:<br>%s
                  </p>
                  <p style="color:#a0aec0;font-size:12px;margin:12px 0 0">%s</p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(titulo, textoHtml, enlace, textoBoton, enlace, pie);
    }

    // ── Envio (Brevo por HTTPS o SMTP) ───────────────────────────────────

    private void despachar(String destino, String asunto, String html) throws MessagingException {
        if (brevoApiKey != null && !brevoApiKey.isBlank()) {
            enviarPorBrevo(destino, asunto, html);
            return;
        }
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(remitente);
        helper.setTo(destino);
        helper.setSubject(asunto);
        helper.setText(html, true);
        mailSender.send(message);
    }

    private void enviarPorBrevo(String destino, String asunto, String html) {
        String emailRemitente = (brevoSenderEmail != null && !brevoSenderEmail.isBlank())
                ? brevoSenderEmail : remitente;

        String json = "{"
                + "\"sender\":{\"name\":" + js(brevoSenderName) + ",\"email\":" + js(emailRemitente) + "},"
                + "\"to\":[{\"email\":" + js(destino) + "}],"
                + "\"subject\":" + js(asunto) + ","
                + "\"htmlContent\":" + js(html)
                + "}";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .timeout(Duration.ofSeconds(20))
                    .header("api-key", brevoApiKey.trim())
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() / 100 != 2) {
                throw new org.springframework.mail.MailSendException(
                        "Brevo respondio " + resp.statusCode() + ": " + resp.body());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new org.springframework.mail.MailSendException("Envio por Brevo interrumpido", e);
        } catch (java.io.IOException e) {
            throw new org.springframework.mail.MailSendException("No se pudo conectar con Brevo: " + e.getMessage(), e);
        }
    }

    /** Convierte un texto en un string JSON valido (con comillas). */
    private static String js(String t) {
        if (t == null) return "\"\"";
        StringBuilder sb = new StringBuilder("\"");
        for (char c : t.toCharArray()) {
            switch (c) {
                case '"'  -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.append('"').toString();
    }

    private static String esc(String texto) {
        if (texto == null) return "";
        return texto.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }

    private String construirHtml(Notificacion n) {

        String colorNivel = switch (
                n.getNivel() != null ? n.getNivel().toUpperCase() : "") {
            case "ALTA"  -> "#e53e3e";
            case "MEDIA" -> "#dd6b20";
            case "BAJA"  -> "#38a169";
            default      -> "#0077b6";
        };

        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="font-family:Arial,sans-serif;background:#f7fafc;
                         padding:0;margin:0">
              <div style="max-width:560px;margin:32px auto;background:white;
                          border-radius:12px;overflow:hidden;
                          box-shadow:0 4px 12px rgba(0,0,0,0.1)">
                <div style="background:linear-gradient(135deg,#0077b6,#023e8a);
                            padding:28px 32px">
                  <h1 style="color:white;margin:0;font-size:20px">
                     AquaBalance
                  </h1>
                  <p style="color:rgba(255,255,255,0.8);margin:6px 0 0;
                             font-size:13px">
                    Sistema de Monitoreo Hídrico
                  </p>
                </div>
                <div style="padding:28px 32px">
                  <div style="display:inline-block;background:%s;color:white;
                              padding:4px 14px;border-radius:999px;
                              font-size:12px;font-weight:700;margin-bottom:16px">
                    %s — %s
                  </div>
                  <h2 style="color:#1a3c5e;margin:0 0 12px;font-size:18px">
                    %s
                  </h2>
                  <p style="color:#4a5568;margin:0 0 20px;line-height:1.6">
                    %s
                  </p>
                  <p style="color:#a0aec0;font-size:12px;margin:0">%s</p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(
                colorNivel,
                n.getTipo(),
                n.getNivel() != null ? n.getNivel() : "INFO",
                n.getTitulo(),
                n.getMensaje(),
                n.getFecha() != null ? n.getFecha().toString() : ""
        );
    }


    private String construirHtmlBienvenida(String nombre, String email) {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="font-family:Arial,sans-serif;background:#f7fafc;padding:0;margin:0">
              <div style="max-width:560px;margin:32px auto;background:white;
                          border-radius:12px;overflow:hidden;
                          box-shadow:0 4px 12px rgba(0,0,0,0.1)">
                <div style="background:linear-gradient(135deg,#0077b6,#023e8a);
                            padding:28px 32px">
                  <h1 style="color:white;margin:0;font-size:22px"> AquaBalance</h1>
                  <p style="color:rgba(255,255,255,0.8);margin:6px 0 0;font-size:13px">
                    Sistema de Monitoreo Hídrico
                  </p>
                </div>
                <div style="padding:28px 32px">
                  <h2 style="color:#1a3c5e;margin:0 0 12px;font-size:20px">
                    ¡Bienvenido, %s! 🎉
                  </h2>
                  <p style="color:#4a5568;line-height:1.6;margin:0 0 16px">
                    Tu cuenta ha sido creada exitosamente en <strong>AquaBalance</strong>.
                    Ya puedes iniciar sesión y comenzar a monitorear los recursos hídricos.
                  </p>
                  <div style="background:#ebf8ff;border-left:4px solid #0077b6;
                              padding:12px 16px;border-radius:4px;margin-bottom:20px">
                    <p style="margin:0;color:#2b6cb0;font-size:14px">
                      📧 <strong>Correo registrado:</strong> %s
                    </p>
                  </div>
                  <p style="color:#a0aec0;font-size:12px;margin:0">
                    Si no creaste esta cuenta, ignora este mensaje.
                  </p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(esc(nombre), esc(email));
    }
}