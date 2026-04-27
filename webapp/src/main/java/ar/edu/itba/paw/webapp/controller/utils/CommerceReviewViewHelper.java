package ar.edu.itba.paw.webapp.controller.utils;

import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.models.user.CommerceReview;
import ar.edu.itba.paw.services.user.ClientService;

import java.util.List;
import java.util.stream.Collectors;

public final class CommerceReviewViewHelper {

    private CommerceReviewViewHelper() {
        // utility class
    }

    public static List<CommerceReviewRow> buildRows(final List<CommerceReview> reviews,
            final ClientService clientService) {
        return reviews.stream()
                .map(review -> new CommerceReviewRow(
                        review,
                        clientService.findByUserId(review.getClientUserId())
                                .map(Client::getFullName)
                                .orElse("-")))
                .collect(Collectors.toList());
    }

    public static final class CommerceReviewRow {
        private final CommerceReview review;
        private final String clientName;

        private CommerceReviewRow(final CommerceReview review, final String clientName) {
            this.review = review;
            this.clientName = clientName;
        }

        public CommerceReview getReview() {
            return review;
        }

        public String getClientName() {
            return clientName;
        }
    }
}
