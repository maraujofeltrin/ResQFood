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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import ar.edu.itba.paw.models.Auction;
import ar.edu.itba.paw.models.AuctionSortOption;
import ar.edu.itba.paw.models.Commerce;
import ar.edu.itba.paw.models.Pack;
import ar.edu.itba.paw.models.PackTag;
import ar.edu.itba.paw.models.PackSortOption;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.services.AuctionService;
import ar.edu.itba.paw.services.CommerceService;
import ar.edu.itba.paw.services.PackService;
import ar.edu.itba.paw.services.ReservationService;
import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.webapp.form.BidForm;
import ar.edu.itba.paw.webapp.form.ReservationForm;

@Controller
public class PackController {

    /** Kept in sync with {@code AuctionServiceImpl}. */
    private static final double AUCTION_MIN_BID_INCREMENT_ARS = 500.0;

    private final ReservationService reservationService;
    private final PackService packService;
    private final CommerceService commerceService;
    private final AuctionService auctionService;
    private final UserService userService;
    private final ServletContext servletContext;
    private final MessageSource messageSource;
    private final ZoneId businessZone;

    private byte[] placeholderBytes;
    private String placeholderContentType;

    @Autowired
    public PackController(final ReservationService reservationService, final PackService packService,
            final CommerceService commerceService, final AuctionService auctionService,
            final UserService userService,
            final ServletContext servletContext,
            final MessageSource messageSource,
            @Value("${app.display-zone:}") final String displayZone) {
        this.reservationService = reservationService;
        this.packService = packService;
        this.commerceService = commerceService;
        this.auctionService = auctionService;
        this.userService = userService;
        this.servletContext = servletContext;
        this.messageSource = messageSource;
        this.businessZone = (displayZone == null || displayZone.trim().isEmpty())
                ? ZoneId.of("America/Argentina/Buenos_Aires")
                : ZoneId.of(displayZone.trim());
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
            final double minimumBidAmount = effective + AUCTION_MIN_BID_INCREMENT_ARS;
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

    private static final int PAGE_SIZE = 6;
    private static final int AUCTION_CAROUSEL_SIZE = 6;
    private static final String TYPE_PACKS = "packs";
    private static final String TYPE_AUCTIONS = "auctions";

    private enum CatalogMode {
        ALL,
        PACKS,
        AUCTIONS
    }

    private static List<String> normalizeTypes(final List<String> rawTypes) {
        if (rawTypes == null || rawTypes.isEmpty()) {
            return Collections.emptyList();
        }
        final Set<String> values = new LinkedHashSet<>();
        for (final String raw : rawTypes) {
            if (raw == null) {
                continue;
            }
            final String type = raw.trim().toLowerCase(Locale.ROOT);
            if (TYPE_PACKS.equals(type) || TYPE_AUCTIONS.equals(type)) {
                values.add(type);
            }
        }
        return new ArrayList<>(values);
    }

    private List<Auction> findCatalogAuctions(final boolean hasQuery, final boolean hasTags, final String query,
            final List<PackTag> selectedTags, final AuctionSortOption sortOption) {
        if (hasQuery && hasTags) {
            return auctionService.searchActiveWithTags(query, selectedTags, sortOption);
        }
        if (hasTags) {
            return auctionService.findActiveByTags(selectedTags, sortOption);
        }
        if (hasQuery) {
            return auctionService.searchActive(query, sortOption);
        }
        return auctionService.findActive(sortOption);
    }

    @GetMapping("/packs")
    public ModelAndView listPacks(
            @RequestParam(value = "q", required = false) final String query,
            @RequestParam(value = "tags", required = false) final List<String> tagNames,
            @RequestParam(value = "sort", required = false) final String sort,
            @RequestParam(value = "types", required = false) final List<String> types,
            @RequestParam(value = "auctionSort", required = false) final String auctionSort,
            @RequestParam(value = "page", defaultValue = "1") final int page) {

        final ModelAndView mav = new ModelAndView("packs/packCatalogView");
        final PackSortOption sortOption = PackSortOption.fromString(sort);
        final AuctionSortOption auctionSortOption = AuctionSortOption.fromString(auctionSort);

        final List<PackTag> selectedTags = new ArrayList<>();
        if (tagNames != null) {
            for (final String name : tagNames) {
                try {
                    selectedTags.add(PackTag.valueOf(name));
                } catch (final IllegalArgumentException ignored) {
                }
            }
        }

        final boolean hasQuery = query != null && !query.trim().isEmpty();
        final String trimmedQuery = hasQuery ? query.trim() : null;
        final boolean hasTags = !selectedTags.isEmpty();

        final List<String> selectedTypes = normalizeTypes(types);
        final boolean packsSelected = selectedTypes.contains(TYPE_PACKS);
        final boolean auctionsSelected = selectedTypes.contains(TYPE_AUCTIONS);

        final CatalogMode catalogMode;
        if ((packsSelected && auctionsSelected) || (!packsSelected && !auctionsSelected)) {
            catalogMode = CatalogMode.ALL;
        } else if (packsSelected) {
            catalogMode = CatalogMode.PACKS;
        } else {
            catalogMode = CatalogMode.AUCTIONS;
        }

        final boolean showPacks = catalogMode != CatalogMode.AUCTIONS;
        final boolean showAuctionsList = catalogMode == CatalogMode.AUCTIONS;
        final boolean showAuctionsCarousel = catalogMode == CatalogMode.ALL;

        List<Pack> allPacks = Collections.emptyList();
        if (showPacks) {
            if (hasQuery && hasTags) {
                allPacks = packService.searchPacksWithTags(trimmedQuery, selectedTags, sortOption);
            } else if (hasTags) {
                allPacks = packService.findActiveByTags(selectedTags, sortOption);
            } else if (hasQuery) {
                allPacks = packService.searchPacks(trimmedQuery, sortOption);
            } else {
                allPacks = packService.findActive(sortOption);
            }
        }

        List<Auction> allAuctions = Collections.emptyList();
        if (showAuctionsList) {
            allAuctions = findCatalogAuctions(hasQuery, hasTags, trimmedQuery, selectedTags, auctionSortOption);
        }

        List<Auction> carouselAuctions = Collections.emptyList();
        if (showAuctionsCarousel) {
            final List<Auction> sortedForCarousel = findCatalogAuctions(hasQuery, hasTags, trimmedQuery,
                    selectedTags, AuctionSortOption.TIME_REMAINING_ASC);
            final int carouselSize = Math.min(AUCTION_CAROUSEL_SIZE, sortedForCarousel.size());
            carouselAuctions = sortedForCarousel.subList(0, carouselSize);
        }

        final int totalItems = showAuctionsList ? allAuctions.size() : allPacks.size();
        final int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / PAGE_SIZE));
        final int safePage = Math.max(1, Math.min(page, totalPages));
        final int fromIdx = (safePage - 1) * PAGE_SIZE;
        final int toIdx = Math.min(fromIdx + PAGE_SIZE, totalItems);

