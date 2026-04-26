package ar.edu.itba.paw.services.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.context.support.ResourceBundleMessageSource;
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
import java.util.Locale;

@Service
public class EmailVerificationMailServiceImpl implements EmailVerificationMailService {

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
    private final String mailFrom;
    private final String mailFromName;

    @Autowired
    public EmailVerificationMailServiceImpl(final JavaMailSender mailSender,
            @Value("${mail.username}") final String mailFrom,
            @Value("${mail.from-name:ResQFood}") final String mailFromName) {
        this.mailSender = mailSender;
        this.mailFrom = mailFrom;
        this.mailFromName = mailFromName;
    }

    @Async
    @Override
    public void sendVerificationMail(final String toEmail, final String verificationUrl, final Locale locale) {
        final Context context = new Context(locale);
        context.setVariable("verificationUrl", verificationUrl);
        final String html = templateEngine.process("email-verification", context);
        final String subject = mailMessages.getMessage("mail.subject.emailVerification", null, locale);

        try {
            final MimeMessage message = mailSender.createMimeMessage();
            final MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom, mailFromName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (final MessagingException | UnsupportedEncodingException e) {
            throw new IllegalStateException("Could not send email verification mail", e);
        }
    }
}
