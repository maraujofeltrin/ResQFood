package ar.edu.itba.paw.webapp.controller.helpers;

import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.services.commerce.CommerceService;
import ar.edu.itba.paw.services.user.UserService;
import ar.edu.itba.paw.webapp.auth.AuthUser;
import ar.edu.itba.paw.webapp.auth.AuthUserLocaleSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Shared helper for resolving the authenticated user, its role, and ownership
 * across all controllers.  Eliminates duplicated {@code resolveCurrentUser} /
 * {@code getAuthenticatedUser} patterns.
 */
@Component
public class AuthenticatedUserResolver {

    private final UserService userService;
    private final CommerceService commerceService;

    @Autowired
    public AuthenticatedUserResolver(final UserService userService, final CommerceService commerceService) {
        this.userService = userService;
        this.commerceService = commerceService;
    }

    /**
     * Resolves the current {@link User} from Spring Security's {@link Authentication}.
     *
     * @throws ResponseStatusException 401 if not authenticated
     */
    public User resolveUser(final Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return AuthUserLocaleSupport.authUserFrom(authentication)
                .map(this::resolveUser)
                .orElseGet(() -> {
                    final String email = authentication.getName();
                    if (email == null || email.isBlank() || "anonymousUser".equalsIgnoreCase(email)) {
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
                    }
                    return userService.findByEmail(email)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
                });
    }

    /**
     * Resolves the current {@link User} from an {@link AuthUser} principal.
     *
     * @throws ResponseStatusException 401 if principal is null or user not found
     */
    public User resolveUser(final AuthUser principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return userService.findById(principal.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    /**
     * Resolves the current user from the {@link SecurityContextHolder} (no explicit param).
     *
     * @throws ResponseStatusException 401 if not authenticated
     */
    public User resolveUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return resolveUser(authentication);
    }

    /**
     * Safely resolves the current user, returning Optional.empty() if not authenticated
     * or anonymous, without throwing exceptions.
     */
    public java.util.Optional<User> resolveUserOrEmpty() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return java.util.Optional.empty();
        }
        final java.util.Optional<AuthUser> authUser = AuthUserLocaleSupport.authUserFrom(auth);
        if (authUser.isPresent()) {
            return userService.findById(authUser.get().getId());
        }
        final String email = auth.getName();
        if (email == null || email.isBlank() || "anonymousUser".equalsIgnoreCase(email)) {
            return java.util.Optional.empty();
        }
        return userService.findByEmail(email);
    }

    /**
     * Resolves the authenticated user's {@link Commerce} profile from an AuthUser principal.
     *
     * @throws ResponseStatusException 401 if not authenticated, 404 if no commerce profile
     */
    public Commerce resolveCommerce(final AuthUser principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return commerceService.findByUserId(principal.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }


}
