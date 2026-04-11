package ar.edu.itba.paw.webapp.auth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class AuthUser extends User {

    public AuthUser(final String username,
                    final String password,
                    final Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
    }
}