package ar.edu.itba.paw.webapp.controller.reservation;

import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.webapp.controller.utils.AuthenticatedUserResolver;
import ar.edu.itba.paw.webapp.controller.utils.ReservationListModelBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/reservations")
public class ReservationListController {

    private final AuthenticatedUserResolver authResolver;
    private final ReservationListModelBuilder modelBuilder;

    @Autowired
    public ReservationListController(final AuthenticatedUserResolver authResolver,
            final ReservationListModelBuilder modelBuilder) {
        this.authResolver = authResolver;
        this.modelBuilder = modelBuilder;
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
}
