package ar.edu.itba.paw.services.user;

import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.persistence.ClientDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ClientServiceImpl implements ClientService {

    private final ClientDao clientDao;

    @Autowired
    public ClientServiceImpl(final ClientDao clientDao) {
        this.clientDao = clientDao;
    }

    @Transactional
    @Override
    public Client createClient(final Long userId, final String name, final String lastName,
            final Boolean notificationsVisibilityPreferences) {
        return clientDao.createClient(userId, name, lastName, notificationsVisibilityPreferences);
    }

    @Override
    public Optional<Client> findByUserId(final Long userId) {
        return clientDao.findByUserId(userId);
    }

    @Transactional
    @Override
    public Client update(final Client client) {
        return clientDao.update(client);
    }
}
