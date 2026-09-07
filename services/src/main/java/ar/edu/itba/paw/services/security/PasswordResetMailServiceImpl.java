package ar.edu.itba.paw.services.security;

import ar.edu.itba.paw.services.mail.MailSenderSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.util.Locale;

@Service
public class PasswordResetMailServiceImpl extends MailSenderSupport implements PasswordResetMailService {

    @Autowired
    public PasswordResetMailServiceImpl(final JavaMailSender mailSender,
            @Value("${mail.username}") final String mailFrom,
            @Value("${mail.from-name:ResQFood}") final String mailFromName) {
        super(mailSender, mailFrom, mailFromName);
    }

    @Async
    @Override
    public void sendPasswordResetMail(final String toEmail, final String resetUrl, final Locale locale) {
        final Context context = new Context(locale);
        context.setVariable("resetUrl", resetUrl);
        final String html = processTemplate("password-reset", context);
        final String subject = resolveSubject("mail.subject.passwordReset", null, locale);
        sendHtmlMail(toEmail, subject, html, "Could not send password reset mail");
    }
}