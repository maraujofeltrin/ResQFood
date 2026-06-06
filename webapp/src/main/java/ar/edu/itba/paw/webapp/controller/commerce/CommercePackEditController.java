package ar.edu.itba.paw.webapp.controller.commerce;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.pack.PackTag;
import ar.edu.itba.paw.models.image.Image;
import ar.edu.itba.paw.services.commerce.CommerceService;
import ar.edu.itba.paw.services.image.ImageService;
import ar.edu.itba.paw.services.pack.PackService;
import ar.edu.itba.paw.webapp.auth.AuthUser;
import ar.edu.itba.paw.webapp.controller.helpers.AuthenticatedUserResolver;
import ar.edu.itba.paw.webapp.form.CreateOfferForm;
import ar.edu.itba.paw.webapp.validation.CreateOfferFormValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.io.IOException;

@Controller
@RequestMapping("/commerce")
public class CommercePackEditController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommercePackEditController.class);

    private final CommerceService commerceService;
    private final PackService packService;
    private final CreateOfferFormValidator createOfferFormValidator;
    private final MessageSource messageSource;
    private final AuthenticatedUserResolver authResolver;
    private final ImageService imageService;

    @Autowired
    public CommercePackEditController(final CommerceService commerceService,
                                      final PackService packService,
                                      final CreateOfferFormValidator createOfferFormValidator,
                                      final MessageSource messageSource,
                                      final AuthenticatedUserResolver authResolver,
                                      final ImageService imageService) {
        this.commerceService = commerceService;
        this.packService = packService;
        this.createOfferFormValidator = createOfferFormValidator;
        this.messageSource = messageSource;
        this.authResolver = authResolver;
        this.imageService = imageService;
    }

    @RequestMapping(value = "/edit-pack/{packId}", method = RequestMethod.GET)
    @PreAuthorize("@own.canWritePack(#packId, authentication.principal.id)")
    public ModelAndView editPackForm(@PathVariable("packId") final long packId,
            @AuthenticationPrincipal final AuthUser principal,
            @ModelAttribute("createOfferForm") final CreateOfferForm form,
            @RequestParam(value = "error", required = false) final String error) {
        final long commerceId = authResolver.resolveUser(principal).getId();

        if (commerceService.findByUserId(commerceId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        final Pack pack = packService.resolvePackForDirectEdit(packId);

        if (form.getTitle() == null) {
            form.setTitle(pack.getTitle());
            form.setDescription(pack.getDescription());
            form.setTags(pack.getTags());
            form.setOriginalPrice(pack.getOriginalPrice());
            form.setFinalPrice(pack.getFinalPrice());
            form.setStock(pack.getStock());
            form.setIsAuction(false);
            form.setExistingImageId(pack.getImageId());
        }

        final ModelAndView mav = new ModelAndView("commerce/editPack");
        mav.addObject("editMode", true);
        mav.addObject("commerceId", commerceId);
        mav.addObject("packId", packId);
        mav.addObject("availableTags", PackTag.values());

        if ("maxUploadSize".equals(error)) {
            mav.addObject("errorMessage",
                    messageSource.getMessage("commerce.createPack.validation.image.maxSize",
                             null, LocaleContextHolder.getLocale()));
        }

        return mav;
    }

    @RequestMapping(value = "/edit-pack/{packId}", method = RequestMethod.POST)
    @PreAuthorize("@own.canWritePack(#packId, authentication.principal.id)")
    public ModelAndView editPack(
            @PathVariable("packId") final long packId,
            @AuthenticationPrincipal final AuthUser principal,
            @Valid @ModelAttribute("createOfferForm") final CreateOfferForm form,
            final BindingResult bindingResult,
            final RedirectAttributes redirectAttributes) {

        form.setIsAuction(false);
        createOfferFormValidator.validatePackModeOnly(form, bindingResult);

        final long commerceId = authResolver.resolveUser(principal).getId();

        if (commerceService.findByUserId(commerceId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (bindingResult.hasErrors()) {
            final ModelAndView mav = new ModelAndView("commerce/editPack");
            mav.addObject("editMode", true);
            mav.addObject("commerceId", commerceId);
            mav.addObject("packId", packId);
            mav.addObject("availableTags", PackTag.values());

            // Persist uploaded image temporarily to show preview on validation failure
            final MultipartFile image = form.getImage();
            if (image != null && !image.isEmpty()) {
                try {
                    final Image savedImage = imageService.saveImage(image.getBytes(), image.getContentType());
                    form.setExistingImageId(savedImage.getId());
                } catch (final IOException ex) {
                    LOGGER.warn("Failed to persist uploaded image preview on edit-pack validation failure", ex);
                }
            }

            return mav;
        }

        try {
            final MultipartFile image = form.getImage();
            Long imageId = form.getExistingImageId();
            if (image != null && !image.isEmpty()) {
                final Image savedImage = imageService.saveImage(image.getBytes(), image.getContentType());
                imageId = savedImage.getId();
            }

            packService.updatePack(
                    packId,
                    form.getTitle(),
                    form.getDescription(),
                    form.getOriginalPrice(),
                    form.getFinalPrice(),
                    form.getStock(),
                    form.getTags(),
                    imageId
            );

            redirectAttributes.addFlashAttribute("dashboardAlertKind", "success");
            redirectAttributes.addFlashAttribute("dashboardAlertMessage",
                    messageSource.getMessage("commerce.dashboard.success.edit", null, LocaleContextHolder.getLocale()));

            return new ModelAndView("redirect:/commerce/products");

        } catch (final ar.edu.itba.paw.models.image.ProfileImageException e) {
            LOGGER.debug("Edit pack rejected by validation", e);
            final ModelAndView mav = new ModelAndView("commerce/editPack");
            mav.addObject("editMode", true);
            mav.addObject("commerceId", commerceId);
            mav.addObject("packId", packId);
            mav.addObject("availableTags", PackTag.values());
            mav.addObject("errorMessage", e.getMessage());
            return mav;
        } catch (final IOException e) {
            LOGGER.warn("Edit pack failed while processing image payload for packId={}", Long.valueOf(packId), e);
            final ModelAndView mav = new ModelAndView("commerce/editPack");
            mav.addObject("editMode", true);
            mav.addObject("commerceId", commerceId);
            mav.addObject("packId", packId);
            mav.addObject("availableTags", PackTag.values());
            mav.addObject("errorMessage",
                    messageSource.getMessage("commerce.createPack.validation.image.processError",
                            null, LocaleContextHolder.getLocale()));
            return mav;
        }
    }

    @RequestMapping(value = "/delete-pack/{packId}", method = RequestMethod.POST)
    @PreAuthorize("@own.canWritePack(#packId, authentication.principal.id)")
    public ModelAndView deletePack(
            @PathVariable("packId") final long packId,
            @AuthenticationPrincipal final AuthUser principal,
            final RedirectAttributes redirectAttributes) {

        final long commerceId = authResolver.resolveUser(principal).getId();

        if (commerceService.findByUserId(commerceId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        packService.deletePack(packId);

        redirectAttributes.addFlashAttribute("dashboardAlertKind", "success");
        redirectAttributes.addFlashAttribute("dashboardAlertMessage",
                messageSource.getMessage("commerce.dashboard.success.delete", null, LocaleContextHolder.getLocale()));

        return new ModelAndView("redirect:/commerce/products");
    }
}
