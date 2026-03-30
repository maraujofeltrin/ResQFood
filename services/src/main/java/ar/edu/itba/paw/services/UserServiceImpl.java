package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.persistence.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    @Autowired
    public UserServiceImpl(final UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User createUser(final String email, final String password, final String name) {
        return userDao.createUser(email, password, name, null, null);
    }
    
    @Override
    public User createUser(final String email, final String password, final String name, final String phone, final User.Role role) {
        return userDao.createUser(email, password, name, phone, role);
    }
    public Optional<User> findByEmail(final String email) {
        return userDao.findByEmail(email);
    }

    @Override
    public Optional<User> findById(final Long id) {
        return userDao.findById(id);
    }
}
