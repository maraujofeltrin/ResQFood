package ar.edu.itba.paw.models.auction;

import ar.edu.itba.paw.models.pack.Pack;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class Auction {

    public enum Status {
        ACTIVE,
        FINISHED,
        CANCELLED
    }

    private final Long id;
    private final Pack pack;
    private final Double initialPrice;
    private Double currentBid;
    private Long currentBidderId;
    private final LocalDateTime endTime;
    private Status status;
    private final LocalDateTime createdAt;

    public Auction(Long id, Pack pack, Double initialPrice, Double currentBid, Long currentBidderId,
                   LocalDateTime endTime, Status status, LocalDateTime createdAt) {
        this.id = id;
        this.pack = pack;
        this.initialPrice = initialPrice;
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

    @Override
    public String toString() {
        return "Auction [id=" + id + ", packId=" + (pack != null ? pack.getId() : null)
                + ", initialPrice=" + initialPrice + ", currentBid=" + currentBid
                + ", currentBidderId=" + currentBidderId + ", endTime=" + endTime
                + ", status=" + status + ", createdAt=" + createdAt + "]";
    }
}
