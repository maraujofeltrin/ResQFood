package ar.edu.itba.paw.services.notification;

import ar.edu.itba.paw.models.notification.NotificationType;

import java.time.LocalDateTime;

public final class NotificationItemView {

    private final long id;
    private final NotificationType type;
    private final String packTitle;
    private final String commerceName;
    private final Double amount;
    private final String pickupCode;
    private final LocalDateTime pickupDate;
    private final LocalDateTime createdAt;
    private final boolean read;
    private final Long reservationId;
    private final Long auctionId;
    private final Long packId;
    private final String customerName;

    public NotificationItemView(final long id, final NotificationType type, final String packTitle,
            final String commerceName, final Double amount, final String pickupCode,
            final LocalDateTime pickupDate, final LocalDateTime createdAt, final boolean read,
            final Long reservationId, final Long auctionId, final Long packId, final String customerName) {
        this.id = id;
        this.type = type;
        this.packTitle = packTitle;
        this.commerceName = commerceName;
        this.amount = amount;
        this.pickupCode = pickupCode;
        this.pickupDate = pickupDate;
        this.createdAt = createdAt;
        this.read = read;
        this.reservationId = reservationId;
        this.auctionId = auctionId;
        this.packId = packId;
        this.customerName = customerName;
    }

    public long getId() { return id; }
    public NotificationType getType() { return type; }
    public String getPackTitle() { return packTitle; }
    public String getCommerceName() { return commerceName; }
    public Double getAmount() { return amount; }
    public String getPickupCode() { return pickupCode; }
    public LocalDateTime getPickupDate() { return pickupDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isRead() { return read; }
    public Long getReservationId() { return reservationId; }
    public Long getAuctionId() { return auctionId; }
    public Long getPackId() { return packId; }
    public String getCustomerName() { return customerName; }
}
