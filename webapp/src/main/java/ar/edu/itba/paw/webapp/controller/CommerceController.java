package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.models.Commerce;
import ar.edu.itba.paw.models.Client;
import ar.edu.itba.paw.models.Pack;
import ar.edu.itba.paw.models.PackTag;
import ar.edu.itba.paw.models.Reservation;
import ar.edu.itba.paw.services.AuctionService;
import ar.edu.itba.paw.services.ClientService;
import ar.edu.itba.paw.services.CommerceService;
import ar.edu.itba.paw.services.PackService;
import ar.edu.itba.paw.services.ReservationService;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.services.UserService;

import ar.edu.itba.paw.webapp.form.CreateOfferForm;

@Controller
@RequestMapping("/commerce")
public class CommerceController {

    private static final int PAGE_SIZE = 6;
    private static final Set<String> ALLOWED_IMAGE_TYPES = new HashSet<>(Arrays.asList(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    ));

    private final CommerceService commerceService;
    private final ClientService clientService;
    private final PackService packService;
    private final ReservationService reservationService;
    private final AuctionService auctionService;
    private final MessageSource messageSource;
    private final UserService userService;
    /** Same zone as commerce opening/closing / pack detail display ({@code app.display-zone}). */
    private final ZoneId businessZone;

    @Autowired
    public CommerceController(final CommerceService commerceService, final ClientService clientService,
                              final PackService packService, final ReservationService reservationService,
                              final AuctionService auctionService, final MessageSource messageSource,
                              final UserService userService,
                              @Value("${app.display-zone:}") final String displayZone) {
        this.commerceService = commerceService;
        this.clientService = clientService;
        this.packService = packService;
        this.reservationService = reservationService;
        this.auctionService = auctionService;
        this.messageSource = messageSource;
        this.userService = userService;
        this.businessZone = (displayZone == null || displayZone.trim().isEmpty())
                ? ZoneId.of("America/Argentina/Buenos_Aires")
                : ZoneId.of(displayZone.trim());
    }


    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView dashboard(@RequestParam(value = "page", defaultValue = "1") final int page) {
        final ModelAndView mav = new ModelAndView("commerce/dashboard");

        final List<Pack> allPacks = packService.findAll();
        final int totalPages = Math.max(1, (int) Math.ceil((double) allPacks.size() / PAGE_SIZE));
        final int safePage = Math.max(1, Math.min(page, totalPages));
        final int fromIdx = (safePage - 1) * PAGE_SIZE;
        final int toIdx = Math.min(fromIdx + PAGE_SIZE, allPacks.size());

        mav.addObject("packs", allPacks.subList(fromIdx, toIdx));
        mav.addObject("currentPage", safePage);
        mav.addObject("totalPages", totalPages);
        mav.addObject("paginationBaseUrl", "/commerce");
        return mav;
    }
    // ── Verify Pickup ────────────────────────────────────────

    @RequestMapping(value = "/verify-pickup", method = RequestMethod.GET)
    public ModelAndView verifyPickupForm() {
        return new ModelAndView("commerce/verify-pickup");
    }

