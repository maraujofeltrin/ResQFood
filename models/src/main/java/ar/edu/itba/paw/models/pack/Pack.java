package ar.edu.itba.paw.models.pack;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.image.Image;
import ar.edu.itba.paw.models.user.Commerce;

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
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "commerce_id")
    private Commerce commerce;

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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private Image image;

    @OneToOne(mappedBy = "pack", fetch = FetchType.LAZY)
    private Auction auction;

    protected Pack() {
    }

    public Pack(final Long id, final Commerce commerce, final String title, final String description,
            final Double originalPrice, final Double finalPrice, final Integer stock, final Boolean active,
            final List<PackTag> tags) {
        this(id, commerce, title, description, originalPrice, finalPrice, stock, active, false, tags, null);
    }

    public Pack(final Long id, final Commerce commerce, final String title, final String description,
            final Double originalPrice, final Double finalPrice, final Integer stock, final Boolean active,
            final Boolean deleted, final List<PackTag> tags, final Image image) {
        this.id = id;
        this.commerce = commerce;
        this.title = title;
        this.description = description;
        this.originalPrice = originalPrice;
        this.finalPrice = finalPrice;
        this.stock = stock;
        this.active = active;
        this.deleted = deleted;
        this.tags = tags;
        this.image = image;
    }

    public Long getId() {
        return id;
    }

    public Commerce getCommerce() {
        return commerce;
    }

    public Long getCommerceId() {
        return commerce != null ? commerce.getUserId() : null;
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

    public void setTags(final List<PackTag> tags) {
        this.tags = tags;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setOriginalPrice(final Double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public void setFinalPrice(final Double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public void setStock(final Integer stock) {
        this.stock = stock;
    }

    public void setActive(final Boolean active) {
        this.active = active;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public Image getImage() {
        return image;
    }

    public Long getImageId() {
        return image != null ? image.getId() : null;
    }

    public void setImage(final Image image) {
        this.image = image;
    }

    public Auction getAuction() {
        return auction;
    }

    public void setAuction(final Auction auction) {
        this.auction = auction;
    }

    /**
     * Returns the price a buyer would pay right now.
     * If there is an active auction, it is the auction's current effective price;
     * otherwise it is the pack's direct-sale final price. Defaults to 0 when null.
     */
    public double getEffectiveSalePrice() {
        if (auction != null && auction.isActive()) {
            final Double eff = auction.getEffectivePrice();
            return eff != null ? eff : 0d;
        }
        return finalPrice != null ? finalPrice : 0d;
    }

    /**
     * Returns the maximum quantity a client may add to a single reservation.
     * Unlimited stock (null) is capped at 999 for form purposes.
     */
    public int getMaxReservableQuantity() {
        if (stock == null) {
            return 999;
        }
        if (stock < 1) {
            return 0;
        }
        return Math.min(stock, 999);
    }

    @Override
    public String toString() {
        return "Pack [id=" + id + ", commerceId=" + getCommerceId() + ", title=" + title + ", description="
                + description + ", originalPrice=" + originalPrice + ", finalPrice=" + finalPrice + ", stock="
                + stock + ", active=" + active + ", tags=" + tags + ", hasImage=" + (image != null) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pack)) return false;
        Pack that = (Pack) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hashCode(id);
    }
}