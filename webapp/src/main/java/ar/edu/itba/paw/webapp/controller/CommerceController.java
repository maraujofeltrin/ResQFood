package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.pack.PackTag;
import ar.edu.itba.paw.models.reservation.PickupByCodeError;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.services.auction.AuctionService;
import ar.edu.itba.paw.services.user.ClientService;
import ar.edu.itba.paw.services.commerce.CommerceOfferService;
import ar.edu.itba.paw.services.commerce.CommercePackAccess;
import ar.edu.itba.paw.services.commerce.CommerceService;
import ar.edu.itba.paw.services.pack.PackService;
import ar.edu.itba.paw.services.reservation.PickupByCodeResult;
import ar.edu.itba.paw.services.reservation.ReservationService;
import ar.edu.itba.paw.services.user.UserService;
import ar.edu.itba.paw.webapp.auth.AuthUser;
import ar.edu.itba.paw.webapp.controller.utils.AuthenticatedUserResolver;
import ar.edu.itba.paw.webapp.form.CreateOfferForm;
import ar.edu.itba.paw.webapp.validation.CreateOfferFormValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/commerce")
public class CommerceController {

    private static final int PAGE_SIZE = 6;

    private final CommerceService commerceService;
    private final ClientService clientService;
    private final PackService packService;
    private final ReservationService reservationService;
    private final AuctionService auctionService;
    private final MessageSource messageSource;
    private final UserService userService;
    private final CommerceOfferService commerceOfferService;
    private final CreateOfferFormValidator createOfferFormValidator;
    private final AuthenticatedUserResolver authResolver;

    @Autowired
    public CommerceController(final CommerceService commerceService, final ClientService clientService,
                              final PackService packService, final ReservationService reservationService,
                              final AuctionService auctionService, final MessageSource messageSource,
                              final UserService userService, final CommerceOfferService commerceOfferService,
                              final CreateOfferFormValidator createOfferFormValidator,
                              final AuthenticatedUserResolver authResolver) {
        this.commerceService = commerceService;
        this.clientService = clientService;
        this.packService = packService;
        this.reservationService = reservationService;
        this.auctionService = auctionService;
        this.messageSource = messageSource;
        this.userService = userService;
        this.commerceOfferService = commerceOfferService;
        this.createOfferFormValidator = createOfferFormValidator;
        this.authResolver = authResolver;
    }

