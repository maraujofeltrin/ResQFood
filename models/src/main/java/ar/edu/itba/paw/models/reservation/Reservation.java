package ar.edu.itba.paw.models.reservation;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.user.Client;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id")
    private Client customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pack_id")
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

    public Reservation(final Long id, final Client customer, final Pack pack, final LocalDateTime reservationDate,
            final Double finalPrice, final Status status, final String pickupCode,
            final LocalDateTime pickupConfirmationDate, final Integer quantity, final String pickupWindow) {
        this.id = id;
        this.customer = customer;
        this.pack = pack;
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

    public Client getCustomer() {
        return customer;
    }

    public Pack getPack() {
        return pack;
    }

    public Long getCustomerId() {
        return customer.getUserId();
    }

    public Long getPackId() {
        return pack.getId();
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
        return "Reservation [id=" + id + ", customerId=" + getCustomerId() + ", packId=" + getPackId()
                + ", reservationDate=" + reservationDate + ", finalPrice=" + finalPrice + ", status=" + status
                + ", pickupCode=" + pickupCode + ", pickupConfirmationDate=" + pickupConfirmationDate + ", quantity="
                + quantity + ", pickupWindow=" + pickupWindow + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation)) return false;
        Reservation that = (Reservation) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hashCode(id);
    }
}