package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.Reservation;
import ar.edu.itba.paw.models.ReservationToken;
import ar.edu.itba.paw.persistence.ReservationTokenDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ReservationMailServiceImpl implements ReservationMailService {

    private final JavaMailSender mailSender;
    private final ReservationTokenDao reservationTokenDao;
    private final String mailFrom;

    @Autowired
    public ReservationMailServiceImpl(final JavaMailSender mailSender,
            final ReservationTokenDao reservationTokenDao,
            @Value("${mail.username}") final String mailFrom) {
        this.mailSender = mailSender;
        this.reservationTokenDao = reservationTokenDao;
        this.mailFrom = mailFrom;
    }

    @Override
    public void sendReservationRequestToCommerce(final Reservation reservation, final String commerceEmail,
            final String baseUrl) {
        final String acceptToken = UUID.randomUUID().toString();
        final String rejectToken = UUID.randomUUID().toString();
        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime expiresAt = now.plusHours(48);

        reservationTokenDao.create(acceptToken, reservation.getId(), ReservationToken.Action.ACCEPT, now, expiresAt);
        reservationTokenDao.create(rejectToken, reservation.getId(), ReservationToken.Action.REJECT, now, expiresAt);

        final String normalizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        final String acceptUrl = normalizedBase + "/reservations/accept?token=" + acceptToken;
        final String rejectUrl = normalizedBase + "/reservations/reject?token=" + rejectToken;

        final String subject = "Solicitud de reserva #" + reservation.getId();
        final String html = buildHtml(reservation, acceptUrl, rejectUrl);

        try {
            final MimeMessage message = mailSender.createMimeMessage();
            final MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(commerceEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (final MessagingException e) {
            throw new IllegalStateException("Could not send reservation mail", e);
        }
    }

    private static String buildHtml(final Reservation reservation, final String acceptUrl, final String rejectUrl) {
        final String dateStr = reservation.getReservationDate() != null ? reservation.getReservationDate().toString()
                : "-";
        final String priceStr = reservation.getFinalPrice() != null ? reservation.getFinalPrice().toString() : "-";
        final String statusStr = reservation.getStatus() != null ? reservation.getStatus().name() : "-";

        return "<!DOCTYPE html><html><body style=\"font-family: system-ui, sans-serif; line-height: 1.5; color: #111827;\">"
                + "<p>Hay una nueva solicitud de reserva para el comercio.</p>"
                + "<ul>"
                + "<li><strong>Reserva:</strong> " + reservation.getId() + "</li>"
                + "<li><strong>Pack:</strong> " + reservation.getPackId() + "</li>"
                + "<li><strong>Fecha:</strong> " + escapeHtml(dateStr) + "</li>"
                + "<li><strong>Precio final:</strong> " + escapeHtml(priceStr) + "</li>"
                + "<li><strong>Estado:</strong> " + escapeHtml(statusStr) + "</li>"
                + "</ul>"
                + "<p style=\"margin-top: 1.5rem;\">Usá los botones de abajo (enlaces válidos 48 horas). "
                + "Si tocás <strong>Rechazar</strong>, en el sitio te pediremos confirmar antes de anular la reserva.</p>"
                + "<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin-top: 1rem;\"><tr>"
                + "<td style=\"padding: 8px 12px;\"><a href=\"" + acceptUrl + "\" "
                + "style=\"display: inline-block; padding: 12px 24px; background: #16a34a; color: #ffffff; "
                + "text-decoration: none; border-radius: 8px; font-weight: 600;\">Aceptar</a></td>"
                + "<td style=\"padding: 8px 12px;\"><a href=\"" + rejectUrl + "\" "
                + "style=\"display: inline-block; padding: 12px 24px; background: #dc2626; color: #ffffff; "
                + "text-decoration: none; border-radius: 8px; font-weight: 600;\">Rechazar</a></td>"
                + "</tr></table>"
                + "</body></html>";
    }

    private static String escapeHtml(final String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
