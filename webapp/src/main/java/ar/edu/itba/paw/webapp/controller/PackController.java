package ar.edu.itba.paw.webapp.controller;

import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

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

import ar.edu.itba.paw.models.Commerce;
import ar.edu.itba.paw.models.Pack;
import ar.edu.itba.paw.models.PackTag;
import ar.edu.itba.paw.models.PackSortOption;
import ar.edu.itba.paw.models.Municipality;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.services.CommerceService;
import ar.edu.itba.paw.services.PackService;
import ar.edu.itba.paw.services.ReservationService;
import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.webapp.form.ReservationForm;

@Controller
public class PackController {

    private final ReservationService reservationService;
    private final PackService packService;
    private final CommerceService commerceService;
    private final UserService userService;
    private final ServletContext servletContext;
    private final MessageSource messageSource;
    private final ZoneId businessZone;

    private byte[] placeholderBytes;
    private String placeholderContentType;

    @Autowired
    public PackController(final ReservationService reservationService, final PackService packService,
            final CommerceService commerceService, final UserService userService,
            final ServletContext servletContext,
            final MessageSource messageSource,
            @Value("${app.display-zone:}") final String displayZone) {
        this.reservationService = reservationService;
        this.packService = packService;
        this.commerceService = commerceService;
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

    private ModelAndView buildPackDetailModel(final Pack pack, final ReservationForm reservationForm) {
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
        final double unitPriceAmount = pack.getFinalPrice() != null ? pack.getFinalPrice() : 0d;
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
        return mav;
    }

    private static final int PAGE_SIZE = 6;

    @GetMapping("/packs")
    public ModelAndView listPacks(
            @RequestParam(value = "q", required = false) final String query,
            @RequestParam(value = "tags", required = false) final List<String> tagNames,
            @RequestParam(value = "sort", required = false) final String sort,
            @RequestParam(value = "location", required = false) final String locationParam,
            @RequestParam(value = "timeRange", required = false) final List<String> timeRange,
            @RequestParam(value = "page", defaultValue = "1") final int page) {

        final ModelAndView mav = new ModelAndView("packs/packCatalogView");
        final PackSortOption sortOption = PackSortOption.fromString(sort);

        // --- parse tags (whitelist via enum) ---
        final List<PackTag> selectedTags = new ArrayList<>();
        if (tagNames != null) {
            for (final String name : tagNames) {
                try {
                    selectedTags.add(PackTag.valueOf(name));
                } catch (final IllegalArgumentException ignored) {
                }
            }
        }

        // --- parse location (whitelist via Municipality enum) ---
        final Municipality municipality = Municipality.fromString(locationParam);
        final String cityFilter = municipality != null ? municipality.getCityName() : null;

        // --- sanitise timeRange (DAO switch-case silently ignores unknown values) ---
        final List<String> safeTimeRange = timeRange != null ? timeRange : new ArrayList<>();

        // --- query text ---
        final String trimmedQuery = (query != null && !query.trim().isEmpty()) ? query.trim() : null;

        // --- unified filter ---
        final List<Pack> allPacks = packService.filterPacks(
                trimmedQuery, selectedTags.isEmpty() ? null : selectedTags,
                cityFilter, safeTimeRange.isEmpty() ? null : safeTimeRange, sortOption);

        // --- pagination ---
        final int totalPages = Math.max(1, (int) Math.ceil((double) allPacks.size() / PAGE_SIZE));
        final int safePage = Math.max(1, Math.min(page, totalPages));
        final int fromIdx = (safePage - 1) * PAGE_SIZE;
        final int toIdx = Math.min(fromIdx + PAGE_SIZE, allPacks.size());
        final List<Pack> packs = allPacks.subList(fromIdx, toIdx);

        final Map<Long, String> commerceNames = packs.stream()
            .collect(java.util.stream.Collectors.toMap(
                Pack::getId,
                p -> commerceService.findByUserId(p.getCommerceId())
                            .map(Commerce::getCommercialName)
                            .orElse("—")
            ));

        // --- build pagination base URL preserving all current filters ---
        final StringBuilder baseUrlBuilder = new StringBuilder("/packs");
        boolean firstParam = true;
        if (trimmedQuery != null) {
            baseUrlBuilder.append(firstParam ? "?" : "&").append("q=").append(java.net.URLEncoder.encode(trimmedQuery, java.nio.charset.StandardCharsets.UTF_8));
            firstParam = false;
        }
        if (!selectedTags.isEmpty()) {
            for (final PackTag tag : selectedTags) {
                baseUrlBuilder.append(firstParam ? "?" : "&").append("tags=").append(tag.name());
                firstParam = false;
            }
        }
        if (sort != null && !sort.isBlank()) {
            baseUrlBuilder.append(firstParam ? "?" : "&").append("sort=").append(sortOption.name());
            firstParam = false;
        }
        if (municipality != null) {
            baseUrlBuilder.append(firstParam ? "?" : "&").append("location=").append(municipality.name());
            firstParam = false;
        }
        if (!safeTimeRange.isEmpty()) {
            for (final String tr : safeTimeRange) {
                baseUrlBuilder.append(firstParam ? "?" : "&").append("timeRange=").append(tr);
                firstParam = false;
            }
        }

        mav.addObject("packs", packs);
        mav.addObject("commerceNames", commerceNames);
        mav.addObject("availableTags", PackTag.values());
        mav.addObject("selectedTags", selectedTags);
        mav.addObject("availableSorts", PackSortOption.values());
        mav.addObject("currentSort", sortOption);
        mav.addObject("availableMunicipalities", Municipality.values());
        mav.addObject("selectedMunicipality", municipality);
        mav.addObject("selectedTimeRanges", safeTimeRange);
        mav.addObject("currentPage", safePage);
        mav.addObject("totalPages", totalPages);
        mav.addObject("paginationBaseUrl", baseUrlBuilder.toString());
        return mav;
    }

    @GetMapping("/packs/{id}")
    public ModelAndView packDetail(@PathVariable("id") final long id) {
        final Pack pack = packService.findById(id)
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return buildPackDetailModel(pack, createDefaultReservationForm());
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
        final Integer stock = pack.getStock();
        if (reservationForm.getQuantity() != null && stock != null
                && reservationForm.getQuantity().intValue() > stock.intValue()) {
            bindingResult.rejectValue("quantity", "reservation.quantity.exceedsStock",
                    new Object[] { stock }, null);
        }
        if (bindingResult.hasErrors()) {
            final ModelAndView mav = buildPackDetailModel(pack, reservationForm);
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
}
