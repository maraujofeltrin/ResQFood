package ar.edu.itba.paw.webapp.controller.commerce;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.models.user.CommerceReviewException;
import ar.edu.itba.paw.services.commerce.CommerceReviewService;
import ar.edu.itba.paw.services.commerce.CommerceService;
import ar.edu.itba.paw.services.pack.PackService;
import ar.edu.itba.paw.webapp.controller.helpers.AuthenticatedUserResolver;
import ar.edu.itba.paw.webapp.controller.helpers.CommerceProfileModelBuilder;
import ar.edu.itba.paw.webapp.controller.helpers.PackDetailModelBuilder;
import ar.edu.itba.paw.webapp.form.BidForm;
import ar.edu.itba.paw.webapp.form.CommerceReviewForm;
import ar.edu.itba.paw.webapp.form.ReservationForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.Locale;

@Controller
public class CommerceReviewController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommerceReviewController.class);

    private final CommerceReviewService commerceReviewService;
    private final PackService packService;
    private final CommerceService commerceService;
    private final AuthenticatedUserResolver authResolver;
    private final PackDetailModelBuilder packDetailModelBuilder;
    private final CommerceProfileModelBuilder commerceProfileModelBuilder;
    private final MessageSource messageSource;

    @Autowired
    public CommerceReviewController(final CommerceReviewService commerceReviewService, final PackService packService,
            final CommerceService commerceService, final AuthenticatedUserResolver authResolver,
            final PackDetailModelBuilder packDetailModelBuilder,
            final CommerceProfileModelBuilder commerceProfileModelBuilder,
            final MessageSource messageSource) {
        this.commerceReviewService = commerceReviewService;
        this.packService = packService;
        this.commerceService = commerceService;
        this.authResolver = authResolver;
        this.packDetailModelBuilder = packDetailModelBuilder;
        this.commerceProfileModelBuilder = commerceProfileModelBuilder;
        this.messageSource = messageSource;
    }

    @PostMapping("/packs/{packId}/commerce-review")
    public ModelAndView submitReviewFromPack(@PathVariable("packId") final long packId,
            @Valid @ModelAttribute("commerceReviewForm") final CommerceReviewForm commerceReviewForm,
            final BindingResult bindingResult,
            final RedirectAttributes redirectAttributes) {
        final Pack pack = packService.findById(packId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (bindingResult.hasErrors()) {
            return packDetailModelBuilder.buildPackDetailModel(pack, createDefaultReservationForm(), new BidForm(),
                    commerceReviewForm);
        }

        return processReviewSubmit(pack.getCommerceId(), commerceReviewForm, redirectAttributes,
                "redirect:/packs/" + packId + "#commerce-reviews");
    }

    @PostMapping("/commerces/{commerceUserId}/commerce-review")
    public ModelAndView submitReviewFromProfile(@PathVariable final long commerceUserId,
            @Valid @ModelAttribute("commerceReviewForm") final CommerceReviewForm commerceReviewForm,
            final BindingResult bindingResult,
            final RedirectAttributes redirectAttributes) {
        if (!commerceService.findByUserId(commerceUserId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (bindingResult.hasErrors()) {
            return commerceProfileModelBuilder.buildProfileModel(commerceUserId, 1, commerceReviewForm)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        }

        return processReviewSubmit(commerceUserId, commerceReviewForm, redirectAttributes,
                "redirect:/commerces/" + commerceUserId + "#commerce-reviews");
    }

    private ModelAndView processReviewSubmit(final long commerceUserId,
            final CommerceReviewForm commerceReviewForm, final RedirectAttributes redirectAttributes,
            final String redirectUrl) {
        final Locale locale = LocaleContextHolder.getLocale();
        final User currentUser = authResolver.resolveUser();

        try {
            commerceReviewService.upsertReview(currentUser.getId(), commerceUserId,
                    commerceReviewForm.getRating().intValue(), commerceReviewForm.getBody());
            redirectAttributes.addFlashAttribute("commerceReviewAlertKind", "success");
            redirectAttributes.addFlashAttribute("commerceReviewAlertMessage",
                    messageSource.getMessage("pack.detail.reviews.alert.success", null, locale));
        } catch (final CommerceReviewException ex) {
            LOGGER.debug("Commerce review rejected clientId={} commerceUserId={} reason={}",
                    Long.valueOf(currentUser.getId()), Long.valueOf(commerceUserId), ex.getReason(), ex);
            redirectAttributes.addFlashAttribute("commerceReviewAlertKind", "error");
            if (ex.getReason() == CommerceReviewException.Reason.NOT_ELIGIBLE) {
                redirectAttributes.addFlashAttribute("commerceReviewAlertMessage",
                        messageSource.getMessage("pack.detail.reviews.alert.notEligible", null, locale));
            } else {
                redirectAttributes.addFlashAttribute("commerceReviewAlertMessage",
                        messageSource.getMessage("pack.detail.reviews.alert.invalid", null, locale));
            }
            redirectAttributes.addFlashAttribute("commerceReviewFormExpanded", Boolean.TRUE);
        }

        return new ModelAndView(redirectUrl);
    }

    private ReservationForm createDefaultReservationForm() {
        final ReservationForm form = new ReservationForm();
        form.setQuantity(Integer.valueOf(1));
        return form;
    }
}
