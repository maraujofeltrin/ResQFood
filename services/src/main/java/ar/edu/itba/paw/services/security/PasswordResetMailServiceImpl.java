package ar.edu.itba.paw.services.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.context.support.ResourceBundleMessageSource;
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
public class PasswordResetMailServiceImpl implements PasswordResetMailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordResetMailServiceImpl.class);

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
    public PasswordResetMailServiceImpl(final JavaMailSender mailSender,
            @Value("${mail.username}") final String mailFrom,
            @Value("${mail.from-name:ResQFood}") final String mailFromName) {
        this.mailSender = mailSender;
        this.mailFrom = mailFrom;
        this.mailFromName = mailFromName;
    }

    @Async
    @Override
    public void sendPasswordResetMail(final String toEmail, final String resetUrl, final Locale locale) {
        final Context context = new Context(locale);
        context.setVariable("resetUrl", resetUrl);
        final String html = templateEngine.process("password-reset", context);
        final String subject = mailMessages.getMessage("mail.subject.passwordReset", null, locale);

        try {
            final MimeMessage message = mailSender.createMimeMessage();
            final MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom, mailFromName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (final MessagingException | UnsupportedEncodingException e) {
            LOGGER.error("Could not send password reset mail", e);
            throw new ar.edu.itba.paw.models.notification.MailDeliveryException("Could not send password reset mail", e);
        }
    }
}