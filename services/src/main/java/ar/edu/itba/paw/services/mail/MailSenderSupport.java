package ar.edu.itba.paw.services.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

/**
 * Shared infrastructure for all mail-sending services.
 * Centralises Thymeleaf template engine setup, i18n message resolution and
 * the low-level SMTP send so that concrete mail services only need to build
 * a {@link Context} and choose a template name.
 */
public abstract class MailSenderSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailSenderSupport.class);

    private static final TemplateEngine TEMPLATE_ENGINE;
    private static final ResourceBundleMessageSource MAIL_MESSAGES;

    static {
        final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("mail/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding("UTF-8");

        final MailMessageResolver messageResolver = new MailMessageResolver();

        TEMPLATE_ENGINE = new TemplateEngine();
        TEMPLATE_ENGINE.setTemplateResolver(templateResolver);
        TEMPLATE_ENGINE.setMessageResolver(messageResolver);

        MAIL_MESSAGES = new ResourceBundleMessageSource();
        MAIL_MESSAGES.setBasename("mail/messages");
        MAIL_MESSAGES.setDefaultEncoding("UTF-8");
        MAIL_MESSAGES.setFallbackToSystemLocale(false);
        MAIL_MESSAGES.setDefaultLocale(Locale.forLanguageTag("es"));
    }

    private final JavaMailSender mailSender;
    private final String mailFrom;
    private final String mailFromName;

    protected MailSenderSupport(final JavaMailSender mailSender,
            final String mailFrom, final String mailFromName) {
        this.mailSender = mailSender;
        this.mailFrom = mailFrom;
        this.mailFromName = mailFromName;
    }

    /**
     * Resolves a subject line from the mail message bundle.
     */
    protected String resolveSubject(final String key, final Object[] args, final Locale locale) {
        return MAIL_MESSAGES.getMessage(key, args, locale);
    }

    /**
     * Processes a Thymeleaf template and returns the rendered HTML.
     */
    protected String processTemplate(final String templateName, final Context context) {
        return TEMPLATE_ENGINE.process(templateName, context);
    }

    /**
     * Sends an HTML email. Throws {@link ar.edu.itba.paw.models.notification.MailDeliveryException}
     * on failure so that callers can decide whether to propagate or swallow it.
     */
    protected void sendHtmlMail(final String toEmail, final String subject,
            final String html, final String errorContext) {
        try {
            final MimeMessage message = mailSender.createMimeMessage();
            final MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom, mailFromName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (final MessagingException | UnsupportedEncodingException e) {
            LOGGER.error("Failed to send mail: {}", errorContext, e);
            throw new ar.edu.itba.paw.models.notification.MailDeliveryException(errorContext, e);
        }
    }
}
