package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.User;

public interface UserDao {
    public User createUser(String email, String password, String name);
}