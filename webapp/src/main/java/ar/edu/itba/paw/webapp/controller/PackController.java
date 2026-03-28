package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.services.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller
public class PackController {

    private final ReservationService reservationService;

    @Autowired
    public PackController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    private static final List<String> DEFAULT_PICKUP_WINDOWS = Arrays.asList(
            "Today, 5:30 PM - 6:00 PM",
            "Today, 6:00 PM - 6:30 PM"
    );

    private static final String GENERIC_HERO =
            "https://lh3.googleusercontent.com/aida-public/AB6AXuDb9hqJJAJNKmO3vDzg7EtSwBaD2qDwByCk6_I-bar41vMvOr6ClV2eSjSKxqDojQWHI3eO8zB1BKkl1ntlGvi8EPZkbXBgzSMu9RiO7poHlFUWWEtzs2P9dfXj4foOTOoEKcnfHrLmCVCpUzdxvrhdZY2EOe0lyz4EURrPh7ee3TGa91znbF11iBDn0K7YO13wkdfJVec0vZk1h0jWNtouqj8Agx98aCT_Kuja_RcUDd3H-EaFaamyPYAagjr_yRloaXoZRO7aLVtd";

    @GetMapping("/packs/{id}")
    public ModelAndView packDetail(@PathVariable("id") final long id) {
        final ModelAndView mav = new ModelAndView("pack-detail/index");
        if (id == 1L) {
            fillArtisanBreadMock(mav, id);
        } else {
            fillGenericPack(mav, id);
        }
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

    private void fillArtisanBreadMock(final ModelAndView mav, final long packId) {
        mav.addObject("packId", packId);
        mav.addObject("unitPriceAmount", 4.50);
        mav.addObject("pageTitle", "Artisan Bread Surprise Pack | The Living Pantry");
        mav.addObject("packTitle", "Artisan Bread Surprise Pack");
        mav.addObject("packDescription",
                "A curated selection of today's freshest surplus. May include sourdough boules, rustic baguettes, specialty focaccia, or morning pastries. Every pack helps us reach our zero-waste goal.");
        mav.addObject("merchantName", "Golden Crust Bakery");
        mav.addObject("badgeLabel", "SURPRISE PACK");
        mav.addObject("reviewSummary", "4.8 (124 reviews)");
        mav.addObject("heroImageUrl",
                "https://lh3.googleusercontent.com/aida-public/AB6AXuDsMx595awk-V0jABJe0t_r-YhdAVRRiZ-30-gjVa2LfK1zQbdzSeLmfhLyf-lYee_Izo80iHmjBz6zyGTzqtsd5mQKJ7_SABhuBaI_Vz7k91CtUf4XvRZ0ifWgJ0bUvXMYt-AAarZMyyPtTeUPjFGEuxo4hTd6_09IzAzQmT31uPBFPWgm8W6kZeOdeiXcyHhAKEtclEbgPDS4NI-Yo9xJz-B5HYcRir9U0Pm_fK8mVGbuJF8V20KDBec1aNVmnIsagd8tkBYOBmMn");
        mav.addObject("heroImageAlt",
                "close-up of artisan sourdough loaves and golden croissants on a rustic wooden table in a sunlit bakery");
        mav.addObject("originalPrice", "$14.00");
        mav.addObject("finalPrice", "$4.50");
        mav.addObject("mealsRescued", "3 full meals");
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
    }

    private void fillGenericPack(final ModelAndView mav, final long id) {
        mav.addObject("packId", id);
        mav.addObject("unitPriceAmount", 7.99);
        mav.addObject("pageTitle", "Surprise Pack #" + id + " | The Living Pantry");
        mav.addObject("packTitle", "Surprise Pack #" + id);
        mav.addObject("packDescription",
                "A curated selection of surplus items from a trusted partner. Contents vary by day — every rescue supports our zero-waste mission.");
        mav.addObject("merchantName", "Partner Merchant #" + id);
        mav.addObject("badgeLabel", "SURPRISE PACK");
        mav.addObject("reviewSummary", "4.5 (48 reviews)");
        mav.addObject("heroImageUrl", GENERIC_HERO);
        mav.addObject("heroImageAlt", "Assorted fresh surplus food on a wooden table");
        mav.addObject("originalPrice", "$20.00");
        mav.addObject("finalPrice", "$7.99");
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
    }
}
