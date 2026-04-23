package ar.edu.itba.paw.models.pack;

import java.util.List;

public class Pack {
    private final Long id;
    private final Long commerceId;
    private String title;
    private String description;
    private Double originalPrice;
    private Double finalPrice;
    private Integer stock;
    private Boolean active;
    private Boolean deleted;
    private List<PackTag> tags;
    private byte[] imageData;
    private String imageContentType;

    public Pack(Long id, Long commerceId, String title, String description, Double originalPrice, Double finalPrice, Integer stock, Boolean active, List<PackTag> tags) {
        this(id, commerceId, title, description, originalPrice, finalPrice, stock, active, false, tags, null, null);
    }

    public Pack(Long id, Long commerceId, String title, String description, Double originalPrice, Double finalPrice, Integer stock, Boolean active, Boolean deleted, List<PackTag> tags, byte[] imageData, String imageContentType) {
        this.id = id;
        this.commerceId = commerceId;
        this.title = title;
        this.description = description;
        this.originalPrice = originalPrice;
        this.finalPrice = finalPrice;
        this.stock = stock;
        this.active = active;
        this.deleted = deleted;
        this.tags = tags;
        this.imageData = imageData;
        this.imageContentType = imageContentType;
    }

    public Long getId() {
        return id;
    }

    public Long getCommerceId() {
        return commerceId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Double getOriginalPrice() {
        return originalPrice;
    }

    public Double getFinalPrice() {
        return finalPrice;
    }

    public Integer getStock() {
        return stock;
    }

    public Boolean getActive() {
        return active;
    }

    public List<PackTag> getTags() {
        return tags;
    }

    public void setTags(List<PackTag> tags) {
        this.tags = tags;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setOriginalPrice(Double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public void setFinalPrice(Double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public String getImageContentType() {
        return imageContentType;
    }

    public void setImageContentType(String imageContentType) {
        this.imageContentType = imageContentType;
    }

    @Override
    public String toString() {
        return "Pack [id=" + id + ", commerceId=" + commerceId + ", title=" + title + ", description=" + description + ", originalPrice=" + originalPrice + ", finalPrice=" + finalPrice + ", stock=" + stock + ", active=" + active + ", tags=" + tags + ", hasImage=" + (imageData != null) + "]";
    }
}
