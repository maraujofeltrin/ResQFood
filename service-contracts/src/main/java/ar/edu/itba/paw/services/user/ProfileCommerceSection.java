package ar.edu.itba.paw.services.user;

import java.util.Objects;

/** Datos del comercio mostrados y editables en el perfil (usuario rol COMMERCE). */
public final class ProfileCommerceSection {

    private final String commercialName;
    private final String category;
    private final String street;
    private final Integer streetNumber;
    private final String city;
    private final String province;
    private final String postalCode;
    private final String openingTime;
    private final String closingTime;

    public ProfileCommerceSection(
            final String commercialName,
            final String category,
            final String street,
            final Integer streetNumber,
            final String city,
            final String province,
            final String postalCode,
            final String openingTime,
            final String closingTime) {
        this.commercialName = Objects.requireNonNull(commercialName);
        this.category = category;
        this.street = street;
        this.streetNumber = streetNumber;
        this.city = city;
        this.province = province;
        this.postalCode = postalCode;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
    }

    public String getCommercialName() {
        return commercialName;
    }

    public String getCategory() {
        return category;
    }

    public String getStreet() {
        return street;
    }

    public Integer getStreetNumber() {
        return streetNumber;
    }

    public String getCity() {
        return city;
    }

    public String getProvince() {
        return province;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getOpeningTime() {
        return openingTime;
    }

    public String getClosingTime() {
        return closingTime;
    }
}
