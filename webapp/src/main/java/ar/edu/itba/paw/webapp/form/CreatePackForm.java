package ar.edu.itba.paw.webapp.form;

import java.util.List;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;

import org.springframework.web.multipart.MultipartFile;

import ar.edu.itba.paw.models.Commerce.Category;
import ar.edu.itba.paw.models.PackTag;

public class CreatePackForm {

    // Identidad del Usuario
    @NotEmpty(message = "{commerce.createPack.validation.email.notEmpty}")
    @Email(message = "{commerce.createPack.validation.email.valid}")
    private String email;

    @NotEmpty(message = "{commerce.createPack.validation.name.notEmpty}")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ].*", message = "{commerce.createPack.validation.name.pattern}")
    private String name;

    // Datos del Local
    @NotEmpty(message = "{commerce.createPack.validation.commercialName.notEmpty}")
    private String commercialName;

    private Category category;

    @NotEmpty(message = "{commerce.createPack.validation.street.notEmpty}")
    private String street;

    @NotNull(message = "{commerce.createPack.validation.streetNumber.notNull}")
    @Min(value = 1, message = "{commerce.createPack.validation.streetNumber.min}")
    @Max(value = 99999, message = "{commerce.createPack.validation.streetNumber.max}")
    private Integer streetNumber;

    @NotEmpty(message = "{commerce.createPack.validation.postalCode.notEmpty}")
    @Pattern(regexp = "^(?i)([cC]?[0-9]{1,5})$", message = "{commerce.createPack.validation.postalCode.pattern}")
    private String postalCode;

    @NotEmpty(message = "{commerce.createPack.validation.city.notEmpty}")
    private String city;

    @NotEmpty(message = "{commerce.createPack.validation.province.notEmpty}")
    private String province;

    @NotEmpty(message = "{commerce.createPack.validation.openingTime.notEmpty}")
    private String openingTime;

    @NotEmpty(message = "{commerce.createPack.validation.closingTime.notEmpty}")
    private String closingTime;

    // Detalles del Pack
    @NotEmpty(message = "{commerce.createPack.validation.title.notEmpty}")
    private String title;

    @NotEmpty(message = "{commerce.createPack.validation.description.notEmpty}")
    private String description;

    private List<PackTag> tags;

    @NotNull(message = "{commerce.createPack.validation.originalPrice.notNull}")
    @Positive(message = "{commerce.createPack.validation.originalPrice.positive}")
    @Digits(integer = 7, fraction = 2, message = "{commerce.createPack.validation.originalPrice.digits}")
    private Double originalPrice;

    @NotNull(message = "{commerce.createPack.validation.finalPrice.notNull}")
    @Positive(message = "{commerce.createPack.validation.finalPrice.positive}")
    @Digits(integer = 7, fraction = 2, message = "{commerce.createPack.validation.finalPrice.digits}")
    private Double finalPrice;

    @NotNull(message = "{commerce.createPack.validation.stock.notNull}")
    @Positive(message = "{commerce.createPack.validation.stock.positive}")
    @Max(value = 999, message = "{commerce.createPack.validation.stock.max}")
    private Integer stock;

    private MultipartFile image;

    // Getters and Setters

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCommercialName() {
        return commercialName;
    }

    public void setCommercialName(String commercialName) {
        this.commercialName = commercialName;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public Integer getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(Integer streetNumber) {
        this.streetNumber = streetNumber;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(String openingTime) {
        this.openingTime = openingTime;
    }

    public String getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(String closingTime) {
        this.closingTime = closingTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<PackTag> getTags() {
        return tags;
    }

    public void setTags(List<PackTag> tags) {
        this.tags = tags;
    }

    public Double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(Double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public Double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(Double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public MultipartFile getImage() {
        return image;
    }

    public void setImage(MultipartFile image) {
        this.image = image;
    }
}
