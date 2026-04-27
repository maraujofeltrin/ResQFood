package ar.edu.itba.paw.models.image;

public class Image {
    private final Long id;
    private final byte[] data;
    private final String contentType;

    public Image(Long id, byte[] data, String contentType) {
        this.id = id;
        this.data = data;
        this.contentType = contentType;
    }

    public Long getId() {
        return id;
    }

    public byte[] getData() {
        return data;
    }

    public String getContentType() {
        return contentType;
    }
}
