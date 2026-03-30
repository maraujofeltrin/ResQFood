package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.Reservation;

public interface ReservationMailService {

    void sendReservationRequestToCommerce(Reservation reservation, String commerceEmail, String baseUrl);

    void sendReservationCodeToClient(Reservation reservation, String clientEmail);
}
