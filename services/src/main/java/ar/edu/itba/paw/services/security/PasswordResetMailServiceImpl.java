package ar.edu.itba.paw.services.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;

@Service
public class PasswordResetMailServiceImpl implements PasswordResetMailService {

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

    @Async("mailTaskExecutor")
    @Override
    public void sendPasswordResetMail(final String toEmail, final String resetUrl) {
        final Context context = new Context();
        context.setVariable("resetUrl", resetUrl);
        final String html = templateEngine.process("password-reset", context);

        try {
            final MimeMessage message = mailSender.createMimeMessage();
            final MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom, mailFromName);
            helper.setTo(toEmail);
            helper.setSubject("Restablecé tu contraseña");
            helper.setText(html, true);
            mailSender.send(message);
        } catch (final MessagingException | UnsupportedEncodingException e) {
            throw new IllegalStateException("Could not send password reset mail to " + toEmail, e);
        }
    }
}