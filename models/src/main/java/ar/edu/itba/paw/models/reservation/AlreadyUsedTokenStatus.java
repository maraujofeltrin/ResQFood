package ar.edu.itba.paw.models.reservation;

public enum AlreadyUsedTokenStatus {
    ACCEPTED("reservation.token.status.used.accepted"),
    REJECTED("reservation.token.status.used.rejected");

    private final String detailCode;

    AlreadyUsedTokenStatus(final String detailCode) {
        this.detailCode = detailCode;
    }

    public String getDetailCode() {
        return detailCode;
    }
}
