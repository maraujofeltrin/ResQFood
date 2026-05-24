package ar.edu.itba.paw.webapp.controller.helpers;

import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.models.user.CommerceReview;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Builds commerce review presentation rows from pre-fetched data.
 * Not instantiable.
 */
public final class CommerceReviewViewHelper {

    private CommerceReviewViewHelper() {
    }

    /**
     * Builds rows from pre-fetched client data to avoid N+1 queries.
     *
     * @param reviews          the reviews to display
     * @param clientsByUserId  pre-fetched map of userId → Client
     * @param businessZone     for date display conversion
     * @param locale           the current locale
     */
    public static List<CommerceReviewRow> buildRows(final List<CommerceReview> reviews,
            final Map<Long, Client> clientsByUserId, final ZoneId businessZone, final Locale locale) {
        return buildRows(reviews, clientsByUserId, businessZone, locale, null);
    }

    public static List<CommerceReviewRow> buildRows(final List<CommerceReview> reviews,
            final Map<Long, Client> clientsByUserId, final ZoneId businessZone, final Locale locale,
            final Long currentClientUserId) {
        final DateTimeFormatter fmt = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale);
        return sortForDisplay(reviews, currentClientUserId).stream()
                .map(review -> {
                    final Client client = clientsByUserId.get(review.getClientUserId());
                    final String name = client != null ? client.getFullName() : "-";
                    final LocalDateTime ts = review.getUpdatedAt() != null ? review.getUpdatedAt()
                            : review.getCreatedAt();
                    final String date = ts != null
                            ? fmt.format(ts.atZone(ZoneOffset.UTC).withZoneSameInstant(businessZone))
                            : "";
                    final boolean edited = review.getUpdatedAt() != null && review.getCreatedAt() != null
                            && !review.getUpdatedAt().equals(review.getCreatedAt());
                    final boolean own = currentClientUserId != null
                            && currentClientUserId.equals(review.getClientUserId());
                    return new CommerceReviewRow(review, name, date, edited, own);
                })
                .collect(Collectors.toList());
    }

    /**
     * Own review first (when present), then newest {@code createdAt} first.
     */
    static List<CommerceReview> sortForDisplay(final List<CommerceReview> reviews,
            final Long currentClientUserId) {
        if (reviews == null || reviews.isEmpty()) {
            return reviews;
        }
        return reviews.stream()
                .sorted(Comparator
                        .comparing((CommerceReview review) -> isOwnReview(review, currentClientUserId))
                        .reversed()
                        .thenComparing(CommerceReview::getCreatedAt,
                                Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    private static boolean isOwnReview(final CommerceReview review, final Long currentClientUserId) {
        return currentClientUserId != null && currentClientUserId.equals(review.getClientUserId());
    }

    public static final class CommerceReviewRow {
        private final CommerceReview review;
        private final String clientName;
        private final String formattedDate;
        private final boolean edited;
        private final boolean own;

        private CommerceReviewRow(final CommerceReview review, final String clientName,
                final String formattedDate, final boolean edited, final boolean own) {
            this.review = review;
            this.clientName = clientName;
            this.formattedDate = formattedDate;
            this.edited = edited;
            this.own = own;
        }

        public CommerceReview getReview() {
            return review;
        }

        public String getClientName() {
            return clientName;
        }

        public String getFormattedDate() {
            return formattedDate;
        }

        public boolean isEdited() {
            return edited;
        }

        public boolean isOwn() {
            return own;
        }
    }
}
