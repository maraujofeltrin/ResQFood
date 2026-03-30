package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.models.Reservation;
import ar.edu.itba.paw.services.ReservationMailService;
import ar.edu.itba.paw.services.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Solo para probar el envío de mail en desarrollo. No usar en producción sin protección.
 */
@Controller
@RequestMapping("/dev")
public class ReservationMailTestController {

    private final ReservationMailService reservationMailService;
    private final ReservationService reservationService;
    private final String appBaseUrl;

    @Autowired
    public ReservationMailTestController(final ReservationMailService reservationMailService,
            final ReservationService reservationService,
            @Value("${app.base-url}") final String appBaseUrl) {
        this.reservationMailService = reservationMailService;
        this.reservationService = reservationService;
        this.appBaseUrl = appBaseUrl;
    }

    /**
     * Ejemplo:
     * GET /webapp/dev/reservation-mail-test?reservationId=1&to=tu_mail@gmail.com
     */
    @GetMapping(value = "/reservation-mail-test", produces = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8")
    @ResponseBody
    public String sendTest(@RequestParam final long reservationId, @RequestParam final String to) {
        final Reservation reservation = reservationService.findById(reservationId).orElseThrow(
                () -> new IllegalArgumentException("No existe reserva con id: " + reservationId));
        reservationMailService.sendReservationRequestToCommerce(reservation, to, appBaseUrl);
        return "Correo enviado a " + to + " (reserva #" + reservationId + "). Revisá la bandeja y el spam.";
    }
}
