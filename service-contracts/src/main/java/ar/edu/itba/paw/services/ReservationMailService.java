package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.Reservation;

public interface ReservationMailService {

    void sendReservationRequestToCommerce(Reservation reservation, String commerceEmail, String baseUrl,
            String pickupDateStr);

    void sendReservationCodeToClient(Reservation reservation, String clientEmail, String pickupDateStr);

    void sendReservationRejectedToClient(Reservation reservation, String clientEmail);
}
