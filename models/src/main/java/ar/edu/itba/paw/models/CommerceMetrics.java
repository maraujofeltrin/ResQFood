package ar.edu.itba.paw.models;

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

    public CommerceMetrics(final List<DailySalesPoint> dailySales, final BigDecimal totalRevenue,
            final long totalReservations, final String bestSellingPackTitle, final int acceptanceRatePercent) {
        this.dailySales = dailySales;
        this.totalRevenue = totalRevenue;
        this.totalReservations = totalReservations;
        this.bestSellingPackTitle = bestSellingPackTitle;
        this.acceptanceRatePercent = acceptanceRatePercent;
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
}
