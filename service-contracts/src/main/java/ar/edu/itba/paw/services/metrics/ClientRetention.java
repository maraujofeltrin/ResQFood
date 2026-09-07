package ar.edu.itba.paw.services.metrics;

public class ClientRetention {

    private final long newClients;
    private final long returningClients;
    private final int newPercent;
    private final int returningPercent;

    public ClientRetention(final long newClients, final long returningClients, final int newPercent,
            final int returningPercent) {
        this.newClients = newClients;
        this.returningClients = returningClients;
        this.newPercent = newPercent;
        this.returningPercent = returningPercent;
    }

    public long getNewClients() {
        return newClients;
    }

    public long getReturningClients() {
        return returningClients;
    }

    public int getNewPercent() {
        return newPercent;
    }

    public int getReturningPercent() {
        return returningPercent;
    }
}
