package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.models.user.Commerce;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class CommerceProfileForm {

    @NotBlank(message = "{register.validation.commercialName.notEmpty}")
    @Size(max = 255, message = "{register.validation.commercialName.size}")
    private String commercialName;

    @NotNull(message = "{register.validation.category.notNull}")
    private Commerce.Category category;

    @NotBlank(message = "{register.validation.street.notEmpty}")
    @Size(max = 255, message = "{register.validation.street.size}")
    private String street;

    @NotNull(message = "{register.validation.streetNumber.notNull}")
    @Min(value = 1, message = "{register.validation.streetNumber.min}")
    @Max(value = 99999, message = "{register.validation.streetNumber.max}")
    private Integer streetNumber;

    @NotBlank(message = "{register.validation.city.notEmpty}")
    @Size(max = 255, message = "{register.validation.city.size}")
    private String city;

    @NotBlank(message = "{register.validation.province.notEmpty}")
    @Size(max = 255, message = "{register.validation.province.size}")
    private String province;

    @NotBlank(message = "{register.validation.postalCode.notEmpty}")
    @Size(max = 50, message = "{register.validation.postalCode.size}")
    @Pattern(regexp = "^(?i)([cC]?[0-9]{1,5})$", message = "{register.validation.postalCode.pattern}")
    private String postalCode;

    @NotBlank(message = "{register.validation.openingTime.notEmpty}")
    @Size(max = 50, message = "{register.validation.openingTime.size}")
    private String openingTime;

    @NotBlank(message = "{register.validation.closingTime.notEmpty}")
    @Size(max = 50, message = "{register.validation.closingTime.size}")
    private String closingTime;

    public String getCommercialName() {
        return commercialName;
    }

    public void setCommercialName(final String commercialName) {
        this.commercialName = commercialName;
    }

    public Commerce.Category getCategory() {
        return category;
    }

    public void setCategory(final Commerce.Category category) {
        this.category = category;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(final String street) {
        this.street = street;
    }

    public Integer getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(final Integer streetNumber) {
        this.streetNumber = streetNumber;
    }

    public String getCity() {
        return city;
    }

    public void setCity(final String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(final String province) {
        this.province = province;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(final String postalCode) {
        this.postalCode = postalCode;
    }

    public String getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(final String openingTime) {
        this.openingTime = openingTime;
    }

    public String getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(final String closingTime) {
        this.closingTime = closingTime;
    }

}
