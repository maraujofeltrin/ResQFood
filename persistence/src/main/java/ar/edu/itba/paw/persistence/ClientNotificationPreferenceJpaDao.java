package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.notification.ClientNotificationPreference;
import ar.edu.itba.paw.models.notification.NotificationType;
import ar.edu.itba.paw.models.user.Client;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Primary
@Repository
public class ClientNotificationPreferenceJpaDao implements ClientNotificationPreferenceDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<ClientNotificationPreference> findByClientAndType(final Long clientId,
            final NotificationType type) {
        return em.createQuery(
                "FROM ClientNotificationPreference p WHERE p.client.userId = :clientId AND p.type = :type",
                ClientNotificationPreference.class)
                .setParameter("clientId", clientId)
                .setParameter("type", type)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public List<ClientNotificationPreference> findByClient(final Long clientId) {
        return em.createQuery(
                "FROM ClientNotificationPreference p WHERE p.client.userId = :clientId ORDER BY p.type",
                ClientNotificationPreference.class)
                .setParameter("clientId", clientId)
                .getResultList();
    }

    @Override
    public ClientNotificationPreference upsert(final Long clientId, final NotificationType type,
            final boolean mailEnabled) {
        return findByClientAndType(clientId, type)
                .map(existing -> {
                    existing.setMailEnabled(mailEnabled);
                    em.flush();
                    return existing;
                })
                .orElseGet(() -> {
                    final Client client = em.getReference(Client.class, clientId);
                    final ClientNotificationPreference pref = new ClientNotificationPreference(null, client, type,
                            mailEnabled);
                    em.persist(pref);
                    em.flush();
                    return pref;
                });
    }
}
