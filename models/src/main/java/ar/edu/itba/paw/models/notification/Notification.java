package ar.edu.itba.paw.models.notification;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.user.User;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notifications_id_seq")
    @SequenceGenerator(sequenceName = "notifications_id_seq", name = "notifications_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 64)
    private NotificationType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id")
    private Auction auction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pack_id")
    private Pack pack;

    @Column(name = "pack_title", length = 255)
    private String packTitle;

    @Column(name = "commerce_name", length = 255)
    private String commerceName;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "pickup_code", length = 32)
    private String pickupCode;

    @Column(name = "pickup_date")
    private LocalDateTime pickupDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    protected Notification() {}

    public Notification(final Long id, final User recipient, final NotificationType type,
            final Reservation reservation, final Auction auction, final Pack pack,
            final String packTitle, final String commerceName, final Double amount,
            final String pickupCode, final LocalDateTime pickupDate,
            final LocalDateTime createdAt, final LocalDateTime readAt, final LocalDateTime deletedAt) {
        this.id = id;
        this.recipient = recipient;
        this.type = type;
        this.reservation = reservation;
        this.auction = auction;
        this.pack = pack;
        this.packTitle = packTitle;
        this.commerceName = commerceName;
        this.amount = amount;
        this.pickupCode = pickupCode;
        this.pickupDate = pickupDate;
        this.createdAt = createdAt;
        this.readAt = readAt;
        this.deletedAt = deletedAt;
    }

    public Long getId() { return id; }
    public User getRecipient() { return recipient; }
    public Long getRecipientId() { return recipient.getId(); }
    public NotificationType getType() { return type; }
    public Reservation getReservation() { return reservation; }
    public Auction getAuction() { return auction; }
    public Pack getPack() { return pack; }
    public String getPackTitle() { return packTitle; }
    public String getCommerceName() { return commerceName; }
    public Double getAmount() { return amount; }
    public String getPickupCode() { return pickupCode; }
    public LocalDateTime getPickupDate() { return pickupDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getReadAt() { return readAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }

    public void setReadAt(final LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public void setDeletedAt(final LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