        final List<Pack> packs = showAuctionsList ? Collections.emptyList() : allPacks.subList(fromIdx, toIdx);
        final List<Auction> auctions = showAuctionsList ? allAuctions.subList(fromIdx, toIdx) : Collections.emptyList();

        final Map<Long, String> commerceNames = new HashMap<>();
        for (final Pack pack : packs) {
            commerceNames.putIfAbsent(
                    pack.getId(),
                    commerceService.findByUserId(pack.getCommerceId())
                            .map(Commerce::getCommercialName)
                            .orElse("—"));
        }
        for (final Auction auctionEntity : auctions) {
            if (auctionEntity.getPack() == null) {
                continue;
            }
            final Pack auctionPack = auctionEntity.getPack();
            commerceNames.putIfAbsent(
                    auctionPack.getId(),
                    commerceService.findByUserId(auctionPack.getCommerceId())
                            .map(Commerce::getCommercialName)
                            .orElse("—"));
        }
        for (final Auction auctionEntity : carouselAuctions) {
            if (auctionEntity.getPack() == null) {
                continue;
            }
            final Pack auctionPack = auctionEntity.getPack();
            commerceNames.putIfAbsent(
                    auctionPack.getId(),
                    commerceService.findByUserId(auctionPack.getCommerceId())
                            .map(Commerce::getCommercialName)
                            .orElse("—"));
        }

        final StringBuilder baseUrlBuilder = new StringBuilder("/packs");
        boolean firstParam = true;
        if (hasQuery) {
            baseUrlBuilder.append(firstParam ? "?" : "&").append("q=")
                    .append(java.net.URLEncoder.encode(trimmedQuery, java.nio.charset.StandardCharsets.UTF_8));
            firstParam = false;
        }
        if (hasTags) {
            for (final PackTag tag : selectedTags) {
                baseUrlBuilder.append(firstParam ? "?" : "&").append("tags=").append(tag.name());
                firstParam = false;
            }
        }
        for (final String selectedType : selectedTypes) {
            baseUrlBuilder.append(firstParam ? "?" : "&").append("types=").append(selectedType);
            firstParam = false;
        }
        if (catalogMode != CatalogMode.AUCTIONS && sort != null && !sort.isBlank()) {
            baseUrlBuilder.append(firstParam ? "?" : "&").append("sort=").append(sortOption.name());
            firstParam = false;
        }
        if (catalogMode == CatalogMode.AUCTIONS) {
            baseUrlBuilder.append(firstParam ? "?" : "&").append("auctionSort=").append(auctionSortOption.name());
            firstParam = false;
        }

        final StringBuilder auctionsViewAllBuilder = new StringBuilder("/packs");
        boolean viewAllFirstParam = true;
        if (hasQuery) {
            auctionsViewAllBuilder.append(viewAllFirstParam ? "?" : "&").append("q=")
                    .append(java.net.URLEncoder.encode(trimmedQuery, java.nio.charset.StandardCharsets.UTF_8));
            viewAllFirstParam = false;
        }
        if (hasTags) {
            for (final PackTag tag : selectedTags) {
                auctionsViewAllBuilder.append(viewAllFirstParam ? "?" : "&").append("tags=").append(tag.name());
                viewAllFirstParam = false;
            }
        }
        auctionsViewAllBuilder.append(viewAllFirstParam ? "?" : "&").append("types=").append(TYPE_AUCTIONS);
        auctionsViewAllBuilder.append("&auctionSort=").append(auctionSortOption.name());

