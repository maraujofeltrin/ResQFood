package ar.edu.itba.paw.services.commerce;

import java.util.Collections;
import java.util.List;

public final class CommercePublicOffers {

    private final List<CommerceProfileOfferItem> items;
    private final int totalOffers;

    public CommercePublicOffers(final List<CommerceProfileOfferItem> items, final int totalOffers) {
        this.items = items != null ? items : Collections.emptyList();
        this.totalOffers = totalOffers;
    }

    public List<CommerceProfileOfferItem> getItems() {
        return items;
    }

    public int getTotalOffers() {
        return totalOffers;
    }
}
