package ar.edu.itba.paw.models.pack;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class MunicipalityConverter implements AttributeConverter<Municipality, String> {

    @Override
    public String convertToDatabaseColumn(Municipality attribute) {
        return attribute == null ? null : attribute.getCityName();
    }

    @Override
    public Municipality convertToEntityAttribute(String dbData) {
        return dbData == null ? null : Municipality.fromCityName(dbData);
    }
}
