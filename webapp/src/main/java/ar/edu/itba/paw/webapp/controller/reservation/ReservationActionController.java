package ar.edu.itba.paw.webapp.controller.reservation;

import javax.validation.Valid;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.auction.BidPlacementException;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.services.auction.AuctionService;
import ar.edu.itba.paw.services.pack.DirectReservationCheck;
import ar.edu.itba.paw.services.pack.PackService;
import ar.edu.itba.paw.services.reservation.ReservationService;
import ar.edu.itba.paw.webapp.controller.utils.AuthenticatedUserResolver;
import ar.edu.itba.paw.webapp.controller.utils.PackDetailModelBuilder;
import ar.edu.itba.paw.webapp.form.BidForm;
import ar.edu.itba.paw.webapp.form.ReservationForm;
import java.util.Locale;

@Controller
public class ReservationActionController {

    private final ReservationService reservationService;
    private final PackService packService;
    private final AuctionService auctionService;
    private final MessageSource messageSource;
    private final AuthenticatedUserResolver authResolver;
    private final PackDetailModelBuilder packDetailModelBuilder;

    @Autowired
    public ReservationActionController(final ReservationService reservationService,
            final PackService packService, final AuctionService auctionService,
            final MessageSource messageSource, final AuthenticatedUserResolver authResolver,
            final PackDetailModelBuilder packDetailModelBuilder) {
        this.reservationService = reservationService;
        this.packService = packService;
        this.auctionService = auctionService;
        this.messageSource = messageSource;
        this.authResolver = authResolver;
        this.packDetailModelBuilder = packDetailModelBuilder;
    }

    private BidForm createDefaultBidForm() {
        return new BidForm();
    }
    private ReservationForm createDefaultReservationForm() {
        ReservationForm form = new ReservationForm();
        form.setQuantity(Integer.valueOf(1));
        return form;
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
                    .map(pack -> packDetailModelBuilder.buildPackDetailModel(pack, reservationForm, createDefaultBidForm()))
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
            return packDetailModelBuilder.buildPackDetailModel(p, reservationForm, createDefaultBidForm());
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
        final User authenticatedUser = authResolver.resolveUser();

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
            return packDetailModelBuilder.buildPackDetailModel(pack, createDefaultReservationForm(), bidForm);
        }

        final User user = authResolver.resolveUser();
        if (user.getRole() != User.Role.CLIENT) {
            redirectAttributes.addFlashAttribute("auctionAlertKind", "error");
            redirectAttributes.addFlashAttribute("auctionAlertMessage",
                    messageSource.getMessage("pack.detail.bid.alert.roleNotClient", null, locale));
            return redirectView;
        }

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
