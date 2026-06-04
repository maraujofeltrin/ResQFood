package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.notification.ClientNotificationPreference;
import ar.edu.itba.paw.models.notification.NotificationType;

import java.util.List;
import java.util.Optional;

public interface ClientNotificationPreferenceDao {

    Optional<ClientNotificationPreference> findByClientAndType(Long clientId, NotificationType type);

    List<ClientNotificationPreference> findByClient(Long clientId);

    ClientNotificationPreference upsert(Long clientId, NotificationType type, boolean mailEnabled);
}
