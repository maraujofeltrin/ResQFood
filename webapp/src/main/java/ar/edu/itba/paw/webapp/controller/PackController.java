package ar.edu.itba.paw.webapp.controller;

import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.servlet.ServletContext;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

import ar.edu.itba.paw.models.Commerce;
import ar.edu.itba.paw.models.Pack;
import ar.edu.itba.paw.services.CommerceService;
import ar.edu.itba.paw.services.PackService;
import ar.edu.itba.paw.services.ReservationService;
import ar.edu.itba.paw.webapp.form.ReservationForm;

@Controller
public class PackController {

    private final ReservationService reservationService;
    private final PackService packService;
    private final CommerceService commerceService;
    private final ServletContext servletContext;
    private final MessageSource messageSource;

    private byte[] placeholderBytes;
    private String placeholderContentType;

    @Autowired
    public PackController(final ReservationService reservationService, final PackService packService,
            final CommerceService commerceService, final ServletContext servletContext,
            final MessageSource messageSource) {
        this.reservationService = reservationService;
        this.packService = packService;
        this.commerceService = commerceService;
        this.servletContext = servletContext;
        this.messageSource = messageSource;
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

    private static final List<String> DEFAULT_PICKUP_WINDOWS = Arrays.asList(
            "Hoy, 17:30 - 18:00",
            "Hoy, 18:00 - 18:30"
    );

    private static String formatUsd(final Double amount) {
        if (amount == null) {
            return "—";
        }
        return NumberFormat.getCurrencyInstance(Locale.US).format(amount);
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

    private static boolean computeOpenNow(final Commerce commerce) {
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
        final LocalTime now = LocalTime.now();
        if (!c.isBefore(o)) {
            return !now.isBefore(o) && !now.isAfter(c);
        }
        return !now.isBefore(o) || !now.isAfter(c);
    }

    private static void addCommerceDetailAttributes(final ModelAndView mav, final Optional<Commerce> commerceOpt) {
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
        mav.addObject("commerceOpenNow", Boolean.valueOf(computeOpenNow(commerce)));
    }

    private ReservationForm createDefaultReservationForm() {
        final ReservationForm form = new ReservationForm();
        form.setQuantity(Integer.valueOf(1));
        if (!DEFAULT_PICKUP_WINDOWS.isEmpty()) {
            form.setPickupWindow(DEFAULT_PICKUP_WINDOWS.get(0));
        }
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
        mav.addObject("originalPrice", formatUsd(pack.getOriginalPrice()));
        mav.addObject("finalPrice", formatUsd(pack.getFinalPrice()));
        mav.addObject("pickupWindows", DEFAULT_PICKUP_WINDOWS);

        final Integer stock = pack.getStock();
        final int quantityMax = stock != null && stock >= 1 ? Math.min(stock, 999) : 999;
        mav.addObject("quantityMax", quantityMax);

        mav.addObject("reservationForm", reservationForm);
        return mav;
    }

    @GetMapping("/packs")
    public ModelAndView listPacks(@RequestParam(value = "q", required = false) final String query) {
        final ModelAndView mav = new ModelAndView("packs/packCatalogView");
        final List<Pack> packs;
        if (query != null && !query.trim().isEmpty()) {
            packs = packService.searchPacks(query.trim());
        } else {
            packs = packService.findActive();
        }
        final Map<Long, String> commerceNames = packs.stream()
            .collect(java.util.stream.Collectors.toMap(
                Pack::getId, 
                pack -> commerceService.findByUserId(pack.getCommerceId())
                            .map(Commerce::getCommercialName)
                            .orElse("—")
            ));

        mav.addObject("packs", packs);
        mav.addObject("commerceNames", commerceNames);
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
            bindingResult.rejectValue("quantity", "reservation.quantity.exceedsStock");
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

        try {
            reservationService.createReservation(
                    packId,
                    reservationForm.getEmail(),
                    reservationForm.getFirstName(),
                    reservationForm.getLastName(),
                    reservationForm.getPhone(),
                    quantity,
                    finalPrice,
                    reservationForm.getPickupWindow());
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
