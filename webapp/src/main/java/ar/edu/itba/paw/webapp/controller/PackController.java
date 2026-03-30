package ar.edu.itba.paw.webapp.controller;

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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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
            "Today, 5:30 PM - 6:00 PM",
            "Today, 6:00 PM - 6:30 PM"
    );

    private static final String GENERIC_HERO =
            "https://lh3.googleusercontent.com/aida-public/AB6AXuDb9hqJJAJNKmO3vDzg7EtSwBaD2qDwByCk6_I-bar41vMvOr6ClV2eSjSKxqDojQWHI3eO8zB1BKkl1ntlGvi8EPZkbXBgzSMu9RiO7poHlFUWWEtzs2P9dfXj4foOTOoEKcnfHrLmCVCpUzdxvrhdZY2EOe0lyz4EURrPh7ee3TGa91znbF11iBDn0K7YO13wkdfJVec0vZk1h0jWNtouqj8Agx98aCT_Kuja_RcUDd3H-EaFaamyPYAagjr_yRloaXoZRO7aLVtd";

    private static String formatUsd(final Double amount) {
        if (amount == null) {
            return "—";
        }
        return NumberFormat.getCurrencyInstance(Locale.US).format(amount);
    }

    @GetMapping("/packs/{id}")
    public ModelAndView packDetail(@PathVariable("id") final long id) {
        final Pack pack = packService.findById(id)
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        final String merchantName = commerceService.findByUserId(pack.getCommerceId())
                .map(c -> c.getCommercialName())
                .filter(n -> n != null && !n.isBlank())
                .orElse("—");

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
        mav.addObject("merchantName", merchantName);
        mav.addObject("badgeLabel", "SURPRISE PACK");
        mav.addObject("reviewSummary", "4.5 (48 reviews)");
        mav.addObject("heroImageUrl", GENERIC_HERO);
        mav.addObject("heroImageAlt", "Assorted fresh surplus food on a wooden table");
        mav.addObject("originalPrice", formatUsd(pack.getOriginalPrice()));
        mav.addObject("finalPrice", formatUsd(pack.getFinalPrice()));
        mav.addObject("mealsRescued", "2 full meals");
        mav.addObject("reservationFormHeading", "Reserve this pack");
        mav.addObject("quantityLabel", "Number of packs");
        mav.addObject("defaultQuantity", "1");
        mav.addObject("pickupWindowLabel", "Pickup window");
        mav.addObject("pickupWindowHint", "Saved as pickup_window (max 512 characters).");
        mav.addObject("unitPriceLabel", "Price per pack");
        mav.addObject("totalLabel", "Total");
        mav.addObject("totalHint",
                "The amount charged will be the price per pack multiplied by the quantity you select.");
        mav.addObject("labelFirstName", "First name");
        mav.addObject("labelLastName", "Last name");
        mav.addObject("labelEmail", "Email");
        mav.addObject("labelPhone", "Phone");
        mav.addObject("confirmButtonLabel", "Confirm reservation");
        mav.addObject("impactMealsLabel", "Meals Rescued");
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
            @RequestParam("unitPrice") final double unitPrice,
            final RedirectAttributes redirectAttributes) {
        try {
            reservationService.createReservation(
                    packId, email, firstName, lastName, phone, quantity, unitPrice, pickupWindow);
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
