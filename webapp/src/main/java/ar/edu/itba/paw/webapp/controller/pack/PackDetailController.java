package ar.edu.itba.paw.webapp.controller.pack;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.services.pack.PackService;
import ar.edu.itba.paw.webapp.auth.AuthUserLocaleSupport;
import ar.edu.itba.paw.webapp.controller.helpers.PackDetailModelBuilder;
import ar.edu.itba.paw.webapp.form.BidForm;
import ar.edu.itba.paw.webapp.form.ReservationForm;
import org.springframework.security.core.Authentication;

@Controller
public class PackDetailController {

    private final PackService packService;
    private final PackDetailModelBuilder packDetailModelBuilder;

    @Autowired
    public PackDetailController(final PackService packService,
            final PackDetailModelBuilder packDetailModelBuilder) {
        this.packService = packService;
        this.packDetailModelBuilder = packDetailModelBuilder;
    }

    private ReservationForm createDefaultReservationForm() {
        final ReservationForm form = new ReservationForm();
        form.setQuantity(Integer.valueOf(1));
        return form;
    }

    private BidForm createDefaultBidForm() {
        return new BidForm();
    }

    @GetMapping("/packs/{id}")
    public ModelAndView packDetail(@PathVariable("id") final long id, final Authentication authentication) {
        final Long viewerUserId = AuthUserLocaleSupport.authUserFrom(authentication)
                .map(authUser -> Long.valueOf(authUser.getId()))
                .orElse(null);
        final Pack pack = packService.findVisibleForDetail(id, viewerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return packDetailModelBuilder.buildPackDetailModel(pack, createDefaultReservationForm(), createDefaultBidForm());
    }
}
