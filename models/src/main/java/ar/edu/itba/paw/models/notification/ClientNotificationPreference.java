package ar.edu.itba.paw.models.notification;

import ar.edu.itba.paw.models.user.Client;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "client_notification_preferences",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_client_notification_preferences_client_type",
                columnNames = {"client_id", "type"}))
public class ClientNotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "client_notification_preferences_id_seq")
    @SequenceGenerator(sequenceName = "client_notification_preferences_id_seq",
            name = "client_notification_preferences_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 64)
    private NotificationType type;

    @Column(name = "mail_enabled", nullable = false)
    private boolean mailEnabled;

    protected ClientNotificationPreference() {}

    public ClientNotificationPreference(final Long id, final Client client,
            final NotificationType type, final boolean mailEnabled) {
        this.id = id;
        this.client = client;
        this.type = type;
        this.mailEnabled = mailEnabled;
    }

    public Long getId() { return id; }
    public Client getClient() { return client; }
    public Long getClientId() { return client.getUserId(); }
    public NotificationType getType() { return type; }
    public boolean isMailEnabled() { return mailEnabled; }
    public void setMailEnabled(final boolean mailEnabled) { this.mailEnabled = mailEnabled; }
}
