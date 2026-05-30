package com.AquaBalance.notifications.infrastructure.email;

import com.AquaBalance.notifications.application.ports.out.NotificacionEmailPort;
import com.AquaBalance.notifications.domain.Notificacion;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class EmailAdapter implements NotificacionEmailPort {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remitente;

    @Value("${notificacion.email.destinatario}")
    private String destinatario;

    @Value("${notificacion.email.habilitado:true}")
    private boolean habilitado;

    public EmailAdapter(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ── Notificaciones de eventos/alertas/informes (correo fijo) ─────────────

    @Override
    @Async
    public void enviar(Notificacion notificacion) {

        System.out.println("📧 EmailAdapter.enviar() — habilitado=" + habilitado
                + " | destinatario=" + destinatario
                + " | nivel=" + notificacion.getNivel());

        if (!habilitado) {
            System.out.println("⚠️  Email deshabilitado en configuración.");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject("🚨 AquaBalance — " + notificacion.getTitulo());
            helper.setText(construirHtml(notificacion), true);

            mailSender.send(message);
            System.out.println("✅ Email enviado exitosamente: "
                    + notificacion.getTitulo());

        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("❌ Error enviando email: " + e.getMessage());
        }
    }

    // ── Bienvenida al usuario recién registrado (correo dinámico) ────────────

    @Async
    public void enviarBienvenida(String emailDestino, String nombre) {

        System.out.println("📧 EmailAdapter.enviarBienvenida() — destinatario="
                + emailDestino + " | nombre=" + nombre);

        if (!habilitado) {
            System.out.println("⚠️  Email deshabilitado en configuración.");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(emailDestino);
            helper.setSubject("💧 Bienvenido a AquaBalance, " + nombre + "!");
            helper.setText(construirHtmlBienvenida(nombre, emailDestino), true);

            mailSender.send(message);
            System.out.println("✅ Correo de bienvenida enviado a: " + emailDestino);

        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("❌ Error enviando bienvenida: " + e.getMessage());
        }
    }

    // ── HTML notificación ─────────────────────────────────────────────────────

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
                    💧 AquaBalance
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

    // ── HTML bienvenida ───────────────────────────────────────────────────────

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
                  <h1 style="color:white;margin:0;font-size:22px">💧 AquaBalance</h1>
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
            """.formatted(nombre, email);
    }
}