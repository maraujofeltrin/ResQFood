package ar.edu.itba.paw.webapp.controller.commerce;

import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.services.commerce.CommerceFavoriteService;
import ar.edu.itba.paw.webapp.controller.helpers.AuthenticatedUserResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class CommerceFavoriteController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommerceFavoriteController.class);

    private final CommerceFavoriteService commerceFavoriteService;
    private final AuthenticatedUserResolver authResolver;

    @Autowired
    public CommerceFavoriteController(final CommerceFavoriteService commerceFavoriteService,
                                      final AuthenticatedUserResolver authResolver) {
        this.commerceFavoriteService = commerceFavoriteService;
        this.authResolver = authResolver;
    }

    @PostMapping("/commerces/{commerceId}/favorite")
    public String toggleFavorite(
            @PathVariable("commerceId") final long commerceId,
            final Authentication authentication) {
        final User user = authResolver.resolveUser(authentication);
        try {
            commerceFavoriteService.toggleFavorite(user.getId(), commerceId);
        } catch (final IllegalArgumentException ex) {
            LOGGER.debug("Commerce favorite toggle rejected clientId={} commerceId={}", Long.valueOf(user.getId()),
                    Long.valueOf(commerceId), ex);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return "redirect:/commerces/" + commerceId;
    }
}
