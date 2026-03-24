package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.User;

public interface UserService {
    User createUser(final String email, final String password, final String name);

}
