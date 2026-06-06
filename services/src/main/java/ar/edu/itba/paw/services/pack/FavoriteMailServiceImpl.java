package ar.edu.itba.paw.services.pack;

import ar.edu.itba.paw.services.mail.MailSenderSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.util.Locale;

@Service
public class FavoriteMailServiceImpl extends MailSenderSupport implements FavoriteMailService {

    @Autowired
    public FavoriteMailServiceImpl(final JavaMailSender mailSender,
            @Value("${mail.username}") final String mailFrom,
            @Value("${mail.from-name:ResQFood}") final String mailFromName) {
        super(mailSender, mailFrom, mailFromName);
    }

    @Async
    @Override
    public void sendFavoritePackRestockedToClient(final String clientEmail, final String packTitle,
            final String commerceName, final Locale locale) {
        final String subject = resolveSubject("mail.subject.favoritePackRestocked", null, locale);
        final Context context = new Context(locale);
        context.setVariable("packTitle", packTitle);
        context.setVariable("commerceName", commerceName);
        final String html = processTemplate("favorite-pack-restocked", context);
        sendHtmlMail(clientEmail, subject, html, "Could not send favorite pack restocked mail");
    }

    @Async
    @Override
    public void sendFavoriteCommerceNewPackToClient(final String clientEmail, final String packTitle,
            final String commerceName, final Locale locale) {
        final String subject = resolveSubject("mail.subject.favoriteCommerceNewPack",
                new Object[]{commerceName != null ? commerceName : ""}, locale);
        final Context context = new Context(locale);
        context.setVariable("packTitle", packTitle);
        context.setVariable("commerceName", commerceName);
        final String html = processTemplate("favorite-commerce-new-pack", context);
        sendHtmlMail(clientEmail, subject, html, "Could not send favorite commerce new pack mail");
    }
}
