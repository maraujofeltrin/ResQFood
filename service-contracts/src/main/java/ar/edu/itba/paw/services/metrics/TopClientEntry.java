package ar.edu.itba.paw.services.metrics;

public class TopClientEntry {

    private final Long clientId;
    private final String clientName;
    private final long reservationCount;

    public TopClientEntry(final Long clientId, final String clientName, final long reservationCount) {
        this.clientId = clientId;
        this.clientName = clientName;
        this.reservationCount = reservationCount;
    }

    public Long getClientId() {
        return clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public long getReservationCount() {
        return reservationCount;
    }
}
