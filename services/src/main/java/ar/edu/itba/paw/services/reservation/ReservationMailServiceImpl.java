package ar.edu.itba.paw.services.reservation;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.reservation.ReservationToken;
import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.persistence.PackDao;
import ar.edu.itba.paw.persistence.ReservationTokenDao;
import ar.edu.itba.paw.services.user.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import ar.edu.itba.paw.services.mail.MailMessageResolver;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReservationMailServiceImpl implements ReservationMailService {

    private static final DateTimeFormatter MAIL_DATE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
    private static final TemplateEngine templateEngine;
    private static final ResourceBundleMessageSource mailMessages;

    static {
        final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("mail/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding("UTF-8");

        final MailMessageResolver messageResolver = new MailMessageResolver();

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        templateEngine.setMessageResolver(messageResolver);

        mailMessages = new ResourceBundleMessageSource();
        mailMessages.setBasename("mail/messages");
        mailMessages.setDefaultEncoding("UTF-8");
        mailMessages.setFallbackToSystemLocale(false);
        mailMessages.setDefaultLocale(Locale.forLanguageTag("es"));
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
            final ZoneId displayZone) {
        this.mailSender = mailSender;
        this.reservationTokenDao = reservationTokenDao;
        this.packDao = packDao;
        this.clientService = clientService;
        this.mailFrom = mailFrom;
        this.mailFromName = mailFromName;
        this.displayZone = displayZone;
    }

    @Async
    @Override
    public void sendReservationRequestToCommerce(final Reservation reservation, final String commerceEmail,
            final String baseUrl, final String pickupDateStr, final Locale locale) {
        final String acceptToken = UUID.randomUUID().toString();
        final String rejectToken = UUID.randomUUID().toString();
        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime expiresAt = now.plusHours(48);

        reservationTokenDao.create(acceptToken, reservation.getId(), ReservationToken.Action.ACCEPT, now, expiresAt);
        reservationTokenDao.create(rejectToken, reservation.getId(), ReservationToken.Action.REJECT, now, expiresAt);

        final String normalizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        final String acceptUrl = normalizedBase + "/reservations/accept?token=" + acceptToken;
        final String rejectUrl = normalizedBase + "/reservations/reject?token=" + rejectToken;

        final PackMailInfo packMailInfo = getPackMailInfo(reservation, locale);
        final String clientName = resolveClientName(reservation.getCustomerId(), locale);
        final String subject = mailMessages.getMessage("mail.subject.reservationRequest",
                new Object[]{reservation.getId(), clientName}, locale);
        final String html = buildCommerceHtml(reservation, packMailInfo.packLabel(), acceptUrl, rejectUrl,
                pickupDateStr, locale);

        sendHtmlMail(commerceEmail, subject, html, "Could not send reservation mail");
    }

    @Async
    @Override
    public void sendReservationCodeToClient(final Reservation reservation, final String clientEmail,
            final String pickupDateStr, final Locale locale) {
        final PackMailInfo packMailInfo = getPackMailInfo(reservation, locale);
        final String subject = mailMessages.getMessage("mail.subject.clientPickupCode",
                new Object[]{packMailInfo.localName()}, locale);
        final String html = buildClientHtml(reservation, packMailInfo.packLabel(), pickupDateStr, locale);
        sendHtmlMail(clientEmail, subject, html, "Could not send client pickup code mail");
    }

    @Async
    @Override
    public void sendAuctionWinnerCodeToClient(final Reservation reservation, final String clientEmail,
            final String pickupDateStr, final Locale locale) {
        final PackMailInfo packMailInfo = getPackMailInfo(reservation, locale);
        final String subject = mailMessages.getMessage("mail.subject.auctionWinnerClient",
                new Object[]{packMailInfo.localName()}, locale);
        final String html = buildAuctionWinnerHtml(reservation, packMailInfo.packLabel(), pickupDateStr, false,
                null, locale);
        sendHtmlMail(clientEmail, subject, html, "Could not send auction winner pickup code mail to client");
    }

    @Async
    @Override
    public void sendAuctionWinnerCodeToCommerce(final Reservation reservation, final String commerceEmail,
            final String pickupDateStr, final Locale locale) {
        final PackMailInfo packMailInfo = getPackMailInfo(reservation, locale);
        final String subject = mailMessages.getMessage("mail.subject.auctionWinnerCommerce",
                new Object[]{packMailInfo.localName()}, locale);
        final String winnerName = resolveClientName(reservation.getCustomerId(), locale);
        final String html = buildAuctionWinnerHtml(reservation, packMailInfo.packLabel(), pickupDateStr, true,
                winnerName, locale);
        sendHtmlMail(commerceEmail, subject, html, "Could not send auction winner pickup code mail to commerce");
    }

    @Async
    @Override
    public void sendReservationRejectedToClient(final Reservation reservation, final String clientEmail,
            final Locale locale) {
        final PackMailInfo packMailInfo = getPackMailInfo(reservation, locale);
        final String subject = mailMessages.getMessage("mail.subject.clientRejected",
                new Object[]{packMailInfo.localName()}, locale);
        final String html = buildClientRejectedHtml(reservation, packMailInfo.packLabel(), locale);
        sendHtmlMail(clientEmail, subject, html, "Could not send rejection mail");
    }

    private String buildClientHtml(final Reservation reservation, final String packLabel, final String pickupDateStr,
            final Locale locale) {
        final String reservationDateStr = reservation.getReservationDate() != null
                ? formatToLocal(reservation.getReservationDate())
                : "-";
        final String priceStr = reservation.getFinalPrice() != null ? reservation.getFinalPrice().toString() : "-";
        final String code = reservation.getPickupCode() == null ? "" : reservation.getPickupCode();

        final Context context = new Context(locale);
        context.setVariable("code", code);
        context.setVariable("reservationId", reservation.getId());
        context.setVariable("packLabel", packLabel);
        context.setVariable("reservationDateStr", reservationDateStr);
        context.setVariable("pickupDateStr", pickupDateStr);
        context.setVariable("priceStr", priceStr);

        return templateEngine.process("client-pickup-code", context);
    }

    private String buildAuctionWinnerHtml(final Reservation reservation, final String packLabel,
            final String pickupDateStr, final boolean forCommerce, final String winnerName, final Locale locale) {
        final String reservationDateStr = reservation.getReservationDate() != null
                ? formatToLocal(reservation.getReservationDate())
                : "-";
        final String priceStr = reservation.getFinalPrice() != null ? reservation.getFinalPrice().toString() : "-";
        final String code = reservation.getPickupCode() == null ? "" : reservation.getPickupCode();

        final Context context = new Context(locale);
        context.setVariable("code", code);
        context.setVariable("reservationId", reservation.getId());
        context.setVariable("packLabel", packLabel);
        context.setVariable("reservationDateStr", reservationDateStr);
        context.setVariable("pickupDateStr", pickupDateStr);
        context.setVariable("priceStr", priceStr);
        context.setVariable("showCode", !forCommerce);
        context.setVariable("winnerName", forCommerce ? winnerName : null);

        return templateEngine.process("auction-winner-pickup-code", context);
    }

    private String buildClientRejectedHtml(final Reservation reservation, final String packLabel,
            final Locale locale) {
        final String reservationDateStr = reservation.getReservationDate() != null
                ? formatToLocal(reservation.getReservationDate())
                : "-";

        final Context context = new Context(locale);
        context.setVariable("reservationId", reservation.getId());
        context.setVariable("packLabel", packLabel);
        context.setVariable("reservationDateStr", reservationDateStr);

        return templateEngine.process("client-reservation-rejected", context);
    }

    private String buildCommerceHtml(final Reservation reservation, final String packTitle, final String acceptUrl,
            final String rejectUrl, final String pickupDateStr, final Locale locale) {
        final String dateStr = reservation.getReservationDate() != null
                ? formatToLocal(reservation.getReservationDate())
                : "-";
        final String priceStr = reservation.getFinalPrice() != null ? reservation.getFinalPrice().toString() : "-";

        final Context context = new Context(locale);
        context.setVariable("packTitle", packTitle);
        context.setVariable("dateStr", dateStr);
        context.setVariable("priceStr", priceStr);
        context.setVariable("pickupDateStr", pickupDateStr);
        context.setVariable("acceptUrl", acceptUrl);
        context.setVariable("rejectUrl", rejectUrl);

        return templateEngine.process("commerce-reservation", context);
    }

    private PackMailInfo getPackMailInfo(final Reservation reservation, final Locale locale) {
        final Pack pack = packDao.findById(reservation.getPackId()).orElse(null);
        final String fallbackName = mailMessages.getMessage("mail.label.packFallback",
                new Object[]{reservation.getPackId()}, locale);
        final String localName = pack != null ? pack.getTitle() : fallbackName;
        final String packLabel = pack != null
                ? (pack.getTitle() + " (#" + pack.getId() + ")")
                : fallbackName;
        return new PackMailInfo(localName, packLabel);
    }

    private String resolveClientName(final Long customerId, final Locale locale) {
        final String fallbackClientName = mailMessages.getMessage("mail.label.clientFallback", null, locale);
        if (customerId == null) {
            return fallbackClientName;
        }

        final Optional<Client> maybeClient = clientService.findByUserId(customerId);
        if (maybeClient.isEmpty()) {
            return fallbackClientName;
        }

        final Client client = maybeClient.get();
        final String fullName = (client.getName() == null ? "" : client.getName())
                + (client.getLastName() == null ? "" : (" " + client.getLastName()));
        return fullName.trim().isEmpty() ? fallbackClientName : fullName.trim();
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
        if (dt == null) {
            return "-";
        }
        try {
            final ZonedDateTime zonedDateTime = ZonedDateTime.of(dt, ZoneOffset.UTC).withZoneSameInstant(displayZone);
            return zonedDateTime.format(MAIL_DATE_FORMATTER);
        } catch (final Exception e) {
            try {
                return dt.format(MAIL_DATE_FORMATTER);
            } catch (final Exception ex) {
                return "-";
            }
        }
    }
}