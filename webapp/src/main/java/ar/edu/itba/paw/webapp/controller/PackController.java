package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.models.Commerce;
import ar.edu.itba.paw.models.Pack;
import ar.edu.itba.paw.services.CommerceService;
import ar.edu.itba.paw.services.PackService;
import ar.edu.itba.paw.services.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.NumberFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Controller
public class PackController {

    private final ReservationService reservationService;
    private final PackService packService;
    private final CommerceService commerceService;

    @Autowired
    public PackController(final ReservationService reservationService, final PackService packService,
            final CommerceService commerceService) {
        this.reservationService = reservationService;
        this.packService = packService;
        this.commerceService = commerceService;
    }

    private static final List<String> DEFAULT_PICKUP_WINDOWS = Arrays.asList(
            "Hoy, 17:30 - 18:00",
            "Hoy, 18:00 - 18:30"
    );

    private static final String GENERIC_HERO =
            "https://lh3.googleusercontent.com/aida-public/AB6AXuDb9hqJJAJNKmO3vDzg7EtSwBaD2qDwByCk6_I-bar41vMvOr6ClV2eSjSKxqDojQWHI3eO8zB1BKkl1ntlGvi8EPZkbXBgzSMu9RiO7poHlFUWWEtzs2P9dfXj4foOTOoEKcnfHrLmCVCpUzdxvrhdZY2EOe0lyz4EURrPh7ee3TGa91znbF11iBDn0K7YO13wkdfJVec0vZk1h0jWNtouqj8Agx98aCT_Kuja_RcUDd3H-EaFaamyPYAagjr_yRloaXoZRO7aLVtd";

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

    @GetMapping("/packs/{id}")
    public ModelAndView packDetail(@PathVariable("id") final long id) {
        final Pack pack = packService.findById(id)
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        final Optional<Commerce> commerceOpt = commerceService.findByUserId(pack.getCommerceId());

        final String title = pack.getTitle() != null && !pack.getTitle().isBlank()
                ? pack.getTitle()
                : "Pack";

        final ModelAndView mav = new ModelAndView("pack-detail/index");
        mav.addObject("packId", pack.getId());
        final double unitPriceAmount = pack.getFinalPrice() != null ? pack.getFinalPrice() : 0d;
        mav.addObject("unitPriceAmount", unitPriceAmount);
        mav.addObject("pageTitle", title + " | The Living Pantry");
        mav.addObject("packTitle", title);
        mav.addObject("packDescription", pack.getDescription() != null ? pack.getDescription() : "");
        addCommerceDetailAttributes(mav, commerceOpt);
        mav.addObject("badgeLabel", "SURPRISE PACK");
        mav.addObject("heroImageUrl", GENERIC_HERO);
        mav.addObject("originalPrice", formatUsd(pack.getOriginalPrice()));
        mav.addObject("finalPrice", formatUsd(pack.getFinalPrice()));
        mav.addObject("reservationFormHeading", "Reserva este pack");
        mav.addObject("quantityLabel", "Cantidad de packs");
        mav.addObject("defaultQuantity", "1");
        mav.addObject("pickupWindowLabel", "Franja horaria de retiro");
        mav.addObject("unitPriceLabel", "Precio por pack");
        mav.addObject("totalLabel", "Total");
        mav.addObject("totalHint",
                "El monto cobrado sera el precio por pack multiplicado por la cantidad que selecciones.");
        mav.addObject("labelFirstName", "Nombre");
        mav.addObject("labelLastName", "Apellido");
        mav.addObject("labelEmail", "Correo electronico");
        mav.addObject("labelPhone", "Telefono");
        mav.addObject("confirmButtonLabel", "Confirmar reserva");
        mav.addObject("pickupWindows", DEFAULT_PICKUP_WINDOWS);

        final Integer stock = pack.getStock();
        final int quantityMax = stock != null && stock >= 1 ? Math.min(stock, 999) : 999;
        mav.addObject("quantityMax", quantityMax);

        return mav;
    }

    @PostMapping("/packs/{packId}/reserve")
    public ModelAndView submitReservation(
            @PathVariable("packId") final long packId,
            @RequestParam("firstName") final String firstName,
            @RequestParam("lastName") final String lastName,
            @RequestParam("email") final String email,
            @RequestParam("phone") final String phone,
            @RequestParam("quantity") final int quantity,
            @RequestParam("pickupWindow") final String pickupWindow,
            final RedirectAttributes redirectAttributes) {
        final Optional<Pack> packOpt = packService.findById(packId)
                .filter(p -> Boolean.TRUE.equals(p.getActive()));

        if (packOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("reservationAlertKind", "error");
            redirectAttributes.addFlashAttribute("reservationAlertMessage",
                    "This pack is not available for reservation.");
            return new ModelAndView("redirect:/packs/" + packId);
        }

        final Pack pack = packOpt.get();
        final Double finalPrice = pack.getFinalPrice();
        if (finalPrice == null) {
            redirectAttributes.addFlashAttribute("reservationAlertKind", "error");
            redirectAttributes.addFlashAttribute("reservationAlertMessage",
                    "Your reservation could not be completed. Please try again.");
            return new ModelAndView("redirect:/packs/" + packId);
        }

        final Integer stock = pack.getStock();
        if (stock != null && quantity > stock) {
            redirectAttributes.addFlashAttribute("reservationAlertKind", "error");
            redirectAttributes.addFlashAttribute("reservationAlertMessage",
                    "The selected quantity exceeds available stock.");
            return new ModelAndView("redirect:/packs/" + packId);
        }

        try {
            reservationService.createReservation(
                    packId, email, firstName, lastName, phone, quantity, finalPrice, pickupWindow);
            redirectAttributes.addFlashAttribute("reservationAlertKind", "success");
            redirectAttributes.addFlashAttribute("reservationAlertMessage",
                    "Your reservation was confirmed. We've sent your reservation code to your email address.");
        } catch (final Exception ex) {
            redirectAttributes.addFlashAttribute("reservationAlertKind", "error");
            redirectAttributes.addFlashAttribute("reservationAlertMessage",
                    "Your reservation could not be completed. Please try again.");
        }
        return new ModelAndView("redirect:/packs/" + packId);
    }
}
