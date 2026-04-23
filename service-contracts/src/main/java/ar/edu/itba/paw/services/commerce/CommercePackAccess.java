package ar.edu.itba.paw.services.commerce;

import ar.edu.itba.paw.models.pack.Pack;

import java.util.Optional;

/**
 * Whether a commerce may edit or delete a pack (e.g. direct-sale only; not auction-tied).
 */
public sealed interface CommercePackAccess
        permits CommercePackAccess.Granted, CommercePackAccess.NotFound, CommercePackAccess.ForbiddenAuction {

    record Granted(Pack pack) implements CommercePackAccess {
    }

    record NotFound() implements CommercePackAccess {
    }

    /** Pack exists but is tied to an auction (or similar policy). */
    record ForbiddenAuction() implements CommercePackAccess {
    }

    static Optional<Pack> resolvePackIfGranted(final CommercePackAccess access) {
        if (access instanceof final Granted g) {
            return Optional.of(g.pack());
        }
        return Optional.empty();
    }
}
