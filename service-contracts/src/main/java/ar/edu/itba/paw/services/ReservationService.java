package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.Reservation;

import java.util.Optional;

public interface ReservationService {
    Optional<Reservation> findById(final Long id);

    Reservation confirmPickup(final Long id);
}
