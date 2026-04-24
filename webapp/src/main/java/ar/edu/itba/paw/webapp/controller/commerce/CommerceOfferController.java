package ar.edu.itba.paw.webapp.controller.commerce;

import ar.edu.itba.paw.models.pack.PackTag;
import ar.edu.itba.paw.services.commerce.CommerceOfferService;
import ar.edu.itba.paw.webapp.auth.AuthUser;
import ar.edu.itba.paw.webapp.controller.utils.AuthenticatedUserResolver;
import ar.edu.itba.paw.webapp.form.CreateOfferForm;
import ar.edu.itba.paw.webapp.validation.CreateOfferFormValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Collections;

@Controller
@RequestMapping("/commerce")
public class CommerceOfferController {

    private final CommerceOfferService commerceOfferService;
    private final CreateOfferFormValidator createOfferFormValidator;
    private final MessageSource messageSource;
    private final AuthenticatedUserResolver authResolver;

    @Autowired
    public CommerceOfferController(final CommerceOfferService commerceOfferService,
                                   final CreateOfferFormValidator createOfferFormValidator,
                                   final MessageSource messageSource,
                                   final AuthenticatedUserResolver authResolver) {
        this.commerceOfferService = commerceOfferService;
        this.createOfferFormValidator = createOfferFormValidator;
        this.messageSource = messageSource;
        this.authResolver = authResolver;
    }

    @RequestMapping(value = "/create-offer", method = RequestMethod.GET)
    public ModelAndView createOfferForm(@ModelAttribute("createOfferForm") final CreateOfferForm form,
            @RequestParam(value = "error", required = false) final String error) {
        final ModelAndView mav = new ModelAndView("commerce/createOfferView");
        mav.addObject("availableTags", PackTag.values());
        if ("maxUploadSize".equals(error)) {
            mav.addObject("errorMessage",
                    messageSource.getMessage("commerce.createPack.validation.image.maxSize",
                            null, LocaleContextHolder.getLocale()));
        }
        return mav;
    }

    @RequestMapping(value = "/create-offer", method = RequestMethod.POST)
    public ModelAndView createOffer(
            @AuthenticationPrincipal final AuthUser principal,
            @Valid @ModelAttribute("createOfferForm") final CreateOfferForm form,
            final BindingResult bindingResult,
            final RedirectAttributes redirectAttributes) {

        createOfferFormValidator.validate(form, bindingResult);
        if (bindingResult.hasErrors()) {
            final ModelAndView mav = new ModelAndView("commerce/createOfferView");
            mav.addObject("availableTags", PackTag.values());
            return mav;
        }

        final boolean isAuction = form.getIsAuction();

        try {
            final long commerceId = authResolver.resolveUser(principal).getId();

            byte[] imageData = null;
            String imageContentType = null;
            final MultipartFile image = form.getImage();
            if (image != null && !image.isEmpty()) {
                imageData = image.getBytes();
                imageContentType = image.getContentType();
            }

            if (isAuction) {
                commerceOfferService.createAuctionOffer(
                        commerceId, form.getTitle(), form.getDescription(), form.getOriginalPrice(),
                        form.getInitialPrice(), form.getEndDate(), form.getEndTime(),
                        form.getTags() != null ? form.getTags() : Collections.emptyList(), imageData, imageContentType);
                redirectAttributes.addFlashAttribute("dashboardAlertKind", "success");
                redirectAttributes.addFlashAttribute("dashboardAlertMessage", messageSource.getMessage(
                        "commerce.dashboard.success.create.auction", null, LocaleContextHolder.getLocale()));
            } else {
                commerceOfferService.createDirectPack(
                        commerceId, form.getTitle(), form.getDescription(), form.getOriginalPrice(),
                        form.getFinalPrice(), form.getStock(),
                        form.getTags() != null ? form.getTags() : Collections.emptyList(), imageData, imageContentType);
                redirectAttributes.addFlashAttribute("dashboardAlertKind", "success");
                redirectAttributes.addFlashAttribute("dashboardAlertMessage", messageSource
                        .getMessage("commerce.dashboard.success.create.pack", null, LocaleContextHolder.getLocale()));
            }
            return new ModelAndView("redirect:/commerce");

        } catch (final IllegalArgumentException e) {
            final ModelAndView mav = new ModelAndView("commerce/createOfferView");
            mav.addObject("availableTags", PackTag.values());
            mav.addObject("errorMessage", e.getMessage());
            return mav;
        } catch (IOException e) {
            final ModelAndView mav = new ModelAndView("commerce/createOfferView");
            mav.addObject("availableTags", PackTag.values());
            mav.addObject("errorMessage",
                    messageSource.getMessage("commerce.createPack.validation.image.processError",
                            null, LocaleContextHolder.getLocale()));
            return mav;
        }
    }
}
