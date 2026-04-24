package ar.edu.itba.paw.webapp.controller.commerce;

import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.reservation.PickupByCodeError;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.services.user.ClientService;
import ar.edu.itba.paw.services.pack.PackService;
import ar.edu.itba.paw.services.reservation.PickupByCodeResult;
import ar.edu.itba.paw.services.reservation.ReservationService;
import ar.edu.itba.paw.webapp.auth.AuthUser;
import ar.edu.itba.paw.webapp.controller.utils.AuthenticatedUserResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/commerce")
public class CommercePickupController {

    private final ReservationService reservationService;
    private final PackService packService;
    private final ClientService clientService;
    private final AuthenticatedUserResolver authResolver;

    @Autowired
    public CommercePickupController(final ReservationService reservationService,
                                    final PackService packService,
                                    final ClientService clientService,
                                    final AuthenticatedUserResolver authResolver) {
        this.reservationService = reservationService;
        this.packService = packService;
        this.clientService = clientService;
        this.authResolver = authResolver;
    }

    @RequestMapping(value = "/verify-pickup", method = RequestMethod.GET)
    public ModelAndView verifyPickupForm() {
        return new ModelAndView("commerce/verify-pickup");
    }

    @RequestMapping(value = "/verify-pickup", method = RequestMethod.POST)
    public ModelAndView verifyPickupPost(@AuthenticationPrincipal final AuthUser principal,
            @RequestParam(value = "pickupCode", required = false) final String pickupCode) {
        final ModelAndView mav = new ModelAndView("commerce/verify-pickup");
        final Commerce commerce = authResolver.resolveCommerce(principal);

        final PickupByCodeResult result = reservationService.confirmPickupByCode(pickupCode, commerce.getUserId());
        if (result.isSuccess()) {
            final Reservation confirmed = result.reservation().orElseThrow(IllegalStateException::new);
            mav.addObject("pickupSuccess", true);
            mav.addObject("confirmedReservation", confirmed);

            if (confirmed.getPackId() != null) {
                packService.findById(confirmed.getPackId())
                        .ifPresent(pack -> mav.addObject("confirmedPack", pack));
            }

            if (confirmed.getCustomerId() != null) {
                clientService.findByUserId(confirmed.getCustomerId())
                        .ifPresent(client -> mav.addObject("confirmedClientName", client.getFullName()));
            }
        } else {
            final String key;
            final PickupByCodeError err = result.error().orElse(PickupByCodeError.NOT_FOUND);
            switch (err) {
                case EMPTY:
                    key = "commerce.verifyPickup.error.empty";
                    break;
                case NOT_FOUND:
                    key = "commerce.verifyPickup.error.notFound";
                    break;
                case WRONG_COMMERCE:
                    key = "commerce.verifyPickup.error.wrongCommerce";
                    break;
                case ALREADY_COMPLETED:
                    key = "commerce.verifyPickup.error.alreadyCompleted";
                    break;
                case ALREADY_CANCELED:
                    key = "commerce.verifyPickup.error.alreadyCanceled";
                    break;
                default:
                    key = "commerce.verifyPickup.error.notFound";
                    break;
            }
            mav.addObject("pickupError", key);
        }

        mav.addObject("submittedCode", pickupCode);
        return mav;
    }
}
