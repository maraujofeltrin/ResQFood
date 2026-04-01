package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.Pack;
import ar.edu.itba.paw.models.Reservation;
import ar.edu.itba.paw.models.ReservationToken;
import ar.edu.itba.paw.persistence.PackDao;
import ar.edu.itba.paw.persistence.ReservationTokenDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class ReservationMailServiceImpl implements ReservationMailService {

    private static final DateTimeFormatter MAIL_DATE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    private final JavaMailSender mailSender;
    private final ReservationTokenDao reservationTokenDao;
    private final PackDao packDao;
    private final String mailFrom;

    @Autowired
    public ReservationMailServiceImpl(final JavaMailSender mailSender,
            final ReservationTokenDao reservationTokenDao,
            final PackDao packDao,
            @Value("${mail.username}") final String mailFrom) {
        this.mailSender = mailSender;
        this.reservationTokenDao = reservationTokenDao;
        this.packDao = packDao;
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

        final Pack pack = packDao.findById(reservation.getPackId()).orElse(null);
        final String packTitle = pack != null ? pack.getTitle() : "Pack #" + reservation.getPackId();

        final String subject = "Solicitud de reserva #" + reservation.getId();
        final String html = buildCommerceHtml(reservation, packTitle, acceptUrl, rejectUrl);

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

    @Override
    public void sendReservationCodeToClient(final Reservation reservation, final String clientEmail) {
        final Pack pack = packDao.findById(reservation.getPackId()).orElse(null);
        final String localName = pack != null ? pack.getTitle() : ("Pack #" + reservation.getPackId());
        final String packLabel = pack != null
            ? (pack.getTitle() + " (#" + pack.getId() + ")")
            : ("Pack #" + reservation.getPackId());

        final String subject = "¡Tu reserva de " + localName + " te espera!";
        final String html = buildClientHtml(reservation, packLabel);
        try {
            final MimeMessage message = mailSender.createMimeMessage();
            final MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(clientEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (final MessagingException e) {
            throw new IllegalStateException("Could not send client pickup code mail", e);
        }
    }

    private static String buildClientHtml(final Reservation reservation, final String packLabel) {
        final String reservationDateStr = reservation.getReservationDate() != null
                ? reservation.getReservationDate().format(MAIL_DATE_FORMATTER)
                : "-";
        final String pickupDateStr = reservation.getPickupConfirmationDate() != null
                ? reservation.getPickupConfirmationDate().format(MAIL_DATE_FORMATTER)
                : "-";
        final String priceStr = reservation.getFinalPrice() != null ? reservation.getFinalPrice().toString() : "-";
        final String code = reservation.getPickupCode() == null ? "" : reservation.getPickupCode();

        return "<!DOCTYPE html><html><body style=\"margin:0; padding:40px; background:#f3f4ff; font-family:Arial, Helvetica, sans-serif; color:#1f2440;\">"
            + "<div style=\"max-width:600px; margin:0 auto; background:#ffffff; border:1px solid #d7d9ea; border-radius:24px; padding:40px;\">"
            + "<span style=\"display:inline-block; background:#dde3ff; color:#2f3f86; border-radius:999px; padding:6px 12px; font-size:11px; font-weight:700; text-transform:uppercase; letter-spacing:0.06em; margin-bottom:16px;\">RESERVA CONFIRMADA</span>"
            + "<h1 style=\"margin:0 0 16px; color:#2f3f86; font-size:32px; line-height:1.2; font-weight:800;\">Tu código de retiro</h1>"
            + "<p style=\"margin:0 0 24px; color:#5b617c; font-size:15px; line-height:1.6;\">Presentá este código en el comercio para retirar tu pedido.</p>"
            + "<div style=\"text-align:center; margin:0 0 28px;\">"
            + "<div style=\"display:inline-block; border:2px dashed #2f3f86; background:#f6f7ff; color:#2f3f86; border-radius:16px; padding:14px 26px; font-size:42px; letter-spacing:0.12em; font-weight:800;\">"
            + escapeHtml(code) + "</div>"
            + "</div>"
            + "<div style=\"background:#f6f7ff; border:1px solid #d7d9ea; border-radius:16px; padding:20px;\">"
            + "<p style=\"margin:0 0 8px; color:#1f2440; font-size:14px; line-height:1.6;\"><strong style=\"color:#2f3f86;\">Número de reserva:</strong> #"
            + reservation.getId() + "</p>"
            + "<p style=\"margin:0 0 8px; color:#1f2440; font-size:14px; line-height:1.6;\"><strong style=\"color:#2f3f86;\">Pack:</strong> "
            + escapeHtml(packLabel) + "</p>"
            + "<p style=\"margin:0 0 8px; color:#1f2440; font-size:14px; line-height:1.6;\"><strong style=\"color:#2f3f86;\">Fecha de reserva:</strong> "
            + escapeHtml(reservationDateStr) + "</p>"
            + "<p style=\"margin:0 0 8px; color:#1f2440; font-size:14px; line-height:1.6;\"><strong style=\"color:#2f3f86;\">Fecha de retiro:</strong> "
            + escapeHtml(pickupDateStr) + "</p>"
            + "<p style=\"margin:0; color:#1f2440; font-size:14px; line-height:1.6;\"><strong style=\"color:#2f3f86;\">Precio de la reserva:</strong> $"
            + escapeHtml(priceStr) + "</p>"
            + "</div>"
            + "<p style=\"margin:28px 0 0; padding-top:20px; border-top:1px solid #d7d9ea; color:#5b617c; font-size:12px; line-height:1.6;\">"
            + "Este correo fue enviado automáticamente.</p>"
            + "</div></body></html>";
    }

    private static String buildCommerceHtml(final Reservation reservation, final String packTitle, final String acceptUrl, final String rejectUrl) {
        final String dateStr = reservation.getReservationDate() != null ? reservation.getReservationDate().format(MAIL_DATE_FORMATTER) : "-";
        final String priceStr = reservation.getFinalPrice() != null ? reservation.getFinalPrice().toString() : "-";
        final String pickupDateStr = reservation.getPickupConfirmationDate() != null ? reservation.getPickupConfirmationDate().format(MAIL_DATE_FORMATTER) : "-";

        return "<!DOCTYPE html><html><body style=\"margin:0; padding:40px; background:#f3f4ff; font-family:Arial, Helvetica, sans-serif; color:#1f2440;\">"
            + "<div style=\"max-width:600px; margin:0 auto; background:#ffffff; border:1px solid #d7d9ea; border-radius:24px; padding:40px;\">"
            + "<span style=\"display:inline-block; background:#dde3ff; color:#2f3f86; border-radius:999px; padding:6px 12px; "
            + "font-size:11px; font-weight:700; text-transform:uppercase; letter-spacing:0.06em; margin-bottom:16px;\">NUEVA RESERVA</span>"
            + "<h1 style=\"margin:0 0 16px; color:#2f3f86; font-size:34px; line-height:1.2; font-weight:800;\">Tenés una nueva reserva</h1>"
            + "<p style=\"margin:0 0 32px; color:#5b617c; font-size:15px; line-height:1.6;\">"
            + "Revisá los datos y decidí si aceptás o rechazás la solicitud.</p>"
            + "<div style=\"background:#f6f7ff; border:1px solid #d7d9ea; border-radius:16px; padding:20px; margin-bottom:32px;\">"
            + "<p style=\"margin:0 0 8px; color:#1f2440; font-size:14px; line-height:1.6;\"><strong style=\"color:#2f3f86;\">Pack:</strong> "
            + escapeHtml(packTitle) + "</p>"
            + "<p style=\"margin:0 0 8px; color:#1f2440; font-size:14px; line-height:1.6;\"><strong style=\"color:#2f3f86;\">Fecha de reserva:</strong> "
            + escapeHtml(dateStr) + "</p>"
            + "<p style=\"margin:0 0 8px; color:#1f2440; font-size:14px; line-height:1.6;\"><strong style=\"color:#2f3f86;\">Precio final:</strong> "
            + escapeHtml(priceStr) + "</p>"
            + "<p style=\"margin:0; color:#1f2440; font-size:14px; line-height:1.6;\"><strong style=\"color:#2f3f86;\">Fecha de retiro:</strong> "
            + escapeHtml(pickupDateStr) + "</p>"
            + "</div>"
            + "<div>"
            + "<a href=\"" + acceptUrl + "\" style=\"display:inline-block; text-decoration:none; margin-right:12px; "
            + "border-radius:999px; padding:12px 24px; font-weight:700; font-size:14px; line-height:1; color:#ffffff; background:#2f3f86;\">"
            + "Aceptar reserva</a>"
            + "<a href=\"" + rejectUrl + "\" style=\"display:inline-block; text-decoration:none; "
            + "border-radius:999px; padding:12px 24px; font-weight:700; font-size:14px; line-height:1; color:#ffffff; background:#9f1239;\">"
            + "Rechazar reserva</a>"
            + "</div>"
            + "<p style=\"margin:32px 0 0; padding-top:24px; border-top:1px solid #d7d9ea; color:#5b617c; font-size:12px; line-height:1.6;\">"
            + "Este mail fue enviado automáticamente. Los enlaces son de uso único y expiran en 48 horas.</p>"
            + "</div>"
                + "</body></html>";
    }

    private static String escapeHtml(final String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
