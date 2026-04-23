package ar.edu.itba.paw.webapp.controller;

import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.servlet.ServletContext;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.dao.DataIntegrityViolationException;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.auction.AuctionSortOption;
import ar.edu.itba.paw.models.auction.BidPlacementException;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.pack.PackSortOption;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.services.auction.AuctionService;
import ar.edu.itba.paw.services.commerce.CommerceService;
import ar.edu.itba.paw.services.pack.DirectReservationCheck;
import ar.edu.itba.paw.services.pack.PackService;
import ar.edu.itba.paw.services.reservation.ReservationService;
import ar.edu.itba.paw.services.user.UserService;
import ar.edu.itba.paw.webapp.form.BidForm;
import ar.edu.itba.paw.webapp.form.ReservationForm;
import ar.edu.itba.paw.webapp.controller.utils.PackCatalogUtils;

@Controller
public class PackController {

    private final ReservationService reservationService;
    private final PackService packService;
    private final CommerceService commerceService;
    private final AuctionService auctionService;
    private final UserService userService;
    private final ServletContext servletContext;
    private final MessageSource messageSource;
    private final ZoneId businessZone;
    private final PackCatalogUtils packCatalogUtils;

    private byte[] placeholderBytes;
    private String placeholderContentType;

    @Autowired
    public PackController(final ReservationService reservationService, final PackService packService,
            final CommerceService commerceService, final AuctionService auctionService,
            final UserService userService,
            final ServletContext servletContext,
            final MessageSource messageSource,
            final ZoneId businessZone,
            final PackCatalogUtils packCatalogUtils) {
        this.reservationService = reservationService;
        this.packService = packService;
        this.commerceService = commerceService;
        this.auctionService = auctionService;
        this.userService = userService;
        this.servletContext = servletContext;
        this.messageSource = messageSource;
        this.businessZone = businessZone;
        this.packCatalogUtils = packCatalogUtils;
    }

    private synchronized byte[] getPlaceholderBytes() {
        if (placeholderBytes == null) {
            try (InputStream is = servletContext.getResourceAsStream("/images/pack-placeholder.svg")) {
                if (is != null) {
                    placeholderBytes = is.readAllBytes();
                    placeholderContentType = "image/svg+xml";
                }
            } catch (IOException ignored) {
            }
            if (placeholderBytes == null) {
                placeholderBytes = new byte[0];
                placeholderContentType = "application/octet-stream";
            }
        }
        return placeholderBytes;
    }

    private static final Locale LOCALE_AR = new Locale("es", "AR");

    private static String formatPrice(final Double amount) {
        if (amount == null) {
            return "—";
        }
        return NumberFormat.getCurrencyInstance(LOCALE_AR).format(amount);
    }

    private static String dashIfBlank(final String value) {
        if (value == null || value.isBlank()) {
            return "—";
        }
        return value.trim();
    }

    private static String formatStreetLine(final Commerce commerce) {
        if (commerce == null) {
            return "—";
        }
        final String street = commerce.getStreet();
        final Integer number = commerce.getStreetNumber();
        final boolean hasStreet = street != null && !street.isBlank();
        final boolean hasNumber = number != null;
        if (!hasStreet && !hasNumber) {
            return "—";
        }
        if (hasStreet && hasNumber) {
            return street.trim() + " " + number;
        }
        if (hasStreet) {
            return street.trim();
        }
        return String.valueOf(number);
    }

    private static String formatCityProvincePostal(final Commerce commerce) {
        if (commerce == null) {
            return "—";
        }
        final List<String> parts = new ArrayList<>(3);
        if (commerce.getCity() != null && !commerce.getCity().isBlank()) {
            parts.add(commerce.getCity().trim());
        }
        if (commerce.getProvince() != null && !commerce.getProvince().isBlank()) {
            parts.add(commerce.getProvince().trim());
        }
        if (commerce.getPostalCode() != null && !commerce.getPostalCode().isBlank()) {
            parts.add(commerce.getPostalCode().trim());
        }
        return parts.isEmpty() ? "—" : String.join(", ", parts);
    }

