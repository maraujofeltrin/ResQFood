package ar.edu.itba.paw.models.auction;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bids")
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bids_id_seq")
    @SequenceGenerator(sequenceName = "bids_id_seq", name = "bids_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "auction_id", nullable = false)
    private Long auctionId;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    protected Bid() {
        // Just for Hibernate
    }

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
