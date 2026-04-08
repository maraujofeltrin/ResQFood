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
    @NotEmpty(message = "El email no puede estar vacío")
    @Email(message = "El email debe ser válido")
    private String email;

    @NotEmpty(message = "El nombre del titular no puede estar vacío")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ].*", message = "El nombre debe comenzar con una letra")
    private String name;

    // Datos del Local
    @NotEmpty(message = "El nombre comercial no puede estar vacío")
    private String commercialName;

    private Category category;

    @NotEmpty(message = "La calle no puede estar vacía")
    private String street;

    @NotNull(message = "El número no puede estar vacío")
    @Min(value = 1, message = "El número debe ser mayor a 0")
    @Max(value = 99999, message = "El número no puede tener más de 5 cifras")
    private Integer streetNumber;

    @NotEmpty(message = "El código postal no puede estar vacío")
    @Pattern(regexp = "^(?i)([cC]?[0-9]{1,5})$", message = "El código postal debe tener hasta 5 números o comenzar con C")
    private String postalCode;

    @NotEmpty(message = "La ciudad no puede estar vacía")
    private String city;

    @NotEmpty(message = "La provincia no puede estar vacía")
    private String province;

    @NotEmpty(message = "El horario de apertura no puede estar vacío")
    private String openingTime;

    @NotEmpty(message = "El horario de cierre no puede estar vacío")
    private String closingTime;

    // Detalles del Pack
    @NotEmpty(message = "El título no puede estar vacío")
    private String title;

    @NotEmpty(message = "La descripción no puede estar vacía")
    private String description;

    private List<PackTag> tags;

    @NotNull(message = "El precio original no puede estar vacío")
    @Positive(message = "El precio debe ser mayor a 0")
    @Digits(integer = 7, fraction = 2, message = "El precio máximo debe tener 7 cifras como máximo")
    private Double originalPrice;

    @NotNull(message = "El precio de venta no puede estar vacío")
    @Positive(message = "El precio debe ser mayor a 0")
    @Digits(integer = 7, fraction = 2, message = "El precio máximo debe tener 7 cifras como máximo")
    private Double finalPrice;

    @NotNull(message = "La cantidad no puede estar vacía")
    @Positive(message = "La cantidad debe ser mayor a 0")
    @Max(value = 999, message = "La cantidad a publicar debe tener 3 cifras como máximo")
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
