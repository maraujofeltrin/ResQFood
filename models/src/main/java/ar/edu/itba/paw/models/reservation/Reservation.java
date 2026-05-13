package ar.edu.itba.paw.models.reservation;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.user.Client;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
public class Reservation {
    public enum Status {
        RESERVED,
        PAID,
        CANCELED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reservations_id_seq")
    @SequenceGenerator(sequenceName = "reservations_id_seq", name = "reservations_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "customer_id")
    private Long customerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    private Client customer;

    @Column(name = "pack_id")
    private Long packId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pack_id", insertable = false, updatable = false)
    private Pack pack;

    @Column(name = "reservation_date")
    private LocalDateTime reservationDate;

    @Column(name = "final_price")
    private Double finalPrice;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private Status status;

    @Column(name = "pickup_code", unique = true, length = 255)
    private String pickupCode;

    @Column(name = "pickup_confirmation_date")
    private LocalDateTime pickupConfirmationDate;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "pickup_window", length = 512)
    private String pickupWindow;

    protected Reservation() {
        // Just for Hibernate
    }

    public Reservation(Long id, Long customerId, Long packId, LocalDateTime reservationDate, Double finalPrice,
            Status status, String pickupCode, LocalDateTime pickupConfirmationDate, Integer quantity,
            String pickupWindow) {
        this.id = id;
        this.customerId = customerId;
        this.packId = packId;
        this.reservationDate = reservationDate;
        this.finalPrice = finalPrice;
        this.status = status;
        this.pickupCode = pickupCode;
        this.pickupConfirmationDate = pickupConfirmationDate;
        this.quantity = quantity;
        this.pickupWindow = pickupWindow;
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public Client getCustomer() {
        return customer;
    }

    public Long getPackId() {
        return packId;
    }

    public Pack getPack() {
        return pack;
    }

    public LocalDateTime getReservationDate() {
        return reservationDate;
    }

    public Double getFinalPrice() {
        return finalPrice;
    }

    public Status getStatus() {
        return status;
    }

    public String getPickupCode() {
        return pickupCode;
    }

    public LocalDateTime getPickupConfirmationDate() {
        return pickupConfirmationDate;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public String getPickupWindow() {
        return pickupWindow;
    }

    @Override
    public String toString() {
        return "Reservation [id=" + id + ", customerId=" + customerId + ", packId=" + packId + ", reservationDate="
                + reservationDate + ", finalPrice=" + finalPrice + ", status=" + status + ", pickupCode=" + pickupCode
                + ", pickupConfirmationDate=" + pickupConfirmationDate + ", quantity=" + quantity + ", pickupWindow="
                + pickupWindow + "]";
    }
}
