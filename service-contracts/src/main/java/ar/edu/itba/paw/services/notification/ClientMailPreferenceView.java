package ar.edu.itba.paw.services.notification;

import ar.edu.itba.paw.models.notification.NotificationType;

public final class ClientMailPreferenceView {

    private final NotificationType type;
    private final boolean mailEnabled;

    public ClientMailPreferenceView(final NotificationType type, final boolean mailEnabled) {
        this.type = type;
        this.mailEnabled = mailEnabled;
    }

    public NotificationType getType() { return type; }
    public boolean isMailEnabled() { return mailEnabled; }
}
