package ar.edu.itba.paw.services.reservation;

import ar.edu.itba.paw.models.reservation.Reservation;
import java.util.Locale;

public interface ReservationMailService {

    void sendReservationRequestToCommerce(Reservation reservation, String commerceEmail, String baseUrl,
        String pickupDateStr, Locale locale);

    void sendReservationCodeToClient(Reservation reservation, String clientEmail, String pickupDateStr,
        Locale locale);

    void sendAuctionWinnerCodeToClient(Reservation reservation, String clientEmail, String pickupDateStr,
        Locale locale);

    void sendAuctionWinnerCodeToCommerce(Reservation reservation, String commerceEmail, String pickupDateStr,
        Locale locale);

    void sendReservationRejectedToClient(Reservation reservation, String clientEmail, Locale locale);
}
