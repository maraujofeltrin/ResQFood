package ar.edu.itba.paw.models.user;

import java.util.Locale;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class LocaleConverter implements AttributeConverter<Locale, String> {

    @Override
    public String convertToDatabaseColumn(Locale locale) {
        return locale == null ? null : locale.toLanguageTag();
    }

    @Override
    public Locale convertToEntityAttribute(String dbData) {
        return dbData == null ? null : Locale.forLanguageTag(dbData);
    }
}
