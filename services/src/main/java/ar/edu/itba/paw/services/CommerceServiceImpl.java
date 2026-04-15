package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.Commerce;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.persistence.CommerceDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CommerceServiceImpl implements CommerceService {

    private final UserService userService;
    private final CommerceDao commerceDao;

    @Autowired
    public CommerceServiceImpl(final UserService userService, final CommerceDao commerceDao) {
        this.userService = userService;
        this.commerceDao = commerceDao;
    }

    @Transactional
    @Override
    public Commerce getOrCreateCommerce(final String email, final String password, final String name, 
                                        final String commercialName, final Commerce.Category category, 
                                        final String street, final Integer streetNumber, 
                                        final String city, final String province, 
                                        final String postalCode, final String openingTime, final String closingTime) {
        
        Optional<User> maybeUser = userService.findByEmail(email);
        
        if (maybeUser.isPresent()) {
            User user = maybeUser.get();
            if (user.getRole() != null && user.getRole() != User.Role.COMMERCE) {
                throw new IllegalArgumentException("El email ya está registrado y no pertenece a un comercio.");
            }
            
            Optional<Commerce> maybeCommerce = commerceDao.findByUserId(user.getId());
            if (maybeCommerce.isPresent()) {
                return maybeCommerce.get();
            } else {
                return commerceDao.createCommerce(user.getId(), commercialName, category, street, streetNumber, city, province, postalCode, openingTime, closingTime);
            }
        }
        
        final User userToCreate = new User(null, email, password, name, null, User.Role.COMMERCE);
        final Commerce commerceProfile = new Commerce(
            null,
            commercialName,
            category,
            street,
            streetNumber,
            city,
            province,
            postalCode,
            openingTime,
            closingTime);
        final User newUser = userService.createUser(userToCreate, null, commerceProfile);
        return commerceDao.findByUserId(newUser.getId())
            .orElseGet(() -> commerceDao.createCommerce(
                newUser.getId(),
                commercialName,
                category,
                street,
                streetNumber,
                city,
                province,
                postalCode,
                openingTime,
                closingTime));
    }

    @Override
    public Optional<Commerce> findByUserId(final Long userId) {
        return commerceDao.findByUserId(userId);
    }
}
