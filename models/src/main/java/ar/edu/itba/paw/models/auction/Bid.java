package ar.edu.itba.paw.models.auction;

import ar.edu.itba.paw.models.user.Client;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "bids")
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bids_id_seq")
    @SequenceGenerator(sequenceName = "bids_id_seq", name = "bids_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auction_id")
    private Auction auction;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id")
    private Client client;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    protected Bid() {
        // Just for Hibernate
    }

    public Bid(final Long id, final Auction auction, final Client client, final Double amount,
            final LocalDateTime timestamp) {
        this.id = id;
        this.auction = auction;
        this.client = client;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public Auction getAuction() {
        return auction;
    }

    public Client getClient() {
        return client;
    }

    public Long getAuctionId() {
        return auction.getId();
    }

    public Long getClientId() {
        return client.getUserId();
    }

    public Double getAmount() {
        return amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Bid [id=" + id + ", auctionId=" + getAuctionId() + ", clientId=" + getClientId()
                + ", amount=" + amount + ", timestamp=" + timestamp + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bid)) return false;
        Bid that = (Bid) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hashCode(id);
    }
}