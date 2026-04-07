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
import org.springframework.scheduling.annotation.Async;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@Service
public class ReservationMailServiceImpl implements ReservationMailService {

    private static final DateTimeFormatter MAIL_DATE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    private static final TemplateEngine templateEngine;

    static {
        final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("mail/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding("UTF-8");

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
    }

    private final JavaMailSender mailSender;
    private final ReservationTokenDao reservationTokenDao;
    private final PackDao packDao;
    private final String mailFrom;
    private final String mailFromName;

    @Autowired
    public ReservationMailServiceImpl(final JavaMailSender mailSender,
            final ReservationTokenDao reservationTokenDao,
            final PackDao packDao,
            @Value("${mail.username}") final String mailFrom,
            @Value("${mail.from-name:ResQFood}") final String mailFromName) {
        this.mailSender = mailSender;
        this.reservationTokenDao = reservationTokenDao;
        this.packDao = packDao;
        this.mailFrom = mailFrom;
        this.mailFromName = mailFromName;
    }

    @Async("mailTaskExecutor")
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
            helper.setFrom(mailFrom, mailFromName);
            helper.setTo(commerceEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (final MessagingException | UnsupportedEncodingException e) {
            throw new IllegalStateException("Could not send reservation mail", e);
        }
    }

    @Async("mailTaskExecutor")
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
            helper.setFrom(mailFrom, mailFromName);
            helper.setTo(clientEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (final MessagingException | UnsupportedEncodingException e) {
            throw new IllegalStateException("Could not send client pickup code mail", e);
        }
    }

    @Async("mailTaskExecutor")
    @Override
    public void sendReservationRejectedToClient(final Reservation reservation, final String clientEmail) {
        final Pack pack = packDao.findById(reservation.getPackId()).orElse(null);
        final String localName = pack != null ? pack.getTitle() : ("Pack #" + reservation.getPackId());
        final String packLabel = pack != null
            ? (pack.getTitle() + " (#" + pack.getId() + ")")
            : ("Pack #" + reservation.getPackId());

        final String subject = "Tu reserva de " + localName + " fue rechazada";
        final String html = buildClientRejectedHtml(reservation, packLabel);
        try {
            final MimeMessage message = mailSender.createMimeMessage();
            final MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom, mailFromName);
            helper.setTo(clientEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (final MessagingException | UnsupportedEncodingException e) {
            throw new IllegalStateException("Could not send rejection mail", e);
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

        final Context context = new Context();
        context.setVariable("code", code);
        context.setVariable("reservationId", reservation.getId());
        context.setVariable("packLabel", packLabel);
        context.setVariable("reservationDateStr", reservationDateStr);
        context.setVariable("pickupDateStr", pickupDateStr);
        context.setVariable("priceStr", priceStr);

        return templateEngine.process("client-pickup-code", context);
    }

    private static String buildClientRejectedHtml(final Reservation reservation, final String packLabel) {
        final String reservationDateStr = reservation.getReservationDate() != null
                ? reservation.getReservationDate().format(MAIL_DATE_FORMATTER)
                : "-";

        final Context context = new Context();
        context.setVariable("reservationId", reservation.getId());
        context.setVariable("packLabel", packLabel);
        context.setVariable("reservationDateStr", reservationDateStr);

        return templateEngine.process("client-reservation-rejected", context);
    }

    private static String buildCommerceHtml(final Reservation reservation, final String packTitle, final String acceptUrl, final String rejectUrl) {
        final String dateStr = reservation.getReservationDate() != null ? reservation.getReservationDate().format(MAIL_DATE_FORMATTER) : "-";
        final String priceStr = reservation.getFinalPrice() != null ? reservation.getFinalPrice().toString() : "-";
        final String pickupDateStr = reservation.getPickupConfirmationDate() != null ? reservation.getPickupConfirmationDate().format(MAIL_DATE_FORMATTER) : "-";

        final Context context = new Context();
        context.setVariable("packTitle", packTitle);
        context.setVariable("dateStr", dateStr);
        context.setVariable("priceStr", priceStr);
        context.setVariable("pickupDateStr", pickupDateStr);
        context.setVariable("acceptUrl", acceptUrl);
        context.setVariable("rejectUrl", rejectUrl);

        return templateEngine.process("commerce-reservation", context);
    }
}
