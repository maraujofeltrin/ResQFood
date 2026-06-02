package ar.edu.itba.paw.webapp.controller.helpers;

/**
 * Presentation model for a notification row in the navbar sidebar (SSR).
 */
public final class NotificationSidebarItem {

    private final long id;
    private final boolean read;
    private final String title;
    private final String body;
    private final String time;

    public NotificationSidebarItem(final long id, final boolean read, final String title,
            final String body, final String time) {
        this.id = id;
        this.read = read;
        this.title = title;
        this.body = body;
        this.time = time;
    }

    public long getId() {
        return id;
    }

    public boolean isRead() {
        return read;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getTime() {
        return time;
    }
}