    private static Optional<LocalTime> parseFlexibleTime(final String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        final String v = value.trim();
        final DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("h:mm a", Locale.US),
                DateTimeFormatter.ofPattern("hh:mm a", Locale.US),
                DateTimeFormatter.ofPattern("H:mm", Locale.US),
                DateTimeFormatter.ofPattern("HH:mm", Locale.US),
                DateTimeFormatter.ofPattern("H:mm:ss", Locale.US),
                DateTimeFormatter.ofPattern("HH:mm:ss", Locale.US),
        };
        for (final DateTimeFormatter formatter : formatters) {
            try {
                return Optional.of(LocalTime.parse(v, formatter));
            } catch (final DateTimeParseException ignored) {
                // try next pattern
            }
        }
        return Optional.empty();
    }

    private static boolean computeOpenNow(final Commerce commerce, final ZoneId zone) {
        if (commerce == null) {
            return false;
        }
        final Optional<LocalTime> open = parseFlexibleTime(commerce.getOpeningTime());
        final Optional<LocalTime> close = parseFlexibleTime(commerce.getClosingTime());
        if (open.isEmpty() || close.isEmpty()) {
            return false;
        }
        final LocalTime o = open.get();
        final LocalTime c = close.get();
        if (o.equals(c)) {
            return false;
        }
        final LocalTime now = LocalTime.now(zone);
        if (!c.isBefore(o)) {
            return !now.isBefore(o) && !now.isAfter(c);
        }
        return !now.isBefore(o) || !now.isAfter(c);
    }

    private void addCommerceDetailAttributes(final ModelAndView mav, final Optional<Commerce> commerceOpt) {
        final Commerce commerce = commerceOpt.orElse(null);

        final String commercialName = commerce != null && commerce.getCommercialName() != null
                && !commerce.getCommercialName().isBlank()
                        ? commerce.getCommercialName().trim()
                        : "—";
        mav.addObject("commerceCommercialName", commercialName);
        mav.addObject("commerceStreetLine", formatStreetLine(commerce));
        mav.addObject("commerceLocationLine", formatCityProvincePostal(commerce));
        mav.addObject("commerceOpeningTime", commerce != null ? dashIfBlank(commerce.getOpeningTime()) : "—");
        mav.addObject("commerceClosingTime", commerce != null ? dashIfBlank(commerce.getClosingTime()) : "—");
        mav.addObject("commerceOpenNow", Boolean.valueOf(computeOpenNow(commerce, businessZone)));
    }

    private ReservationForm createDefaultReservationForm() {
        final ReservationForm form = new ReservationForm();
        form.setQuantity(Integer.valueOf(1));
        return form;
    }

    private BidForm createDefaultBidForm() {
        return new BidForm();
    }

    private String formatAuctionEndForDisplay(final LocalDateTime endUtc, final Locale locale) {
        final ZonedDateTime z = endUtc.atZone(ZoneOffset.UTC).withZoneSameInstant(businessZone);
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(locale).format(z);
    }

    private ModelAndView buildPackDetailModel(final Pack pack, final ReservationForm reservationForm,
            final BidForm bidForm) {
        final Optional<Commerce> commerceOpt = commerceService.findByUserId(pack.getCommerceId());
        final Locale locale = LocaleContextHolder.getLocale();

        final String title = pack.getTitle() != null && !pack.getTitle().isBlank()
                ? pack.getTitle()
                : messageSource.getMessage("pack.detail.defaultTitle", null, locale);

        final String brand = messageSource.getMessage("app.brand", null, locale);
        final String pageTitle = messageSource.getMessage("pack.detail.pageTitle",
                new Object[] { title, brand }, locale);

        final ModelAndView mav = new ModelAndView("packs/packDetailView");
        mav.addObject("packId", pack.getId());

        final Optional<Auction> auctionOpt = auctionService.findByPackId(pack.getId());
        final boolean auctionPresent = auctionOpt.isPresent();
        final boolean auctionActive = auctionOpt.map(Auction::isActive).orElse(false);
        mav.addObject("auctionPresent", Boolean.valueOf(auctionPresent));
        mav.addObject("auctionActive", Boolean.valueOf(auctionActive));

        boolean auctionClientIsLeading = false;
        if (auctionOpt.isPresent()) {
            final Auction auction = auctionOpt.get();
            mav.addObject("auction", auction);
            final double effective = auction.getEffectivePrice() != null ? auction.getEffectivePrice() : 0d;
            final double minimumBidAmount = effective + auctionService.getMinBidIncrementArs();
            mav.addObject("auctionEffectiveAmount", effective);
            mav.addObject("auctionEffectivePriceDisplay", formatPrice(effective));
            mav.addObject("auctionEndDisplay", formatAuctionEndForDisplay(auction.getEndTime(), locale));
            mav.addObject("auctionMinBidHint",
                    messageSource.getMessage("pack.detail.bid.minHint",
                            new Object[] { formatPrice(effective), formatPrice(minimumBidAmount) },
                            locale));
            if (auctionActive) {
                mav.addObject("bidAmountMin", String.format(Locale.US, "%.2f", minimumBidAmount));
                if (auction.getCurrentBidderId() != null) {
                    final org.springframework.security.core.Authentication auth =
                            SecurityContextHolder.getContext().getAuthentication();
                    if (auth != null && auth.isAuthenticated()) {
                        final Object principal = auth.getPrincipal();
                        if (principal != null && !"anonymousUser".equals(principal)) {
                            final Optional<User> bidderUserOpt = userService.findByEmail(auth.getName());
                            if (bidderUserOpt.isPresent() && bidderUserOpt.get().getRole() == User.Role.CLIENT
                                    && bidderUserOpt.get().getId().equals(auction.getCurrentBidderId())) {
                                auctionClientIsLeading = true;
                            }
                        }
                    }
                }
            }
            mav.addObject("auctionClientIsLeading", Boolean.valueOf(auctionClientIsLeading));
        } else {
            mav.addObject("auctionClientIsLeading", Boolean.FALSE);
        }

        final double unitPriceAmount;
        if (auctionActive && auctionOpt.isPresent()) {
            final Auction auction = auctionOpt.get();
            final Double eff = auction.getEffectivePrice();
            unitPriceAmount = eff != null ? eff : 0d;
        } else {
            unitPriceAmount = pack.getFinalPrice() != null ? pack.getFinalPrice() : 0d;
        }
        mav.addObject("unitPriceAmount", unitPriceAmount);
        mav.addObject("unitPriceNumber", String.format(Locale.US, "%.2f", unitPriceAmount));
        mav.addObject("pageTitle", pageTitle);
        mav.addObject("packTitle", title);
        mav.addObject("packDescription", pack.getDescription() != null ? pack.getDescription() : "");
        addCommerceDetailAttributes(mav, commerceOpt);
        mav.addObject("originalPrice", formatPrice(pack.getOriginalPrice()));
        mav.addObject("finalPrice", formatPrice(pack.getFinalPrice()));
        final Integer stock = pack.getStock();
        final int quantityMax;
        if (stock != null && stock >= 1) {
            quantityMax = Math.min(stock, 999);
        } else if (stock != null) {
            quantityMax = 0;
        } else {
            quantityMax = 999;
        }
        mav.addObject("quantityMax", quantityMax);

        if (stock != null) {
            mav.addObject("packStock", stock);
            mav.addObject("packStockBadgeCssClass",
                    stock.intValue() > 0 ? "pack-detail-badge--stock-available" : "pack-detail-badge--stock-unavailable");
            mav.addObject("packStockBadgeText",
                    messageSource.getMessage("pack.detail.stockBadge", new Object[] { stock }, locale));
        }

        if (stock != null && stock >= 1 && reservationForm.getQuantity() != null
                && reservationForm.getQuantity().intValue() > stock.intValue()) {
            reservationForm.setQuantity(stock);
        }

        mav.addObject("reservationForm", reservationForm);
        mav.addObject("bidForm", bidForm);
        return mav;
    }

    @GetMapping("/packs")
    public ModelAndView listPacks(
            @RequestParam(value = "q", required = false) final String query,
            @RequestParam(value = "tags", required = false) final List<String> tagNames,
            @RequestParam(value = "sort", required = false) final String sort,
            @RequestParam(value = "types", required = false) final List<String> types,
            @RequestParam(value = "auctionSort", required = false) final String auctionSort,
            @RequestParam(value = "location", required = false) final String locationParam,
            @RequestParam(value = "timeRange", required = false) final List<String> timeRange,
            @RequestParam(value = "page", defaultValue = "1") final int page) {
        return packCatalogUtils.buildPackCatalog(query, tagNames, sort, types, auctionSort, locationParam,
                timeRange, page);
    }

    @GetMapping("/packs/{id}")
    public ModelAndView packDetail(@PathVariable("id") final long id) {
        final Pack pack = packService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // Inactive packs are only visible to their owning commerce
        if (!Boolean.TRUE.equals(pack.getActive())) {
            final org.springframework.security.core.Authentication auth =
                    SecurityContextHolder.getContext().getAuthentication();
            boolean isOwner = false;
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                final Optional<User> userOpt = userService.findByEmail(auth.getName());
                if (userOpt.isPresent() && userOpt.get().getId().equals(pack.getCommerceId())) {
                    isOwner = true;
                }
            }
            if (!isOwner) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
        }

        return buildPackDetailModel(pack, createDefaultReservationForm(), createDefaultBidForm());
    }

    @GetMapping("/packs/{id}/image")
    @ResponseBody
    public ResponseEntity<byte[]> packImage(@PathVariable("id") final long id) {
        final Optional<Pack> packOpt = packService.findImageByPackId(id);
        if (packOpt.isPresent()) {
            final Pack pack = packOpt.get();
            if (pack.getImageData() != null && pack.getImageData().length > 0) {
                String contentType = pack.getImageContentType() != null
                        ? pack.getImageContentType() : "application/octet-stream";
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(pack.getImageData());
            }
        }
        final byte[] placeholder = getPlaceholderBytes();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(placeholderContentType))
                .body(placeholder);
    }

    @PostMapping("/packs/{packId}/reserve")
    public ModelAndView submitReservation(
            @PathVariable("packId") final long packId,
            @Valid @ModelAttribute("reservationForm") final ReservationForm reservationForm,
            final BindingResult bindingResult,
            final RedirectAttributes redirectAttributes) {
        final ModelAndView redirectView = new ModelAndView("redirect:/packs/" + packId);

        if (bindingResult.hasErrors()) {
            return packService.findById(packId)
                    .map(pack -> buildPackDetailModel(pack, reservationForm, createDefaultBidForm()))
                    .orElse(redirectView);
        }

        final int quantity = reservationForm.getQuantity().intValue();
        final DirectReservationCheck check = reservationService.checkDirectPackReservation(packId, quantity);

        switch (check.getOutcome()) {
        case OK:
            break;
        case QUANTITY_EXCEEDS_STOCK: {
            final Pack p = check.getPack().orElse(null);
            if (p == null) {
                return redirectView;
            }
            final int stock = check.getAvailableStock().orElse(0);
            bindingResult.rejectValue("quantity", "reservation.quantity.exceedsStock", new Object[] { stock }, null);
            return buildPackDetailModel(p, reservationForm, createDefaultBidForm());
        }
        case AUCTION_ACTIVE: {
            final Optional<Auction> auctionForReserve = auctionService.findByPackId(packId);
            final boolean active = auctionForReserve.map(Auction::isActive).orElse(false);
            redirectAttributes.addFlashAttribute("reservationAlertKind", "error");
            redirectAttributes.addFlashAttribute("reservationAlertMessage", messageSource.getMessage(
                    active ? "reservation.alert.activeAuction" : "reservation.alert.auctionEndedNoDirectSale",
                    null, LocaleContextHolder.getLocale()));
            return redirectView;
        }
        case AUCTION_ENDED_NO_DIRECT:
            redirectAttributes.addFlashAttribute("reservationAlertKind", "error");
            redirectAttributes.addFlashAttribute("reservationAlertMessage",
                    messageSource.getMessage("reservation.alert.packUnavailable", null,
                            LocaleContextHolder.getLocale()));
            return redirectView;
        case PACK_UNAVAILABLE:
            redirectAttributes.addFlashAttribute("reservationAlertKind", "error");
            redirectAttributes.addFlashAttribute("reservationAlertMessage",
                    messageSource.getMessage("reservation.alert.packUnavailable", null,
                            LocaleContextHolder.getLocale()));
            return redirectView;
        case MISSING_FINAL_PRICE:
        default:
            redirectAttributes.addFlashAttribute("reservationAlertKind", "error");
            redirectAttributes.addFlashAttribute("reservationAlertMessage",
                    messageSource.getMessage("reservation.alert.genericError", null,
                            LocaleContextHolder.getLocale()));
            return redirectView;
        }

        final double finalPrice = check.getUnitPrice();
        final String appBaseUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
            .build()
            .toUriString();
        final String username = SecurityContextHolder.getContext().getAuthentication().getName();
        final User authenticatedUser = userService.findByEmail(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        try {
            reservationService.createReservation(
                    packId,
                    authenticatedUser.getId(),
                    quantity,
                    finalPrice,
                    null,
                    appBaseUrl);
            redirectAttributes.addFlashAttribute("reservationAlertKind", "success");
            redirectAttributes.addFlashAttribute("reservationAlertMessage",
                    messageSource.getMessage("reservation.alert.success", null,
                            LocaleContextHolder.getLocale()));
        } catch (final Exception ex) {
            redirectAttributes.addFlashAttribute("reservationAlertKind", "error");
            redirectAttributes.addFlashAttribute("reservationAlertMessage",
                    messageSource.getMessage("reservation.alert.genericError", null,
                            LocaleContextHolder.getLocale()));
        }
        return redirectView;
    }

    @PostMapping("/packs/{packId}/bid")
    public ModelAndView submitBid(
            @PathVariable("packId") final long packId,
            @Valid @ModelAttribute("bidForm") final BidForm bidForm,
            final BindingResult bindingResult,
            final RedirectAttributes redirectAttributes) {
        final Locale locale = LocaleContextHolder.getLocale();
        final ModelAndView redirectView = new ModelAndView("redirect:/packs/" + packId);

        final Optional<Pack> packOpt = packService.findById(packId)
                .filter(p -> Boolean.TRUE.equals(p.getActive()));
        if (packOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("auctionAlertKind", "error");
            redirectAttributes.addFlashAttribute("auctionAlertMessage",
                    messageSource.getMessage("pack.detail.bid.alert.packUnavailable", null, locale));
            return redirectView;
        }
        final Pack pack = packOpt.get();

        final Optional<Auction> auctionOpt = auctionService.findByPackId(packId);
        if (auctionOpt.isEmpty() || !auctionOpt.get().isActive()) {
            redirectAttributes.addFlashAttribute("auctionAlertKind", "error");
            redirectAttributes.addFlashAttribute("auctionAlertMessage",
                    messageSource.getMessage("pack.detail.bid.alert.auctionNotActive", null, locale));
            return redirectView;
        }
        final Auction auction = auctionOpt.get();

        if (bindingResult.hasErrors()) {
            return buildPackDetailModel(pack, createDefaultReservationForm(), bidForm);
        }

        final String email = SecurityContextHolder.getContext().getAuthentication().getName();
        final Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty() || userOpt.get().getRole() != User.Role.CLIENT) {
            redirectAttributes.addFlashAttribute("auctionAlertKind", "error");
            redirectAttributes.addFlashAttribute("auctionAlertMessage",
                    messageSource.getMessage("pack.detail.bid.alert.roleNotClient", null, locale));
            return redirectView;
        }
        final User user = userOpt.get();

        final double amount = bidForm.getAmount().doubleValue();
        try {
            auctionService.placeBid(auction.getId(), user.getId(), amount);
            redirectAttributes.addFlashAttribute("auctionAlertKind", "success");
            redirectAttributes.addFlashAttribute("auctionAlertMessage",
                    messageSource.getMessage("pack.detail.bid.alert.success", null, locale));
        } catch (final BidPlacementException ex) {
            redirectAttributes.addFlashAttribute("auctionAlertKind", "error");
            final String code;
            switch (ex.getReason()) {
            case OWN_COMMERCE:
                code = "pack.detail.bid.alert.ownCommerce";
                break;
            case ALREADY_LEADING:
                code = "pack.detail.bid.alert.alreadyLeading";
                break;
            case AMOUNT_BELOW_MINIMUM:
                code = "pack.detail.bid.alert.belowIncrement";
                break;
            case EXPIRED:
                code = "pack.detail.bid.alert.auctionExpired";
                break;
            case NOT_ACTIVE:
            case AUCTION_NOT_FOUND:
                code = "pack.detail.bid.alert.auctionNotActive";
                break;
            default:
                code = "pack.detail.bid.alert.reject";
            }
            redirectAttributes.addFlashAttribute("auctionAlertMessage",
                    messageSource.getMessage(code, null, locale));
        } catch (final DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("auctionAlertKind", "error");
            redirectAttributes.addFlashAttribute("auctionAlertMessage",
                    messageSource.getMessage("pack.detail.bid.alert.genericError", null, locale));
        } catch (final Exception ex) {
            redirectAttributes.addFlashAttribute("auctionAlertKind", "error");
            redirectAttributes.addFlashAttribute("auctionAlertMessage",
                    messageSource.getMessage("pack.detail.bid.alert.genericError", null, locale));
        }
        return redirectView;
    }
}