    @RequestMapping(value = "/verify-pickup", method = RequestMethod.POST)
    public ModelAndView verifyPickupPost(@RequestParam(value = "pickupCode", required = false) final String pickupCode) {
        final ModelAndView mav = new ModelAndView("commerce/verify-pickup");

        try {
            final Commerce commerce = getAuthenticatedCommerce();
            final Reservation confirmed = reservationService.confirmPickupByCode(pickupCode, commerce.getUserId());

            mav.addObject("pickupSuccess", true);
            mav.addObject("confirmedReservation", confirmed);

            if (confirmed.getPackId() != null) {
                packService.findById(confirmed.getPackId())
                        .ifPresent(pack -> mav.addObject("confirmedPack", pack));
            }

            if (confirmed.getCustomerId() != null) {
                clientService.findByUserId(confirmed.getCustomerId())
                        .ifPresent(client -> {
                            final String firstName = client.getName() == null ? "" : client.getName().trim();
                            final String lastName = client.getLastName() == null ? "" : client.getLastName().trim();
                            final String fullName = (firstName + " " + lastName).trim();
                            mav.addObject("confirmedClientName", fullName.isEmpty() ? "-" : fullName);
                        });
            }

        } catch (final IllegalArgumentException ex) {
            final String key;
            switch (ex.getMessage()) {
                case "EMPTY":
                    key = "commerce.verifyPickup.error.empty";
                    break;
                case "NOT_FOUND":
                    key = "commerce.verifyPickup.error.notFound";
                    break;
                case "WRONG_COMMERCE":
                    key = "commerce.verifyPickup.error.wrongCommerce";
                    break;
                default:
                    key = "commerce.verifyPickup.error.notFound";
                    break;
            }
            mav.addObject("pickupError", key);
        } catch (final IllegalStateException ex) {
            final String key;
            switch (ex.getMessage()) {
                case "ALREADY_COMPLETED":
                    key = "commerce.verifyPickup.error.alreadyCompleted";
                    break;
                case "ALREADY_CANCELED":
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
            @Valid @ModelAttribute("createOfferForm") final CreateOfferForm form,
            final BindingResult bindingResult) {

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
            final Commerce commerce = getAuthenticatedCommerce();

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
                        commerce.getUserId(), form.getTitle(), form.getDescription(),
                        form.getOriginalPrice(), form.getInitialPrice(), 1,
                        form.getTags() != null ? form.getTags() : Collections.emptyList(),
                        imageData, imageContentType
                );

                // Wall-clock in business zone → UTC (same convention as auction storage / display)
                final LocalDateTime endUtc = parseAuctionEndAsUtc(form.getEndDate(), form.getEndTime());

                // Create the auction wrapping the pack
                auctionService.createAuction(pack.getId(), form.getInitialPrice(), endUtc);

            } else {
                packService.createPack(commerce.getUserId(), form.getTitle(), form.getDescription(),
                                       form.getOriginalPrice(), form.getFinalPrice(), form.getStock(),
                                       form.getTags() != null ? form.getTags() : Collections.emptyList(),
                                       imageData, imageContentType);
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
     * Retrieves the Commerce entity for the currently authenticated user.
     */
    private Commerce getAuthenticatedCommerce() {
        final String email = SecurityContextHolder.getContext().getAuthentication().getName();
        final User user = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));
        return commerceService.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("Commerce not found for user: " + user.getId()));
    }

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

        // Validate end instant if both date and time are present (interpreted in business zone)
        if (form.getEndDate() != null && !form.getEndDate().isBlank()
                && form.getEndTime() != null && !form.getEndTime().isBlank()) {
            try {
                final LocalDate date = LocalDate.parse(form.getEndDate().trim());
                final LocalTime time = LocalTime.parse(form.getEndTime().trim());
                final ZonedDateTime endZoned = ZonedDateTime.of(date, time, businessZone);
                if (!endZoned.toInstant().isAfter(Instant.now())) {
                    bindingResult.rejectValue("endDate", "error.endDate",
                            messageSource.getMessage("commerce.createAuction.validation.endDateTime.future",
                                    null, LocaleContextHolder.getLocale()));
                }
            } catch (DateTimeParseException e) {
                bindingResult.rejectValue("endDate", "error.endDate",
                        messageSource.getMessage("commerce.createAuction.validation.endDateTime.invalid",
                                null, LocaleContextHolder.getLocale()));
            }
        }
    }

    /**
     * HTML {@code date}/{@code time} fields are wall-clock in the business display zone (see
     * {@link #businessZone}), matching commerce opening/closing semantics; persisted end is UTC.
     */
    private LocalDateTime parseAuctionEndAsUtc(final String endDate, final String endTime) {
        final LocalDate date = LocalDate.parse(endDate.trim());
        final LocalTime time = LocalTime.parse(endTime.trim());
        return ZonedDateTime.of(date, time, businessZone)
                .withZoneSameInstant(ZoneOffset.UTC)
                .toLocalDateTime();
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
}
