package ar.edu.itba.paw.webapp.controller.helpers;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.services.commerce.CommerceFavoriteService;
import ar.edu.itba.paw.services.commerce.CommerceService;
import ar.edu.itba.paw.services.pack.PackService;
import ar.edu.itba.paw.webapp.form.CommerceReviewForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
public class CommerceProfileModelBuilder {

    private static final int PAGE_SIZE = 12;

    private final CommerceService commerceService;
    private final PackService packService;
    private final CommerceReviewPageAttributes commerceReviewPageAttributes;
    private final MessageSource messageSource;
    private final CommerceDetailAttributesHelper commerceDetailAttributesHelper;
    private final CommerceFavoriteService commerceFavoriteService;
    private final AuthenticatedUserResolver authResolver;

    @Autowired
    public CommerceProfileModelBuilder(final CommerceService commerceService,
            final PackService packService,
            final CommerceReviewPageAttributes commerceReviewPageAttributes,
            final MessageSource messageSource,
            final CommerceDetailAttributesHelper commerceDetailAttributesHelper,
            final CommerceFavoriteService commerceFavoriteService,
            final AuthenticatedUserResolver authResolver) {
        this.commerceService = commerceService;
        this.packService = packService;
        this.commerceReviewPageAttributes = commerceReviewPageAttributes;
        this.messageSource = messageSource;
        this.commerceDetailAttributesHelper = commerceDetailAttributesHelper;
        this.commerceFavoriteService = commerceFavoriteService;
        this.authResolver = authResolver;
    }

    public Optional<ModelAndView> buildProfileModel(final long commerceUserId, final int page,
            final int reviewPage) {
        return doBuildProfileModel(commerceUserId, page, reviewPage, null);
    }

    public Optional<ModelAndView> buildProfileModel(final long commerceUserId, final int page,
            final CommerceReviewForm submittedForm) {
        return doBuildProfileModel(commerceUserId, page, 1, submittedForm);
    }

    private Optional<ModelAndView> doBuildProfileModel(final long commerceUserId, final int page,
            final int reviewPage, final CommerceReviewForm submittedForm) {
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

        final int totalOffers = packService.countPublicOffersByCommerce(commerceUserId);
        final int totalPages = Math.max(1,
                (int) Math.ceil((double) totalOffers / (double) PAGE_SIZE));
        final int safePage = Math.max(1, Math.min(page, totalPages));
        final List<Pack> offers = packService.getPublicOffersByCommerce(commerceUserId, safePage, PAGE_SIZE);

        final ModelAndView mav = new ModelAndView("commerce/commerceProfileView");
        mav.addObject("pageTitle", pageTitle);
        mav.addObject("commerceUserId", Long.valueOf(commerceUserId));
        mav.addObject("commerceCategory", commerce.getCategory());
        mav.addObject("profileImageId",
                commerce.getUser() != null ? commerce.getUser().getProfileImageId() : null);
        commerceDetailAttributesHelper.addCommerceDetailAttributes(mav, commerceOpt);

        commerceReviewPageAttributes.addReviewPageAttributes(mav, commerceUserId, reviewPage, submittedForm,
                submittedForm != null);

        mav.addObject("profileOffers", offers);
        mav.addObject("totalOffers", totalOffers);

        mav.addObject("currentPage", safePage);
        mav.addObject("totalPages", totalPages);
        mav.addObject("paginationBaseUrl", "/commerces/" + commerceUserId);

        final boolean commerceFavoriteSelected = authResolver.resolveUserOrEmpty()
                .filter(u -> u.getRole() == User.Role.CLIENT)
                .map(u -> commerceFavoriteService.isFavorite(u.getId(), commerceUserId))
                .orElse(false);
        mav.addObject("commerceFavoriteSelected", commerceFavoriteSelected);

        return Optional.of(mav);
    }
}
