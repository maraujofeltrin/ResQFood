package ar.edu.itba.paw.webapp.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationHelper {

    private final UserDetailsService userDetailsService;

    @Autowired
    public AuthenticationHelper(final UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public void autoLogin(final String email, final String rawPassword) {
        final UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        final UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, rawPassword, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
