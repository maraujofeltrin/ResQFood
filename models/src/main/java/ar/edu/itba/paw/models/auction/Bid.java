package ar.edu.itba.paw.models.auction;

import java.time.LocalDateTime;

public class Bid {

    private final Long id;
    private final Long auctionId;
    private final Long clientId;
    private final Double amount;
    private final LocalDateTime timestamp;

    public Bid(Long id, Long auctionId, Long clientId, Double amount, LocalDateTime timestamp) {
        this.id = id;
        this.auctionId = auctionId;
        this.clientId = clientId;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public Long getAuctionId() {
        return auctionId;
    }

    public Long getClientId() {
        return clientId;
    }

    public Double getAmount() {
        return amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Bid [id=" + id + ", auctionId=" + auctionId + ", clientId=" + clientId
                + ", amount=" + amount + ", timestamp=" + timestamp + "]";
    }
}
