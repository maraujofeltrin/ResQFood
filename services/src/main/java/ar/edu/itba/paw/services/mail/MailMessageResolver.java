package ar.edu.itba.paw.services.mail;

import org.springframework.context.support.ResourceBundleMessageSource;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.messageresolver.AbstractMessageResolver;

import java.util.Locale;

public class MailMessageResolver extends AbstractMessageResolver {

    private final ResourceBundleMessageSource messageSource;

    public MailMessageResolver() {
        final ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("mail/messages");
        source.setDefaultEncoding("UTF-8");
        source.setFallbackToSystemLocale(false);
        source.setDefaultLocale(Locale.forLanguageTag("es"));
        this.messageSource = source;
    }

    @Override
    public String resolveMessage(final ITemplateContext context, final Class<?> origin, final String key,
            final Object[] messageParameters) {
        return messageSource.getMessage(key, messageParameters, context.getLocale());
    }

    @Override
    public String createAbsentMessageRepresentation(final ITemplateContext context, final Class<?> origin,
            final String key, final Object[] messageParameters) {
        return "??" + key + "??";
    }
}