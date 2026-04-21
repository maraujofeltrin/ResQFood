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
import java.util.Optional;
import java.util.UUID;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import ar.edu.itba.paw.models.Client;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

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
    private final ClientService clientService;
    private final ZoneId displayZone;
    private final String mailFrom;
    private final String mailFromName;

    @Autowired
    public ReservationMailServiceImpl(final JavaMailSender mailSender,
            final ReservationTokenDao reservationTokenDao,
            final PackDao packDao,
            final ClientService clientService,
            @Value("${mail.username}") final String mailFrom,
            @Value("${mail.from-name:ResQFood}") final String mailFromName,
            @Value("${app.display-zone:}") final String displayZone) {
        this.mailSender = mailSender;
        this.reservationTokenDao = reservationTokenDao;
        this.packDao = packDao;
        this.clientService = clientService;
        this.mailFrom = mailFrom;
        this.mailFromName = mailFromName;
        this.displayZone = (displayZone == null || displayZone.trim().isEmpty()) ? ZoneId.of("America/Argentina/Buenos_Aires") : ZoneId.of(displayZone.trim());
    }

    @Async("mailTaskExecutor")
    @Override
    public void sendReservationRequestToCommerce(final Reservation reservation, final String commerceEmail,
            final String baseUrl, final String pickupDateStr) {
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

        String clientName = "Cliente";
        if (reservation.getCustomerId() != null) {
            final Long cid = reservation.getCustomerId();
            final java.util.Optional<Client> maybeClient = clientService.findByUserId(cid);
            if (maybeClient.isPresent()) {
                final Client c = maybeClient.get();
                clientName = (c.getName() == null ? "" : c.getName()) + (c.getLastName() == null ? "" : (" " + c.getLastName()));
                clientName = clientName.trim().isEmpty() ? "Cliente" : clientName.trim();
            }
        }
        final String subject = "Solicitud de reserva #" + reservation.getId() + " de " + clientName;
        final String html = buildCommerceHtml(reservation, packTitle, acceptUrl, rejectUrl, pickupDateStr);

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
    public void sendReservationCodeToClient(final Reservation reservation, final String clientEmail,
            final String pickupDateStr) {
        final PackMailInfo packMailInfo = getPackMailInfo(reservation);

        final String subject = "¡Tu reserva de " + packMailInfo.localName() + " te espera!";
        final String html = buildClientHtml(reservation, packMailInfo.packLabel(), pickupDateStr);
        sendHtmlMail(clientEmail, subject, html, "Could not send client pickup code mail");
    }

    @Async("mailTaskExecutor")
    @Override
    public void sendAuctionWinnerCodeToClient(final Reservation reservation, final String clientEmail,
            final String pickupDateStr) {
        final PackMailInfo packMailInfo = getPackMailInfo(reservation);

        final String subject = "¡Ganaste la subasta de " + packMailInfo.localName() + "! Tu código de retiro";
        final String html = buildAuctionWinnerHtml(reservation, packMailInfo.packLabel(), pickupDateStr, false, null);
        sendHtmlMail(clientEmail, subject, html, "Could not send auction winner pickup code mail to client");
    }

    @Async("mailTaskExecutor")
    @Override
    public void sendAuctionWinnerCodeToCommerce(final Reservation reservation, final String commerceEmail,
            final String pickupDateStr) {
        final PackMailInfo packMailInfo = getPackMailInfo(reservation);

        final String subject = "La subasta de " + packMailInfo.localName() + " ya tiene ganador";
        final String winnerName = resolveClientName(reservation.getCustomerId());
        final String html = buildAuctionWinnerHtml(reservation, packMailInfo.packLabel(), pickupDateStr, true,
                winnerName);
        sendHtmlMail(commerceEmail, subject, html, "Could not send auction winner pickup code mail to commerce");
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
        sendHtmlMail(clientEmail, subject, html, "Could not send rejection mail");
    }

    private String buildClientHtml(final Reservation reservation, final String packLabel, final String pickupDateStr) {
        final String reservationDateStr = reservation.getReservationDate() != null
                ? formatToLocal(reservation.getReservationDate())
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

    private String buildAuctionWinnerHtml(final Reservation reservation, final String packLabel,
            final String pickupDateStr, final boolean forCommerce, final String winnerName) {
        final String reservationDateStr = reservation.getReservationDate() != null
                ? formatToLocal(reservation.getReservationDate())
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
        context.setVariable("showCode", !forCommerce);
        context.setVariable("badgeText", forCommerce ? "SUBASTA FINALIZADA" : "SUBASTA GANADA");
        context.setVariable("titleText", forCommerce ? "Subasta finalizada con ganador" : "Ganaste la subasta");
        context.setVariable("introText", forCommerce
            ? "La subasta finalizó con un ganador. Revisá los datos de la reserva para gestionar la entrega."
                : "Presentá este código en el comercio para retirar el pack que ganaste en la subasta.");
        context.setVariable("winnerName", forCommerce ? winnerName : null);

        return templateEngine.process("auction-winner-pickup-code", context);
    }

    private String buildClientRejectedHtml(final Reservation reservation, final String packLabel) {
        final String reservationDateStr = reservation.getReservationDate() != null
                ? formatToLocal(reservation.getReservationDate())
                : "-";

        final Context context = new Context();
        context.setVariable("reservationId", reservation.getId());
        context.setVariable("packLabel", packLabel);
        context.setVariable("reservationDateStr", reservationDateStr);

        return templateEngine.process("client-reservation-rejected", context);
    }

    private String buildCommerceHtml(final Reservation reservation, final String packTitle, final String acceptUrl,
            final String rejectUrl, final String pickupDateStr) {
        final String dateStr = reservation.getReservationDate() != null ? formatToLocal(reservation.getReservationDate()) : "-";
        final String priceStr = reservation.getFinalPrice() != null ? reservation.getFinalPrice().toString() : "-";

        final Context context = new Context();
        context.setVariable("packTitle", packTitle);
        context.setVariable("dateStr", dateStr);
        context.setVariable("priceStr", priceStr);
        context.setVariable("pickupDateStr", pickupDateStr);
        context.setVariable("acceptUrl", acceptUrl);
        context.setVariable("rejectUrl", rejectUrl);

        return templateEngine.process("commerce-reservation", context);
    }

    private PackMailInfo getPackMailInfo(final Reservation reservation) {
        final Pack pack = packDao.findById(reservation.getPackId()).orElse(null);
        final String localName = pack != null ? pack.getTitle() : ("Pack #" + reservation.getPackId());
        final String packLabel = pack != null
                ? (pack.getTitle() + " (#" + pack.getId() + ")")
                : ("Pack #" + reservation.getPackId());
        return new PackMailInfo(localName, packLabel);
    }

    private String resolveClientName(final Long customerId) {
        if (customerId == null) {
            return "Cliente";
        }

        final Optional<Client> maybeClient = clientService.findByUserId(customerId);
        if (maybeClient.isEmpty()) {
            return "Cliente";
        }

        final Client client = maybeClient.get();
        final String fullName = (client.getName() == null ? "" : client.getName())
                + (client.getLastName() == null ? "" : (" " + client.getLastName()));
        return fullName.trim().isEmpty() ? "Cliente" : fullName.trim();
    }

    private void sendHtmlMail(final String toEmail, final String subject, final String html,
            final String errorMessage) {
        try {
            final MimeMessage message = mailSender.createMimeMessage();
            final MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom, mailFromName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (final MessagingException | UnsupportedEncodingException e) {
            throw new IllegalStateException(errorMessage, e);
        }
    }

    private record PackMailInfo(String localName, String packLabel) {
    }

    private String formatToLocal(final java.time.LocalDateTime dt) {
        if (dt == null) return "-";
        try {
            final ZonedDateTime z = ZonedDateTime.of(dt, ZoneOffset.UTC).withZoneSameInstant(displayZone);
            return z.format(MAIL_DATE_FORMATTER);
        } catch (final Exception e) {
            try {
                return dt.format(MAIL_DATE_FORMATTER);
            } catch (final Exception ex) {
                return "-";
            }
        }
    }
}
