package ar.edu.itba.paw.webapp.form;

import java.util.List;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

import ar.edu.itba.paw.models.Commerce.Category;
import ar.edu.itba.paw.models.PackTag;

public class CreatePackForm {


    // Detalles del Pack
    @NotBlank(message = "{commerce.createPack.validation.title.notEmpty}")
    @Size(max = 100)
    private String title;

    @NotBlank(message = "{commerce.createPack.validation.description.notEmpty}")
    @Size(max = 500)
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
