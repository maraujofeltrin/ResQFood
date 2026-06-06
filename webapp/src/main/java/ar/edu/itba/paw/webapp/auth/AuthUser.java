package ar.edu.itba.paw.webapp.auth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.Locale;

public class AuthUser extends User {

    private final long id;
    private Locale locale;

    public AuthUser(final long id,
                    final String username,
                    final String password,
                    final boolean enabled,
                    final Locale locale,
                    final Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, true, true, true, authorities);
        this.id = id;
        this.locale = locale;
    }

    public long getId() {
        return id;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(final Locale locale) {
        this.locale = locale;
    }
}
