package ar.edu.itba.paw.webapp.form;

import org.springframework.web.multipart.MultipartFile;

public class ProfileAccountForm {

    private MultipartFile photo;
    private String street;
    private String streetNumber;
    private String city;
    private String province;
    private String postalCode;
    private String category;
    private String openingTime;
    private String closingTime;

    public MultipartFile getPhoto() {
        return photo;
    }

    public void setPhoto(final MultipartFile photo) {
        this.photo = photo;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(final String street) {
        this.street = street;
    }

    public String getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(final String streetNumber) {
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

    public String getCategory() {
        return category;
    }

    public void setCategory(final String category) {
        this.category = category;
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
