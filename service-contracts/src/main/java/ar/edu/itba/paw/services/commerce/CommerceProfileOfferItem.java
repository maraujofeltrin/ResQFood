package ar.edu.itba.paw.services.commerce;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.pack.Pack;

import java.util.Objects;

public final class CommerceProfileOfferItem {

    private final Pack pack;
    private final Auction auction;

    public CommerceProfileOfferItem(final Pack pack, final Auction auction) {
        this.pack = Objects.requireNonNull(pack, "pack");
        this.auction = auction;
    }

    public Pack getPack() {
        return pack;
    }

    public Auction getAuction() {
        return auction;
    }
}
