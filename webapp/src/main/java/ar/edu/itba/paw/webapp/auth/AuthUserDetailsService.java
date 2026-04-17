package ar.edu.itba.paw.webapp.auth;

import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.services.UserPasswordConstants;
import ar.edu.itba.paw.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
public class AuthUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Autowired
    public AuthUserDetailsService(final UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        final User user = userService.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (user.getRole() == null) {
            throw new UsernameNotFoundException("User " + username + " has no role assigned");
        }

        final String roleName = "ROLE_" + user.getRole().name();
        final Collection<? extends GrantedAuthority> authorities =
                Collections.singleton(new SimpleGrantedAuthority(roleName));

        final String password = UserPasswordConstants.RESERVATION_PENDING_PASSWORD.equals(user.getPassword())
            ? "$2a$10$00000000000000000000000000000000000000000000000000000"
            : user.getPassword();

        return new AuthUser(
                user.getEmail(),
                password,
            user.isVerified(),
                authorities);
    }
}