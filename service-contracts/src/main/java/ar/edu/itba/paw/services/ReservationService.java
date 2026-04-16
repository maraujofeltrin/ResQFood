package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.Reservation;

import java.util.List;
import java.util.Optional;

public interface ReservationService {

    /**
     * Crea o reutiliza {@link ar.edu.itba.paw.models.User} por email (rol CLIENT si es nuevo), asegura fila en
     * {@code clients}, y persiste la reserva con {@code customer_id} = {@code clients.user_id}.
     */
    Reservation createReservation(long packId, String email, String firstName, String lastName, String phone,
            int quantity, double unitPrice, String pickupWindow, String baseUrl);

    Optional<Reservation> findById(final Long id);

    List<Reservation> findByCustomerId(final Long customerId);

    List<Reservation> findByCommerceId(final Long commerceId);

    String computePickupDateStr(Reservation reservation);

    Reservation confirmPickup(final Long id);
}
