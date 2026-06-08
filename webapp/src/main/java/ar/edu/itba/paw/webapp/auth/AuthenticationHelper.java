package ar.edu.itba.paw.webapp.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Component
public class AuthenticationHelper {

    private final UserDetailsService userDetailsService;

    @Autowired
    public AuthenticationHelper(final UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public void autoLogin(final String email, final HttpServletRequest request) {
        final UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        final Authentication auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        final SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);
        final HttpSession existingSession = request.getSession(false);
        if (existingSession != null) {
            existingSession.invalidate();
        }
        request.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
    }
}
