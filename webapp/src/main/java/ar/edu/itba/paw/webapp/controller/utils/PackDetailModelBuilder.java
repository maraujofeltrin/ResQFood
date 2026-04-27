package ar.edu.itba.paw.webapp.controller.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.auction.Bid;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.services.auction.AuctionService;
import ar.edu.itba.paw.services.commerce.CommerceService;
import ar.edu.itba.paw.services.user.ClientService;
import ar.edu.itba.paw.webapp.form.BidForm;
import ar.edu.itba.paw.webapp.form.ReservationForm;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
public class PackDetailModelBuilder {

    private final CommerceService commerceService;
    private final AuctionService auctionService;
    private final ClientService clientService;
    private final MessageSource messageSource;
    private final ZoneId businessZone;
    private final AuthenticatedUserResolver authResolver;
    private static final Locale LOCALE_AR = new Locale("es", "AR");

    @Autowired
    public PackDetailModelBuilder(final CommerceService commerceService, final AuctionService auctionService,
            final ClientService clientService, final MessageSource messageSource, final ZoneId businessZone,
            final AuthenticatedUserResolver authResolver) {
        this.commerceService = commerceService;
        this.auctionService = auctionService;
        this.clientService = clientService;
        this.messageSource = messageSource;
        this.businessZone = businessZone;
        this.authResolver = authResolver;
    }

    private static String formatPrice(final Double amount) {
        if (amount == null) return "—";
        return NumberFormat.getCurrencyInstance(LOCALE_AR).format(amount);
    }

    private static String dashIfBlank(final String value) {
        if (value == null || value.isBlank()) return "—";
        return value.trim();
    }

    private void addCommerceDetailAttributes(final ModelAndView mav, final Optional<Commerce> commerceOpt) {
        final Commerce commerce = commerceOpt.orElse(null);
        final String commercialName = commerce != null && commerce.getCommercialName() != null && !commerce.getCommercialName().isBlank()
                ? commerce.getCommercialName().trim() : "—";
        mav.addObject("commerceCommercialName", commercialName);
        mav.addObject("commerceStreetLine", commerce != null ? commerce.getFullStreetLine() : "—");
        mav.addObject("commerceLocationLine", commerce != null ? commerce.getCityProvincePostal() : "—");
        mav.addObject("commerceOpeningTime", commerce != null ? dashIfBlank(commerce.getOpeningTime()) : "—");
        mav.addObject("commerceClosingTime", commerce != null ? dashIfBlank(commerce.getClosingTime()) : "—");
        mav.addObject("commerceOpenNow", Boolean.valueOf(commerce != null && commerce.isOpenNow(businessZone)));
    }

    private String formatAuctionEndForDisplay(final LocalDateTime endUtc, final Locale locale) {
        final ZonedDateTime z = endUtc.atZone(ZoneOffset.UTC).withZoneSameInstant(businessZone);
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(locale).format(z);
    }

    public ModelAndView buildPackDetailModel(final Pack pack, final ReservationForm reservationForm, final BidForm bidForm) {
        final Optional<Commerce> commerceOpt = commerceService.findByUserId(pack.getCommerceId());
        final Locale locale = LocaleContextHolder.getLocale();
        final String title = pack.getTitle() != null && !pack.getTitle().isBlank() ? pack.getTitle() : messageSource.getMessage("pack.detail.defaultTitle", null, locale);
        final String brand = messageSource.getMessage("app.brand", null, locale);
        final String pageTitle = messageSource.getMessage("pack.detail.pageTitle", new Object[] { title, brand }, locale);
        final ModelAndView mav = new ModelAndView("packs/packDetailView");
        mav.addObject("packId", pack.getId());
        mav.addObject("packImageId", pack.getImageId());

        final Optional<Auction> auctionOpt = auctionService.findByPackId(pack.getId());
        final boolean auctionPresent = auctionOpt.isPresent();
        final boolean auctionActive = auctionOpt.map(Auction::isActive).orElse(false);
        mav.addObject("auctionPresent", Boolean.valueOf(auctionPresent));
        mav.addObject("auctionActive", Boolean.valueOf(auctionActive));
        if (auctionPresent) {
            final long auctionId = auctionOpt.get().getId();
            final List<Bid> bidHistory = auctionService.getBidHistory(auctionId);
            final List<BidHistoryViewHelper.BidHistoryRow> bidHistoryItems = BidHistoryViewHelper.buildRows(
                    bidHistory, clientService, messageSource, locale);
            mav.addObject("auctionBidHistoryItems", bidHistoryItems);
        } else {
            mav.addObject("auctionBidHistoryItems", Collections.emptyList());
        }

        boolean auctionClientIsLeading = false;
        if (auctionOpt.isPresent()) {
            final Auction auction = auctionOpt.get();
            mav.addObject("auction", auction);
            final double effective = auction.getEffectivePrice() != null ? auction.getEffectivePrice() : 0d;
            final double increment = auction.getMinBidIncrement() != null ? auction.getMinBidIncrement() : 0d;
            final double minimumBidAmount = effective + increment;
            mav.addObject("auctionEffectiveAmount", effective);
            mav.addObject("auctionEffectivePriceDisplay", formatPrice(effective));
            mav.addObject("auctionEndDisplay", formatAuctionEndForDisplay(auction.getEndTime(), locale));
            mav.addObject("auctionMinBidHint", messageSource.getMessage("pack.detail.bid.minHint",
                    new Object[] { formatPrice(effective), formatPrice(minimumBidAmount), formatPrice(increment) }, locale));
            if (auctionActive) {
                mav.addObject("bidAmountMin", String.format(Locale.US, "%.2f", minimumBidAmount));
                auctionClientIsLeading = authResolver.resolveUserOrEmpty().map(u -> u.getRole() == User.Role.CLIENT && auctionService.isClientLeading(auction.getId(), u.getId())).orElse(false);
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
            mav.addObject("packStockBadgeCssClass", stock.intValue() > 0 ? "pack-detail-badge--stock-available" : "pack-detail-badge--stock-unavailable");
            mav.addObject("packStockBadgeText", messageSource.getMessage("pack.detail.stockBadge", new Object[] { stock }, locale));
        }
        if (stock != null && stock >= 1 && reservationForm.getQuantity() != null && reservationForm.getQuantity().intValue() > stock.intValue()) {
            reservationForm.setQuantity(stock);
        }
        mav.addObject("reservationForm", reservationForm);
        mav.addObject("bidForm", bidForm);
        return mav;
    }
}
