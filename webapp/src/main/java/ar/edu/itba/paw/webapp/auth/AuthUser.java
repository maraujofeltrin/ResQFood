package ar.edu.itba.paw.webapp.auth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class AuthUser extends User {

    private final long id;

    public AuthUser(final long id,
                    final String username,
                    final String password,
                    final boolean enabled,
                    final Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, true, true, true, authorities);
        this.id = id;
    }

    public long getId() {
        return id;
    }
}