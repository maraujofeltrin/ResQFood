package ar.edu.itba.paw.models.pack;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "packs")
public class Pack {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "packs_id_seq")
    @SequenceGenerator(sequenceName = "packs_id_seq", name = "packs_id_seq", allocationSize = 1)
    private Long id;
    @Column(name = "commerce_id", nullable = false)
    private Long commerceId;
    @Column(name = "title", nullable = false)
    private String title;
    @Column(name = "description", nullable = false)
    private String description;
    @Column(name = "original_price", nullable = false)
    private Double originalPrice;
    @Column(name = "final_price", nullable = false)
    private Double finalPrice;
    @Column(name = "stock", nullable = false)
    private Integer stock;
    @Column(name = "active", nullable = false)
    private Boolean active;
    @Column(name = "deleted", nullable = false)
    private Boolean deleted;
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "pack_tags", joinColumns = @JoinColumn(name = "pack_id"))
    @Column(name = "tag", nullable = false)
    @Enumerated(EnumType.STRING)
    private List<PackTag> tags;
    @Column(name = "image_id")
    private Long imageId;

    protected Pack() {
    }

    public Pack(Long id, Long commerceId, String title, String description, Double originalPrice, Double finalPrice, Integer stock, Boolean active, List<PackTag> tags) {
        this(id, commerceId, title, description, originalPrice, finalPrice, stock, active, false, tags, null);
    }

    public Pack(Long id, Long commerceId, String title, String description, Double originalPrice, Double finalPrice, Integer stock, Boolean active, Boolean deleted, List<PackTag> tags, Long imageId) {
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
        this.imageId = imageId;
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

    public Long getImageId() {
        return imageId;
    }

    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }

    @Override
    public String toString() {
        return "Pack [id=" + id + ", commerceId=" + commerceId + ", title=" + title + ", description=" + description + ", originalPrice=" + originalPrice + ", finalPrice=" + finalPrice + ", stock=" + stock + ", active=" + active + ", tags=" + tags + ", hasImage=" + (imageId != null) + "]";
    }
}
