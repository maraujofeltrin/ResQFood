package ar.edu.itba.paw.services.commerce;

import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.persistence.CommerceDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CommerceServiceImpl implements CommerceService {

    private final CommerceDao commerceDao;

    @Autowired
    public CommerceServiceImpl(final CommerceDao commerceDao) {
        this.commerceDao = commerceDao;
    }

    @Override
    public Optional<Commerce> findByUserId(final Long userId) {
        return commerceDao.findByUserId(userId);
    }
}
