package ar.edu.itba.paw.models;

public class Pack {
    private final Long id;
    private String title;
    private String description;
    private Double originalPrice;
    private Double finalPrice;
    private Integer stock;
    private Boolean active;
    

    public Pack(Long id, String title, String description, Double originalPrice, Double finalPrice, Integer stock, Boolean active) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.originalPrice = originalPrice;
        this.finalPrice = finalPrice;
        this.stock = stock;
        this.active = active;
    }

    public Long getId() {
        return id;
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

    @Override
    public String toString() {
        return "Mysterybox [id=" + id + ", title=" + title + ", description=" + description + ", originalPrice=" + originalPrice + ", finalPrice=" + finalPrice + ", stock=" + stock + ", active=" + active + "]";
    }
}