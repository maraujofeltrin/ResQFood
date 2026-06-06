package ar.edu.itba.paw.services.auction;

import ar.edu.itba.paw.services.mail.MailSenderSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.util.Locale;

@Service
public class AuctionMailServiceImpl extends MailSenderSupport implements AuctionMailService {

    @Autowired
    public AuctionMailServiceImpl(final JavaMailSender mailSender,
            @Value("${mail.username}") final String mailFrom,
            @Value("${mail.from-name:ResQFood}") final String mailFromName) {
        super(mailSender, mailFrom, mailFromName);
    }

    @Async
    @Override
    public void sendAuctionOutbidToClient(final String clientEmail, final String packTitle,
            final String commerceName, final double newAmount, final Locale locale) {
        final String fallback = resolveSubject("mail.label.packFallback", new Object[]{""}, locale);
        final String subject = resolveSubject("mail.subject.auctionOutbid",
                new Object[]{packTitle != null ? packTitle : fallback}, locale);
        final Context context = new Context(locale);
        context.setVariable("packTitle", packTitle);
        context.setVariable("commerceName", commerceName);
        context.setVariable("newAmount", newAmount);
        final String html = processTemplate("auction-outbid", context);
        sendHtmlMail(clientEmail, subject, html, "Could not send auction outbid mail");
    }

    @Async
    @Override
    public void sendAuctionFinishedLostToClient(final String clientEmail, final String packTitle,
            final String commerceName, final Locale locale) {
        final String subject = resolveSubject("mail.subject.auctionFinishedLost",
                new Object[]{packTitle != null ? packTitle : ""}, locale);
        final Context context = new Context(locale);
        context.setVariable("packTitle", packTitle);
        context.setVariable("commerceName", commerceName);
        final String html = processTemplate("auction-finished-lost", context);
        sendHtmlMail(clientEmail, subject, html, "Could not send auction finished lost mail");
    }
}
