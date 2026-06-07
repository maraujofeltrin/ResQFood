package ar.edu.itba.paw.models.auction;

import ar.edu.itba.paw.models.pack.Pack;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "auctions")
public class Auction {

    public enum Status {
        ACTIVE,
        FINISHED,
        CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "auctions_id_seq")
    @SequenceGenerator(sequenceName = "auctions_id_seq", name = "auctions_id_seq", allocationSize = 1)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pack_id")
    private Pack pack;

    @Column(name = "initial_price", nullable = false)
    private Double initialPrice;

    @Column(name = "min_bid_increment", nullable = false)
    private Double minBidIncrement;

    @Column(name = "current_bid")
    private Double currentBid;

    @Column(name = "current_bidder_id")
    private Long currentBidderId;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Status status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * The maximum bid placed by the currently logged-in client on this auction.
     */
    @Transient
    private Double myMaxBid;

    protected Auction() {
        // Just for Hibernate
    }

    public Auction(Long id, Pack pack, Double initialPrice, Double minBidIncrement, Double currentBid, Long currentBidderId,
                   LocalDateTime endTime, Status status, LocalDateTime createdAt) {
        this.id = id;
        this.pack = pack;
        this.initialPrice = initialPrice;
        this.minBidIncrement = minBidIncrement;
        this.currentBid = currentBid;
        this.currentBidderId = currentBidderId;
        this.endTime = endTime;
        this.status = status;
        this.createdAt = createdAt;
    }

    /**
     * Returns {@code true} if the auction is in ACTIVE status and has not yet expired.
     */
    public boolean isActive() {
        return status == Status.ACTIVE && LocalDateTime.now(ZoneOffset.UTC).isBefore(endTime);
    }

    /**
     * Returns the current effective price: the highest bid if any, otherwise the initial price.
     */
    public Double getEffectivePrice() {
        return currentBid != null ? currentBid : initialPrice;
    }

    public Long getId() {
        return id;
    }

    public Pack getPack() {
        return pack;
    }

    public Double getInitialPrice() {
        return initialPrice;
    }

    /**
     * Minimum amount by which a new bid must exceed the current effective (standing) price.
     */
    public Double getMinBidIncrement() {
        return minBidIncrement;
    }

    public Double getCurrentBid() {
        return currentBid;
    }

    public void setCurrentBid(Double currentBid) {
        this.currentBid = currentBid;
    }

    public Long getCurrentBidderId() {
        return currentBidderId;
    }

    public void setCurrentBidderId(Long currentBidderId) {
        this.currentBidderId = currentBidderId;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Double getMyMaxBid() {
        return myMaxBid;
    }

    public void setMyMaxBid(final Double myMaxBid) {
        this.myMaxBid = myMaxBid;
    }

    @Override
    public String toString() {
        return "Auction [id=" + id + ", packId=" + (pack != null ? pack.getId() : null)
                + ", initialPrice=" + initialPrice + ", minBidIncrement=" + minBidIncrement
                + ", currentBid=" + currentBid
                + ", currentBidderId=" + currentBidderId + ", endTime=" + endTime
                + ", status=" + status + ", createdAt=" + createdAt + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Auction)) return false;
        Auction that = (Auction) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hashCode(id);
    }
}