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
import ar.edu.itba.paw.webapp.controller.helpers.AuthenticatedUserResolver;
import ar.edu.itba.paw.webapp.controller.helpers.PackDetailModelBuilder;
import ar.edu.itba.paw.webapp.form.BidForm;
import ar.edu.itba.paw.webapp.form.ReservationForm;

@Controller
public class PackDetailController {

    private final PackService packService;
    private final AuthenticatedUserResolver authResolver;
    private final PackDetailModelBuilder packDetailModelBuilder;

    @Autowired
    public PackDetailController(final PackService packService,
            final AuthenticatedUserResolver authResolver,
            final PackDetailModelBuilder packDetailModelBuilder) {
        this.packService = packService;
        this.authResolver = authResolver;
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
    public ModelAndView packDetail(@PathVariable("id") final long id) {
        final Long viewerUserId = authResolver.resolveUserOrEmpty()
                .map(user -> user.getId())
                .orElse(null);
        final Pack pack = packService.findVisibleForDetail(id, viewerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return packDetailModelBuilder.buildPackDetailModel(pack, createDefaultReservationForm(), createDefaultBidForm());
    }
}