    @GetMapping(value = "")
    public ModelAndView dashboard(@AuthenticationPrincipal final AuthUser principal,
            @RequestParam(value = "page", defaultValue = "1") final int page,
            @RequestParam(value = "tab", defaultValue = "items") final String tab) {
        final long id = authResolver.resolveUser(principal).getId();

        final java.util.Optional<Commerce> commerceOpt = commerceService.findByUserId(id);
        if (!commerceOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        final Commerce commerce = commerceOpt.get();
        final ModelAndView mav = new ModelAndView("commerce/dashboard");

        final List<Pack> allPacks = packService.findByCommerceId(id);
        final List<ar.edu.itba.paw.models.auction.Auction> commerceAuctions = auctionService.findByCommerceId(id);
        final Set<Long> auctionPackIds = commerceAuctions.stream()
                .map(a -> a.getPack().getId())
                .collect(Collectors.toSet());

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

        final int itemsCount = allPacks.size();
        final int auctionsCount = (int) allPacks.stream().filter(p -> auctionPackIds.contains(p.getId())).count();
        final int packsCount = itemsCount - auctionsCount;

        mav.addObject("commerce", commerce);
        mav.addObject("packs", displayedPacks.subList(fromIdx, toIdx));
        mav.addObject("currentPage", safePage);
        mav.addObject("totalPages", totalPages);
        mav.addObject("commerceId", id);
        mav.addObject("auctionPackIds", auctionPackIds);
        mav.addObject("currentTab", tab);
        mav.addObject("paginationBaseUrl", "/commerce?tab=" + tab);
        mav.addObject("itemsCount", itemsCount);
        mav.addObject("packsCount", packsCount);
        mav.addObject("auctionsCount", auctionsCount);
        return mav;
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

    @RequestMapping(value = "/edit-pack/{packId}", method = RequestMethod.GET)
    public ModelAndView editPackForm(@PathVariable("packId") final long packId,
            @AuthenticationPrincipal final AuthUser principal,
            @ModelAttribute("createOfferForm") final CreateOfferForm form,
            @RequestParam(value = "error", required = false) final String error) {
        final long commerceId = authResolver.resolveUser(principal).getId();

        if (commerceService.findByUserId(commerceId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        final Pack pack = resolveEditPack(packId, commerceId, "commerce.editPack.error.auctionForbidden.editadas");

        if (form.getTitle() == null) {
            form.setTitle(pack.getTitle());
            form.setDescription(pack.getDescription());
            form.setTags(pack.getTags());
            form.setOriginalPrice(pack.getOriginalPrice());
            form.setFinalPrice(pack.getFinalPrice());
            form.setStock(pack.getStock());
            form.setIsAuction(false);
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

        final Pack packToUpdate = resolveEditPack(packId, commerceId,
                "commerce.editPack.error.auctionForbidden.editadas");

        if (bindingResult.hasErrors()) {
            final ModelAndView mav = new ModelAndView("commerce/editPack");
            mav.addObject("editMode", true);
            mav.addObject("commerceId", commerceId);
            mav.addObject("packId", packId);
            mav.addObject("availableTags", PackTag.values());
            return mav;
        }

        try {
            final MultipartFile image = form.getImage();
            byte[] imgData = null;
            String imgType = null;
            if (image != null && !image.isEmpty()) {
                imgData = image.getBytes();
                imgType = image.getContentType();
            }

            packService.updatePack(
                    packToUpdate.getId(),
                    form.getTitle(),
                    form.getDescription(),
                    form.getOriginalPrice(),
                    form.getFinalPrice(),
                    form.getStock(),
                    form.getTags(),
                    imgData,
                    imgType
            );

            redirectAttributes.addFlashAttribute("dashboardAlertKind", "success");
            redirectAttributes.addFlashAttribute("dashboardAlertMessage",
                    messageSource.getMessage("commerce.dashboard.success.edit", null, LocaleContextHolder.getLocale()));

            return new ModelAndView("redirect:/commerce");

        } catch (IllegalArgumentException e) {
            final ModelAndView mav = new ModelAndView("commerce/editPack");
            mav.addObject("editMode", true);
            mav.addObject("commerceId", commerceId);
            mav.addObject("packId", packId);
            mav.addObject("availableTags", PackTag.values());
            mav.addObject("errorMessage", e.getMessage());
            return mav;
        } catch (IOException e) {
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
    public ModelAndView deletePack(
            @PathVariable("packId") final long packId,
            @AuthenticationPrincipal final AuthUser principal,
            final RedirectAttributes redirectAttributes) {

        final long commerceId = authResolver.resolveUser(principal).getId();

        if (commerceService.findByUserId(commerceId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        resolveEditPack(packId, commerceId, "commerce.editPack.error.auctionForbidden.eliminadas");
        packService.deletePack(packId);

        redirectAttributes.addFlashAttribute("dashboardAlertKind", "success");
        redirectAttributes.addFlashAttribute("dashboardAlertMessage",
                messageSource.getMessage("commerce.dashboard.success.delete", null, LocaleContextHolder.getLocale()));

        return new ModelAndView("redirect:/commerce");
    }



    private Pack resolveEditPack(final long packId, final long commerceId, final String forbiddenActionKey) {
        final CommercePackAccess access = packService.resolvePackForDirectEdit(packId, commerceId);
        if (access instanceof CommercePackAccess.NotFound) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        if (access instanceof CommercePackAccess.ForbiddenAuction) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    messageSource.getMessage(forbiddenActionKey, null, LocaleContextHolder.getLocale()));
        }
        return ((CommercePackAccess.Granted) access).pack();
    }
}
