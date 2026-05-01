package ar.edu.itba.paw.webapp.controller.utils;

import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.models.user.CommerceReview;
import ar.edu.itba.paw.services.user.ClientService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class CommerceReviewViewHelper {

    private CommerceReviewViewHelper() {
    }

    public static List<CommerceReviewRow> buildRows(final List<CommerceReview> reviews,
            final ClientService clientService, final ZoneId businessZone, final Locale locale) {
        final DateTimeFormatter fmt = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale);
        return reviews.stream()
                .map(review -> {
                    final String name = clientService.findByUserId(review.getClientUserId())
                            .map(Client::getFullName)
                            .orElse("-");
                    final LocalDateTime ts = review.getUpdatedAt() != null ? review.getUpdatedAt()
                            : review.getCreatedAt();
                    final String date = ts != null
                            ? fmt.format(ts.atZone(ZoneOffset.UTC).withZoneSameInstant(businessZone))
                            : "";
                    final boolean edited = review.getUpdatedAt() != null && review.getCreatedAt() != null
                            && !review.getUpdatedAt().equals(review.getCreatedAt());
                    return new CommerceReviewRow(review, name, date, edited);
                })
                .collect(Collectors.toList());
    }

    public static final class CommerceReviewRow {
        private final CommerceReview review;
        private final String clientName;
        private final String formattedDate;
        private final boolean edited;

        private CommerceReviewRow(final CommerceReview review, final String clientName,
                final String formattedDate, final boolean edited) {
            this.review = review;
            this.clientName = clientName;
            this.formattedDate = formattedDate;
            this.edited = edited;
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
    }
}
