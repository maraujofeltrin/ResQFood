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
public class EmailVerificationMailServiceImpl implements EmailVerificationMailService {

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
    public EmailVerificationMailServiceImpl(final JavaMailSender mailSender,
            @Value("${mail.username}") final String mailFrom,
            @Value("${mail.from-name:ResQFood}") final String mailFromName) {
        this.mailSender = mailSender;
        this.mailFrom = mailFrom;
        this.mailFromName = mailFromName;
    }

    @Async("mailTaskExecutor")
    @Override
    public void sendVerificationMail(final String toEmail, final String verificationUrl) {
        final Context context = new Context();
        context.setVariable("verificationUrl", verificationUrl);
        final String html = templateEngine.process("email-verification", context);

        try {
            final MimeMessage message = mailSender.createMimeMessage();
            final MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom, mailFromName);
            helper.setTo(toEmail);
            helper.setSubject("Verificá tu cuenta en ResQFood");
            helper.setText(html, true);
            mailSender.send(message);
        } catch (final MessagingException | UnsupportedEncodingException e) {
            throw new IllegalStateException("Could not send email verification mail", e);
        }
    }
}
