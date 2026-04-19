package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.models.Pack;
import ar.edu.itba.paw.models.PackTag;
import ar.edu.itba.paw.models.Commerce;
import ar.edu.itba.paw.services.AuctionService;
import ar.edu.itba.paw.services.CommerceService;
import ar.edu.itba.paw.services.PackService;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.webapp.auth.AuthUser;
import ar.edu.itba.paw.webapp.form.CreateOfferForm;
import ar.edu.itba.paw.webapp.form.EditPackForm;

@Controller
@RequestMapping("/commerce")
public class CommerceController {

    private static final int PAGE_SIZE = 6;
    private static final Set<String> ALLOWED_IMAGE_TYPES = new HashSet<>(Arrays.asList(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    ));

    private final CommerceService commerceService;
    private final PackService packService;
    private final AuctionService auctionService;
    private final MessageSource messageSource;
    private final UserService userService;

    @Autowired
    public CommerceController(final CommerceService commerceService, final PackService packService,
                              final AuctionService auctionService, final MessageSource messageSource,
                              final UserService userService) {
        this.commerceService = commerceService;
        this.packService = packService;
        this.auctionService = auctionService;
        this.messageSource = messageSource;
        this.userService = userService;
    }

    /**
     * Entry point: renderiza el dashboard del commerce autenticado.
     */
    @GetMapping(value = "")
    public ModelAndView dashboard(@AuthenticationPrincipal final AuthUser principal,
                                  @RequestParam(value = "page", defaultValue = "1") final int page,
                                  @RequestParam(value = "tab", defaultValue = "items") final String tab) {
        final long id = getAuthenticatedUser(principal).getId();

        final java.util.Optional<Commerce> commerceOpt = commerceService.findByUserId(id);
        if (!commerceOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        final Commerce commerce = commerceOpt.get();
        final ModelAndView mav = new ModelAndView("commerce/dashboard");

        final List<Pack> allPacks = packService.findByCommerceId(id);
        final List<ar.edu.itba.paw.models.Auction> commerceAuctions = auctionService.findByCommerceId(id);
        final Set<Long> auctionPackIds = commerceAuctions.stream()
                .map(a -> a.getPack().getId())
                .collect(java.util.stream.Collectors.toSet());
        
        final List<Pack> displayedPacks;
        if ("auctions".equalsIgnoreCase(tab)) {
            displayedPacks = allPacks.stream()
                    .filter(p -> auctionPackIds.contains(p.getId()))
                    .toList();
        } else if ("packs".equalsIgnoreCase(tab)) {
            displayedPacks = allPacks.stream()
                    .filter(p -> !auctionPackIds.contains(p.getId()))
                    .toList();
        } else {
            displayedPacks = allPacks;
        }

        final int totalPages = Math.max(1, (int) Math.ceil((double) displayedPacks.size() / PAGE_SIZE));
        final int safePage = Math.max(1, Math.min(page, totalPages));
        final int fromIdx = (safePage - 1) * PAGE_SIZE;
        final int toIdx = Math.min(fromIdx + PAGE_SIZE, displayedPacks.size());

        mav.addObject("commerce", commerce);
        mav.addObject("packs", displayedPacks.subList(fromIdx, toIdx));
        mav.addObject("currentPage", safePage);
        mav.addObject("totalPages", totalPages);
        mav.addObject("commerceId", id);
        mav.addObject("auctionPackIds", auctionPackIds);
        mav.addObject("currentTab", tab);
        mav.addObject("paginationBaseUrl", "/commerce?tab=" + tab);
        return mav;
    }

    // ── Create Offer (unified pack / auction) ────────────────

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

        final boolean isAuction = form.getIsAuction();

        // ── Conditional validation ──
        if (isAuction) {
            validateAuctionFields(form, bindingResult);
        } else {
            validatePackFields(form, bindingResult);
        }

        validateImage(form.getImage(), bindingResult);

        if (bindingResult.hasErrors()) {
            final ModelAndView mav = new ModelAndView("commerce/createOfferView");
            mav.addObject("availableTags", PackTag.values());
            return mav;
        }

        try {
            final long commerceId = getAuthenticatedUser(principal).getId();

            byte[] imageData = null;
            String imageContentType = null;
            final MultipartFile image = form.getImage();
            if (image != null && !image.isEmpty()) {
                imageData = image.getBytes();
                imageContentType = image.getContentType();
            }

            if (isAuction) {
                // Create the underlying pack (stock=1, finalPrice=initialPrice as floor)
                Pack pack = packService.createPack(
                        commerceId, form.getTitle(), form.getDescription(),
                        form.getOriginalPrice(), form.getInitialPrice(), 1,
                        form.getTags() != null ? form.getTags() : Collections.emptyList(),
                        imageData, imageContentType
                );

                // Parse auction end date/time (already validated)
                LocalDateTime endDateTime = LocalDateTime.parse(form.getEndDate() + "T" + form.getEndTime());

                // Create the auction wrapping the pack
                auctionService.createAuction(pack.getId(), form.getInitialPrice(), endDateTime);

                redirectAttributes.addFlashAttribute("dashboardAlertKind", "success");
                redirectAttributes.addFlashAttribute("dashboardAlertMessage", messageSource.getMessage("commerce.dashboard.success.create.auction", null, LocaleContextHolder.getLocale()));

            } else {
                packService.createPack(commerceId, form.getTitle(), form.getDescription(),
                                       form.getOriginalPrice(), form.getFinalPrice(), form.getStock(),
                                       form.getTags() != null ? form.getTags() : Collections.emptyList(),
                                       imageData, imageContentType);
                redirectAttributes.addFlashAttribute("dashboardAlertKind", "success");
                redirectAttributes.addFlashAttribute("dashboardAlertMessage", messageSource.getMessage("commerce.dashboard.success.create.pack", null, LocaleContextHolder.getLocale()));
            }
            return new ModelAndView("redirect:/commerce");

        } catch (IllegalArgumentException e) {
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

    // ── Helpers ───────────────────────────────────────────────

    /**
     * Validates pack-specific fields (finalPrice, stock) when isAuction is false.
     */
    private void validatePackFields(final CreateOfferForm form, final BindingResult bindingResult) {
        if (form.getFinalPrice() == null) {
            bindingResult.rejectValue("finalPrice", "error.finalPrice",
                    messageSource.getMessage("commerce.createPack.validation.finalPrice.notNull",
                            null, LocaleContextHolder.getLocale()));
        } else if (form.getFinalPrice() <= 0) {
            bindingResult.rejectValue("finalPrice", "error.finalPrice",
                    messageSource.getMessage("commerce.createPack.validation.finalPrice.positive",
                            null, LocaleContextHolder.getLocale()));
        } else if (form.getOriginalPrice() != null && form.getFinalPrice() > form.getOriginalPrice()) {
            bindingResult.rejectValue("finalPrice", "error.finalPrice",
                    messageSource.getMessage("commerce.createPack.validation.finalPrice.exceedsOriginal",
                            null, LocaleContextHolder.getLocale()));
        }

        if (form.getStock() == null) {
            bindingResult.rejectValue("stock", "error.stock",
                    messageSource.getMessage("commerce.createPack.validation.stock.notNull",
                            null, LocaleContextHolder.getLocale()));
        } else if (form.getStock() <= 0) {
            bindingResult.rejectValue("stock", "error.stock",
                    messageSource.getMessage("commerce.createPack.validation.stock.positive",
                            null, LocaleContextHolder.getLocale()));
        } else if (form.getStock() > 999) {
            bindingResult.rejectValue("stock", "error.stock",
                    messageSource.getMessage("commerce.createPack.validation.stock.max",
                            null, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * Validates auction-specific fields (initialPrice, endDate, endTime) when isAuction is true.
     */
    private void validateAuctionFields(final CreateOfferForm form, final BindingResult bindingResult) {
        if (form.getInitialPrice() == null) {
            bindingResult.rejectValue("initialPrice", "error.initialPrice",
                    messageSource.getMessage("commerce.createAuction.validation.initialPrice.notNull",
                            null, LocaleContextHolder.getLocale()));
        } else if (form.getInitialPrice() <= 0) {
            bindingResult.rejectValue("initialPrice", "error.initialPrice",
                    messageSource.getMessage("commerce.createAuction.validation.initialPrice.positive",
                            null, LocaleContextHolder.getLocale()));
        } else if (form.getOriginalPrice() != null && form.getInitialPrice() > form.getOriginalPrice()) {
            bindingResult.rejectValue("initialPrice", "error.initialPrice",
                    messageSource.getMessage("commerce.createAuction.validation.initialPrice.exceedsOriginal",
                            null, LocaleContextHolder.getLocale()));
        }

        if (form.getEndDate() == null || form.getEndDate().isBlank()) {
            bindingResult.rejectValue("endDate", "error.endDate",
                    messageSource.getMessage("commerce.createAuction.validation.endDate.notEmpty",
                            null, LocaleContextHolder.getLocale()));
        }
        if (form.getEndTime() == null || form.getEndTime().isBlank()) {
            bindingResult.rejectValue("endTime", "error.endTime",
                    messageSource.getMessage("commerce.createAuction.validation.endTime.notEmpty",
                            null, LocaleContextHolder.getLocale()));
        }

        // Validate endDateTime if both date and time are present
        if (form.getEndDate() != null && !form.getEndDate().isBlank()
                && form.getEndTime() != null && !form.getEndTime().isBlank()) {
            try {
                LocalDateTime endDateTime = LocalDateTime.parse(form.getEndDate() + "T" + form.getEndTime());
                if (endDateTime.isBefore(LocalDateTime.now())) {
                    bindingResult.rejectValue("endDate", "error.endDate",
                            messageSource.getMessage("commerce.createAuction.validation.endDateTime.future",
                                    null, LocaleContextHolder.getLocale()));
                }
            } catch (Exception e) {
                bindingResult.rejectValue("endDate", "error.endDate",
                        messageSource.getMessage("commerce.createAuction.validation.endDateTime.invalid",
                                null, LocaleContextHolder.getLocale()));
            }
        }
    }

    /**
     * Server-side image type validation.
     */
    private void validateImage(final MultipartFile image, final BindingResult bindingResult) {
        if (image != null && !image.isEmpty()) {
            final String contentType = image.getContentType();
            if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
                bindingResult.rejectValue("image", "error.image.invalidType",
                        messageSource.getMessage("commerce.createPack.validation.image.invalidType",
                                null, LocaleContextHolder.getLocale()));
            }
        }

    }

    @RequestMapping(value = "/edit-pack/{packId}", method = RequestMethod.GET)
    public ModelAndView editPackForm(@PathVariable("packId") final long packId,
                                     @AuthenticationPrincipal final AuthUser principal,
                                     @ModelAttribute("editPackForm") final EditPackForm form,
                                     @RequestParam(value = "error", required = false) final String error) {
        final long commerceId = getAuthenticatedUser(principal).getId();

        if (commerceService.findByUserId(commerceId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        final Pack pack = getValidManageablePack(packId, commerceId, "commerce.editPack.error.auctionForbidden.editadas");

        if (form.getTitle() == null) {
            form.setTitle(pack.getTitle());
            form.setDescription(pack.getDescription());
            form.setTags(pack.getTags());
            form.setOriginalPrice(pack.getOriginalPrice());
            form.setFinalPrice(pack.getFinalPrice());
            form.setStock(pack.getStock());
        }

        final ModelAndView mav = new ModelAndView("commerce/editPack");
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
    public ModelAndView editPack(
            @PathVariable("packId") final long packId,
            @AuthenticationPrincipal final AuthUser principal,
            @Valid @ModelAttribute("editPackForm") final EditPackForm form,
            final BindingResult bindingResult,
            final RedirectAttributes redirectAttributes) {

        final long commerceId = getAuthenticatedUser(principal).getId();

        if (commerceService.findByUserId(commerceId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        final Pack packToUpdate = getValidManageablePack(packId, commerceId, "commerce.editPack.error.auctionForbidden.editadas");

        if (form.getOriginalPrice() != null && form.getFinalPrice() != null && form.getFinalPrice() > form.getOriginalPrice()) {
            bindingResult.rejectValue("finalPrice", "error.finalPrice", "El precio de venta no puede ser mayor al precio original");
        }

        final MultipartFile image = form.getImage();
        if (image != null && !image.isEmpty()) {
            final String contentType = image.getContentType();
            if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
                bindingResult.rejectValue("image", "error.image.invalidType",
                        messageSource.getMessage("commerce.createPack.validation.image.invalidType",
                                null, LocaleContextHolder.getLocale()));
            }
        }

        if (bindingResult.hasErrors()) {
            final ModelAndView mav = new ModelAndView("commerce/editPack");
            mav.addObject("commerceId", commerceId);
            mav.addObject("packId", packId);
            mav.addObject("availableTags", PackTag.values());
            return mav;
        }

        try {
            packToUpdate.setTitle(form.getTitle());
            packToUpdate.setDescription(form.getDescription());
            packToUpdate.setOriginalPrice(form.getOriginalPrice());
            packToUpdate.setFinalPrice(form.getFinalPrice());
            packToUpdate.setStock(form.getStock());
            packToUpdate.setTags(form.getTags() != null ? form.getTags() : Collections.emptyList());

            packService.update(packToUpdate);

            if (image != null && !image.isEmpty()) {
                packService.updateImage(packToUpdate.getId(), image.getBytes(), image.getContentType());
            }

            redirectAttributes.addFlashAttribute("dashboardAlertKind", "success");
            redirectAttributes.addFlashAttribute("dashboardAlertMessage", messageSource.getMessage("commerce.dashboard.success.edit", null, LocaleContextHolder.getLocale()));

            return new ModelAndView("redirect:/commerce");

        } catch (IllegalArgumentException e) {
            final ModelAndView mav = new ModelAndView("commerce/editPack");
            mav.addObject("commerceId", commerceId);
            mav.addObject("packId", packId);
            mav.addObject("availableTags", PackTag.values());
            mav.addObject("errorMessage", e.getMessage());
            return mav;
        } catch (IOException e) {
            final ModelAndView mav = new ModelAndView("commerce/editPack");
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
    public ModelAndView deletePack(
            @PathVariable("packId") final long packId,
            @AuthenticationPrincipal final AuthUser principal,
            final RedirectAttributes redirectAttributes) {

        final long commerceId = getAuthenticatedUser(principal).getId();

        if (commerceService.findByUserId(commerceId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        getValidManageablePack(packId, commerceId, "commerce.editPack.error.auctionForbidden.eliminadas");

        packService.deletePack(packId);

        redirectAttributes.addFlashAttribute("dashboardAlertKind", "success");
        redirectAttributes.addFlashAttribute("dashboardAlertMessage", messageSource.getMessage("commerce.dashboard.success.delete", null, LocaleContextHolder.getLocale()));

        return new ModelAndView("redirect:/commerce");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Devuelve el User del principal autenticado.
     * Si por algún motivo el principal es nulo lanza 401.
     */
    private User getAuthenticatedUser(final AuthUser principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return userService.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private Pack getValidManageablePack(final long packId, final long commerceId, final String forbiddenActionKey) {
        final java.util.Optional<Pack> packOpt = packService.findById(packId);
        if (!packOpt.isPresent() || packOpt.get().getCommerceId() != commerceId || Boolean.TRUE.equals(packOpt.get().getDeleted())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        if (auctionService.findByPackId(packId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                messageSource.getMessage(forbiddenActionKey, null, LocaleContextHolder.getLocale()));
        }
        return packOpt.get();
    }


}
