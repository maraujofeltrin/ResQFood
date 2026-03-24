package ar.edu.itba.paw.persistence;

import org.springframework.stereotype.Repository;

import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.persistence.UserDao;

@Repository
public class UserJdbcDao implements UserDao {
    
    @Override
    public User createUser(String email, String password, String name) {
        return new User(email, password, name);
    }
}