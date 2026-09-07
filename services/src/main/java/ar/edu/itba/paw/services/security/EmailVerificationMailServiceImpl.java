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
public class EmailVerificationMailServiceImpl extends MailSenderSupport implements EmailVerificationMailService {

    @Autowired
    public EmailVerificationMailServiceImpl(final JavaMailSender mailSender,
            @Value("${mail.username}") final String mailFrom,
            @Value("${mail.from-name:ResQFood}") final String mailFromName) {
        super(mailSender, mailFrom, mailFromName);
    }

    @Async
    @Override
    public void sendVerificationMail(final String toEmail, final String verificationUrl, final Locale locale) {
        final Context context = new Context(locale);
        context.setVariable("verificationUrl", verificationUrl);
        final String html = processTemplate("email-verification", context);
        final String subject = resolveSubject("mail.subject.emailVerification", null, locale);
        sendHtmlMail(toEmail, subject, html, "Could not send email verification mail");
    }
}
