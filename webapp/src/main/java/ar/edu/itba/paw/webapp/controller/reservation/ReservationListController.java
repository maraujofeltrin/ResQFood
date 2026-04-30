package ar.edu.itba.paw.webapp.controller.reservation;

import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.reservation.ReservationRejectionError;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.services.reservation.ReservationService;
import ar.edu.itba.paw.services.reservation.ReservationServiceResult;
import ar.edu.itba.paw.webapp.controller.utils.AuthenticatedUserResolver;
import ar.edu.itba.paw.webapp.controller.utils.ReservationListModelBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Controller
@RequestMapping("/reservations")
public class ReservationListController {

    private final AuthenticatedUserResolver authResolver;
    private final ReservationListModelBuilder modelBuilder;
    private final ReservationService reservationService;

    @Autowired
    public ReservationListController(final AuthenticatedUserResolver authResolver,
            final ReservationListModelBuilder modelBuilder,
            final ReservationService reservationService) {
        this.authResolver = authResolver;
        this.modelBuilder = modelBuilder;
        this.reservationService = reservationService;
    }

    @GetMapping
    public ModelAndView reservations(
            @RequestParam(value = "page", defaultValue = "1") final int page,
            @RequestParam(value = "q", required = false) final String query,
            @RequestParam(value = "status", required = false) final String status,
            @RequestParam(value = "auctionStatus", required = false) final String auctionStatusParam,
            @RequestParam(value = "tab", required = false) final String tabParam,
            final Authentication authentication) {

        final User currentUser = authResolver.resolveUser(authentication);

        if (currentUser.getRole() == User.Role.CLIENT) {
            return modelBuilder.buildClientView(page, query, status, auctionStatusParam, tabParam, currentUser);
        }
        if (currentUser.getRole() == User.Role.COMMERCE) {
            return modelBuilder.buildCommerceView(page, query, status, currentUser);
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    @PostMapping("/{id}/reject")
    public String rejectReservationFromCard(@PathVariable("id") final Long reservationId,
            @RequestParam(value = "page", required = false) final Integer page,
            @RequestParam(value = "q", required = false) final String query,
            @RequestParam(value = "status", required = false) final String status,
            final Authentication authentication,
            final RedirectAttributes redirectAttributes) {
        final User currentUser = authResolver.requireRole(authentication, User.Role.COMMERCE);

        final ReservationServiceResult<ReservationRejectionError> result =
                reservationService.tryRejectReservationForCommerce(reservationId, currentUser.getId());
        if (result.isSuccess()) {
            redirectAttributes.addFlashAttribute("reservationActionKind", "success");
            redirectAttributes.addFlashAttribute("reservationActionMessageCode", "commerce.reservations.action.reject.success");
        } else {
            final ReservationRejectionError error = result.error().orElse(ReservationRejectionError.INVALID_STATUS);
            if (error == ReservationRejectionError.WRONG_COMMERCE) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
            redirectAttributes.addFlashAttribute("reservationActionKind", "error");
            redirectAttributes.addFlashAttribute("reservationActionMessageCode", rejectionMessageCode(error));
        }

        return "redirect:" + buildReservationsRedirectUrl(page, query, status);
    }

    private static String rejectionMessageCode(final ReservationRejectionError error) {
        switch (error) {
            case ALREADY_CANCELED:
                return "commerce.reservations.action.reject.alreadyCanceled";
            case ALREADY_COMPLETED:
                return "commerce.reservations.action.reject.alreadyCompleted";
            case INVALID_STATUS:
                return "commerce.reservations.action.reject.invalidStatus";
            default:
                return "commerce.reservations.action.reject.error";
        }
    }

    private static String buildReservationsRedirectUrl(final Integer page, final String query, final String status) {
        final StringBuilder baseUrl = new StringBuilder("/reservations");
        boolean firstParam = true;

        if (page != null && page > 1) {
            baseUrl.append(firstParam ? "?" : "&").append("page=").append(page);
            firstParam = false;
        }

        if (query != null && !query.isBlank()) {
            baseUrl.append(firstParam ? "?" : "&").append("q=").append(URLEncoder.encode(query.trim(), StandardCharsets.UTF_8));
            firstParam = false;
        }

        if (status != null && !status.isBlank()) {
            try {
                Reservation.Status statusFilter = Reservation.Status.valueOf(status.trim().toUpperCase(Locale.ROOT));
                baseUrl.append(firstParam ? "?" : "&").append("status=").append(statusFilter.name());
            } catch (final IllegalArgumentException ex) {
                // invalid status param, skip
            }
        }

        return baseUrl.toString();
    }
}
