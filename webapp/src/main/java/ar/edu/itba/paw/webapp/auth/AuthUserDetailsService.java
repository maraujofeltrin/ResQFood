package ar.edu.itba.paw.webapp.auth;

import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.services.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

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

        final Locale locale = user.getLocale() != null ? user.getLocale() : Locale.forLanguageTag("es");
        return new AuthUser(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.isVerified(),
                locale,
                user.getProfileImageId(),
                authorities);
    }
}