package ar.edu.itba.paw.services.commerce;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.pack.Pack;

import java.util.Collections;
import java.util.List;

public final class CommercePublicOffers {

    private final List<Pack> directPacks;
    private final int directPacksTotal;
    private final List<Auction> activeAuctions;
    private final int activeAuctionsTotal;

    public CommercePublicOffers(final List<Pack> directPacks, final int directPacksTotal,
                                final List<Auction> activeAuctions, final int activeAuctionsTotal) {
        this.directPacks = directPacks != null ? directPacks : Collections.emptyList();
        this.directPacksTotal = directPacksTotal;
        this.activeAuctions = activeAuctions != null ? activeAuctions : Collections.emptyList();
        this.activeAuctionsTotal = activeAuctionsTotal;
    }

    public List<Pack> getDirectPacks() {
        return directPacks;
    }

    public int getDirectPacksTotal() {
        return directPacksTotal;
    }

    public List<Auction> getActiveAuctions() {
        return activeAuctions;
    }

    public int getActiveAuctionsTotal() {
        return activeAuctionsTotal;
    }
}
