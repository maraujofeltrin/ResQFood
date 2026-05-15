package ar.edu.itba.paw.services.metrics;

import java.math.BigDecimal;
import java.util.List;

public class CommerceMetrics {

    public static class DailySalesPoint {
        private final String date;
        private final long count;

        public DailySalesPoint(final String date, final long count) {
            this.date = date;
            this.count = count;
        }

        public String getDate() {
            return date;
        }

        public long getCount() {
            return count;
        }
    }

    private final List<DailySalesPoint> dailySales;
    private final BigDecimal totalRevenue;
    private final long totalReservations;
    private final String bestSellingPackTitle;
    private final int acceptanceRatePercent;
    private final long canceledReservations;
    private final BigDecimal averageTicket;
    private final long uniqueClients;
    private final List<TopPackEntry> topPacks;
    private final List<TopClientEntry> topClients;
    private final ClientRetention clientRetention;

    public CommerceMetrics(final List<DailySalesPoint> dailySales, final BigDecimal totalRevenue,
            final long totalReservations, final String bestSellingPackTitle, final int acceptanceRatePercent,
            final long canceledReservations, final BigDecimal averageTicket, final long uniqueClients,
            final List<TopPackEntry> topPacks, final List<TopClientEntry> topClients,
            final ClientRetention clientRetention) {
        this.dailySales = dailySales;
        this.totalRevenue = totalRevenue;
        this.totalReservations = totalReservations;
        this.bestSellingPackTitle = bestSellingPackTitle;
        this.acceptanceRatePercent = acceptanceRatePercent;
        this.canceledReservations = canceledReservations;
        this.averageTicket = averageTicket;
        this.uniqueClients = uniqueClients;
        this.topPacks = topPacks;
        this.topClients = topClients;
        this.clientRetention = clientRetention;
    }

    public List<DailySalesPoint> getDailySales() {
        return dailySales;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public long getTotalReservations() {
        return totalReservations;
    }

    public String getBestSellingPackTitle() {
        return bestSellingPackTitle;
    }

    public int getAcceptanceRatePercent() {
        return acceptanceRatePercent;
    }

    public long getCanceledReservations() {
        return canceledReservations;
    }

    public BigDecimal getAverageTicket() {
        return averageTicket;
    }

    public long getUniqueClients() {
        return uniqueClients;
    }

    public List<TopPackEntry> getTopPacks() {
        return topPacks;
    }

    public List<TopClientEntry> getTopClients() {
        return topClients;
    }

    public ClientRetention getClientRetention() {
        return clientRetention;
    }
}

