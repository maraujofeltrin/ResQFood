package ar.edu.itba.paw.webapp.controller.utils;

import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.services.commerce.CommerceService;
import ar.edu.itba.paw.services.user.UserService;
import ar.edu.itba.paw.webapp.auth.AuthUser;
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
        final String email = authentication.getName();
        if (email == null || email.isBlank() || "anonymousUser".equalsIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return userService.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
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
        return userService.findByEmail(principal.getUsername())
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
     * Resolves the authenticated user's {@link Commerce} profile.
     *
     * @throws ResponseStatusException 401 if not authenticated, 404 if no commerce profile
     */
    public Commerce resolveCommerce(final Authentication authentication) {
        final User user = resolveUser(authentication);
        return commerceService.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    /**
     * Resolves the authenticated user's {@link Commerce} profile from an AuthUser principal.
     *
     * @throws ResponseStatusException 401 if not authenticated, 404 if no commerce profile
     */
    public Commerce resolveCommerce(final AuthUser principal) {
        final User user = resolveUser(principal);
        return commerceService.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    /**
     * Asserts the authenticated user has the given role.
     *
     * @throws ResponseStatusException 403 if role doesn't match
     */
    public User requireRole(final Authentication authentication, final User.Role requiredRole) {
        final User user = resolveUser(authentication);
        if (user.getRole() != requiredRole) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return user;
    }

    /**
     * Asserts the authenticated user is a commerce and owns the pack identified by {@code commerceUserId}.
     *
     * @throws ResponseStatusException 403 if not the owner
     */
    public void requireCommerceOwnership(final Authentication authentication, final long expectedCommerceUserId) {
        final User user = requireRole(authentication, User.Role.COMMERCE);
        if (user.getId() != expectedCommerceUserId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }
}