        mav.addObject("packs", packs);
        mav.addObject("auctions", auctions);
        mav.addObject("auctionsCarousel", carouselAuctions);
        mav.addObject("catalogMode", catalogMode.name());
        mav.addObject("commerceNames", commerceNames);
        mav.addObject("availableTags", PackTag.values());
        mav.addObject("selectedTags", selectedTags);
        mav.addObject("selectedTypes", selectedTypes);
        mav.addObject("availableSorts", PackSortOption.values());
        mav.addObject("currentSort", sortOption);
        mav.addObject("availableAuctionSorts", AuctionSortOption.values());
        mav.addObject("currentAuctionSort", auctionSortOption);
        mav.addObject("currentPage", safePage);
        mav.addObject("totalPages", totalPages);
        mav.addObject("paginationBaseUrl", baseUrlBuilder.toString());
        mav.addObject("auctionsViewAllUrl", auctionsViewAllBuilder.toString());
        return mav;
    }

    @GetMapping("/packs/{id}")
    public ModelAndView packDetail(@PathVariable("id") final long id) {
        final Pack pack = packService.findById(id)
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

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
        final Optional<Pack> packOpt = packService.findById(packId)
                .filter(p -> Boolean.TRUE.equals(p.getActive()));

        final ModelAndView redirectView = new ModelAndView("redirect:/packs/" + packId);

        if (packOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("reservationAlertKind", "error");
            redirectAttributes.addFlashAttribute("reservationAlertMessage",
                    messageSource.getMessage("reservation.alert.packUnavailable", null,
                            LocaleContextHolder.getLocale()));
            return redirectView;
        }

        final Pack pack = packOpt.get();
        final Optional<Auction> auctionForReserve = auctionService.findByPackId(packId);
        if (auctionForReserve.isPresent()) {
            final Auction a = auctionForReserve.get();
            if (a.getStatus() == Auction.Status.ACTIVE) {
                redirectAttributes.addFlashAttribute("reservationAlertKind", "error");
                redirectAttributes.addFlashAttribute("reservationAlertMessage",
                        messageSource.getMessage(
                                a.isActive() ? "reservation.alert.activeAuction"
                                        : "reservation.alert.auctionEndedNoDirectSale",
                                null, LocaleContextHolder.getLocale()));
                return redirectView;
            }
            if (a.getStatus() == Auction.Status.FINISHED) {
                redirectAttributes.addFlashAttribute("reservationAlertKind", "error");
                redirectAttributes.addFlashAttribute("reservationAlertMessage",
                        messageSource.getMessage("reservation.alert.packUnavailable", null,
                                LocaleContextHolder.getLocale()));
                return redirectView;
            }
        }

        final Integer stock = pack.getStock();
        if (reservationForm.getQuantity() != null && stock != null
                && reservationForm.getQuantity().intValue() > stock.intValue()) {
            bindingResult.rejectValue("quantity", "reservation.quantity.exceedsStock",
                    new Object[] { stock }, null);
        }
        if (bindingResult.hasErrors()) {
            final ModelAndView mav = buildPackDetailModel(pack, reservationForm, createDefaultBidForm());
            return mav;
        }

        final Double finalPrice = pack.getFinalPrice();
        if (finalPrice == null) {
            redirectAttributes.addFlashAttribute("reservationAlertKind", "error");
            redirectAttributes.addFlashAttribute("reservationAlertMessage",
                    messageSource.getMessage("reservation.alert.genericError", null,
                            LocaleContextHolder.getLocale()));
            return redirectView;
        }

        final int quantity = reservationForm.getQuantity().intValue();
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
        } catch (final IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("auctionAlertKind", "error");
            final String code;
            final String msg = ex.getMessage() != null ? ex.getMessage() : "";
            if (msg.contains("Cannot bid on your own auction")) {
                code = "pack.detail.bid.alert.ownCommerce";
            } else if (msg.contains("Already highest bidder")) {
                code = "pack.detail.bid.alert.alreadyLeading";
            } else if (msg.contains("Minimum bid increment")) {
                code = "pack.detail.bid.alert.belowIncrement";
            } else if (msg.contains("must be greater than current price")) {
                code = "pack.detail.bid.alert.belowMinimum";
            } else {
                code = "pack.detail.bid.alert.reject";
            }
            redirectAttributes.addFlashAttribute("auctionAlertMessage",
                    messageSource.getMessage(code, null, locale));
        } catch (final IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("auctionAlertKind", "error");
            final String code;
            final String msg = ex.getMessage() != null ? ex.getMessage() : "";
            if (msg.contains("expired")) {
                code = "pack.detail.bid.alert.auctionExpired";
            } else if (msg.contains("not active")) {
                code = "pack.detail.bid.alert.auctionNotActive";
            } else {
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
