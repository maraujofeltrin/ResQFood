package ar.edu.itba.paw.services.metrics;

public class TopPackEntry {

    private final Long packId;
    private final String packTitle;
    private final Long imageId;
    private final long unitsSold;

    public TopPackEntry(final Long packId, final String packTitle, final Long imageId, final long unitsSold) {
        this.packId = packId;
        this.packTitle = packTitle;
        this.imageId = imageId;
        this.unitsSold = unitsSold;
    }

    public Long getPackId() {
        return packId;
    }

    public String getPackTitle() {
        return packTitle;
    }

    public Long getImageId() {
        return imageId;
    }

    public long getUnitsSold() {
        return unitsSold;
    }
}
