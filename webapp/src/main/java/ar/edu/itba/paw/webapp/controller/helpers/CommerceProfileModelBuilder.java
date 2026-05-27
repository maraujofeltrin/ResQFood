package ar.edu.itba.paw.webapp.controller.helpers;

import ar.edu.itba.paw.services.commerce.CommerceProfileOfferItem;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.services.commerce.CommercePublicOffers;
import ar.edu.itba.paw.services.commerce.CommerceService;
import ar.edu.itba.paw.services.user.UserService;
import ar.edu.itba.paw.webapp.form.CommerceReviewForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Component
public class CommerceProfileModelBuilder {

    private static final int PAGE_SIZE = 12;

    private final CommerceService commerceService;
    private final CommerceReviewPageAttributes commerceReviewPageAttributes;
    private final UserService userService;
    private final MessageSource messageSource;
    private final CommerceDetailAttributesHelper commerceDetailAttributesHelper;

    @Autowired
    public CommerceProfileModelBuilder(final CommerceService commerceService,
            final CommerceReviewPageAttributes commerceReviewPageAttributes, final UserService userService,
            final MessageSource messageSource,
            final CommerceDetailAttributesHelper commerceDetailAttributesHelper) {
        this.commerceService = commerceService;
        this.commerceReviewPageAttributes = commerceReviewPageAttributes;
        this.userService = userService;
        this.messageSource = messageSource;
        this.commerceDetailAttributesHelper = commerceDetailAttributesHelper;
    }

    public Optional<ModelAndView> buildProfileModel(final long commerceUserId, final int page) {
        return buildProfileModel(commerceUserId, page, null);
    }

    public Optional<ModelAndView> buildProfileModel(final long commerceUserId, final int page,
            final CommerceReviewForm submittedForm) {
        final Optional<Commerce> commerceOpt = commerceService.findByUserId(commerceUserId);
        if (commerceOpt.isEmpty()) {
            return Optional.empty();
        }

        final Commerce commerce = commerceOpt.get();
        final Locale locale = LocaleContextHolder.getLocale();
        final String commercialName = commerce.getCommercialName() != null && !commerce.getCommercialName().isBlank()
                ? commerce.getCommercialName().trim()
                : messageSource.getMessage("commerce.profile.defaultName", null, locale);
        final String brand = messageSource.getMessage("app.brand", null, locale);
        final String pageTitle = messageSource.getMessage("commerce.profile.pageTitle",
                new Object[] { commercialName, brand }, locale);

        final CommercePublicOffers offers = commerceService.getPublicOffers(commerceUserId, page, PAGE_SIZE);
        final int totalPages = Math.max(1,
                (int) Math.ceil((double) offers.getTotalOffers() / (double) PAGE_SIZE));
        final int safePage = Math.max(1, Math.min(page, totalPages));

        final ModelAndView mav = new ModelAndView("commerce/commerceProfileView");
        mav.addObject("pageTitle", pageTitle);
        mav.addObject("commerceUserId", Long.valueOf(commerceUserId));
        mav.addObject("commerceCategory", commerce.getCategory());
        mav.addObject("profileImageId", userService.findById(commerceUserId).map(User::getProfileImageId).orElse(null));
        commerceDetailAttributesHelper.addCommerceDetailAttributes(mav, commerceOpt);

        commerceReviewPageAttributes.addReviewPageAttributes(mav, commerceUserId, submittedForm,
                submittedForm != null);

        mav.addObject("profileOffers", offers.getItems());
        mav.addObject("totalOffers", offers.getTotalOffers());

        final Map<Long, String> commerceNames = new HashMap<>();
        for (final CommerceProfileOfferItem offer : offers.getItems()) {
            commerceNames.put(offer.getPack().getId(), commercialName);
        }
        mav.addObject("commerceNames", commerceNames);

        mav.addObject("currentPage", safePage);
        mav.addObject("totalPages", totalPages);
        mav.addObject("paginationBaseUrl", "/commerces/" + commerceUserId);

        return Optional.of(mav);
    }
}
