package ar.edu.itba.paw.webapp.controller.helpers;

import ar.edu.itba.paw.models.user.CommerceReview;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.services.commerce.CommerceReviewService;
import ar.edu.itba.paw.webapp.form.CommerceReviewForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
public class CommerceReviewPageAttributes {

    private static final int REVIEW_LIST_LIMIT = 5;

    private final CommerceReviewService commerceReviewService;
    private final AuthenticatedUserResolver authResolver;
    private final ZoneId businessZone;

    @Autowired
    public CommerceReviewPageAttributes(final CommerceReviewService commerceReviewService,
            final AuthenticatedUserResolver authResolver,
            final ZoneId businessZone) {
        this.commerceReviewService = commerceReviewService;
        this.authResolver = authResolver;
        this.businessZone = businessZone;
    }

    public void addReviewPageAttributes(final ModelAndView mav, final long commerceId,
            final CommerceReviewForm submittedForm, final boolean formExpanded) {
        final Locale locale = org.springframework.context.i18n.LocaleContextHolder.getLocale();
        final List<CommerceReview> reviews = commerceReviewService.findReviewsForCommerce(commerceId, 1,
                REVIEW_LIST_LIMIT);
        final Optional<User> userOpt = authResolver.resolveUserOrEmpty();
        final Long currentClientId = userOpt.filter(u -> u.getRole() == User.Role.CLIENT)
                .map(User::getId).orElse(null);

        mav.addObject("commerceReviewItems",
                CommerceReviewViewHelper.buildRows(reviews, businessZone, locale, currentClientId));
        mav.addObject("commerceReviewCount", commerceReviewService.countReviewsForCommerce(commerceId));
        mav.addObject("commerceReviewAverageRating",
                commerceReviewService.averageRatingForCommerce(commerceId).orElse(null));

        boolean canReview = false;
        boolean alreadyReviewed = false;
        CommerceReviewForm form = submittedForm;
        if (currentClientId != null) {
            canReview = commerceReviewService.canClientReviewCommerce(currentClientId.longValue(), commerceId);
            final Optional<CommerceReview> ownReview = commerceReviewService.findClientReview(
                    currentClientId.longValue(), commerceId);
            alreadyReviewed = ownReview.isPresent();
            if (form == null) {
                form = new CommerceReviewForm();
                if (ownReview.isPresent()) {
                    final CommerceReview review = ownReview.get();
                    form.setRating(review.getRating());
                    form.setBody(review.getBody());
                }
            }
        }
        if (form == null) {
            form = new CommerceReviewForm();
        }
        mav.addObject("commerceReviewForm", form);
        mav.addObject("commerceReviewCanSubmit", Boolean.valueOf(canReview));
        mav.addObject("commerceReviewAlreadySubmitted", Boolean.valueOf(alreadyReviewed));
        final boolean expanded = formExpanded
                || Boolean.TRUE.equals(mav.getModel().get("commerceReviewFormExpanded"));
        mav.addObject("commerceReviewFormExpanded", Boolean.valueOf(expanded));
    }
}
