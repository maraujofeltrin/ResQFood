package ar.edu.itba.paw.webapp.form;

import java.util.List;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

import ar.edu.itba.paw.models.pack.PackTag;
import ar.edu.itba.paw.webapp.validation.constraints.LessOrEqual;
/**
 * Unified form for creating both Packs (direct sale) and Auctions.
 * <p>
 * When {@code isAuction == false} (default): {@code finalPrice} and {@code stock} are required
 * (see {@link ar.edu.itba.paw.webapp.validation.CreateOfferFormValidator}).
 * <p>
 * When {@code isAuction == true}: {@code initialPrice}, {@code minBidIncrement}, {@code endDate} and {@code endTime} are
 * required (see {@link ar.edu.itba.paw.webapp.validation.CreateOfferFormValidator}).
 */
@LessOrEqual.List({
    @LessOrEqual(first = "finalPrice", second = "originalPrice", message = "commerce.createPack.validation.finalPrice.exceedsOriginal"),
    @LessOrEqual(first = "initialPrice", second = "originalPrice", message = "commerce.createAuction.validation.initialPrice.exceedsOriginal")
})
public class CreateOfferForm {

    // ── Common Fields ──────────────────────────────────────────

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

    private MultipartFile image;
    
    /** If an image was previously uploaded and persisted temporarily, this holds its id. */
    private Long existingImageId;

    // ── Mode Toggle ────────────────────────────────────────────

    private boolean isAuction;

    // ── Pack-only Fields (validated when isAuction == false) ──

    private Double finalPrice;

    private Integer stock;

    // ── Auction-only Fields (validated when isAuction == true) ─

    private Double initialPrice;

    private Double minBidIncrement;

    private String endDate;

    private String endTime;

    // ── Getters and Setters ────────────────────────────────────

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

    public MultipartFile getImage() {
        return image;
    }

    public void setImage(MultipartFile image) {
        this.image = image;
    }

    public Long getExistingImageId() {
        return existingImageId;
    }

    public void setExistingImageId(Long existingImageId) {
        this.existingImageId = existingImageId;
    }

    public boolean getIsAuction() {
        return isAuction;
    }

    public void setIsAuction(boolean isAuction) {
        this.isAuction = isAuction;
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

    public Double getInitialPrice() {
        return initialPrice;
    }

    public void setInitialPrice(Double initialPrice) {
        this.initialPrice = initialPrice;
    }

    public Double getMinBidIncrement() {
        return minBidIncrement;
    }

    public void setMinBidIncrement(Double minBidIncrement) {
        this.minBidIncrement = minBidIncrement;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
