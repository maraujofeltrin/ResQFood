package ar.edu.itba.paw.webapp.controller.pack;

import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.services.pack.PackFavoriteService;
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
public class PackFavoriteController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PackFavoriteController.class);

    private final PackFavoriteService packFavoriteService;
    private final AuthenticatedUserResolver authResolver;

    @Autowired
    public PackFavoriteController(final PackFavoriteService packFavoriteService,
            final AuthenticatedUserResolver authResolver) {
        this.packFavoriteService = packFavoriteService;
        this.authResolver = authResolver;
    }

    @PostMapping("/packs/{packId}/favorite")
    public String toggleFavorite(
            @PathVariable("packId") final long packId,
            final Authentication authentication) {
        final User user = authResolver.resolveUser(authentication);
        try {
            packFavoriteService.toggleFavorite(user.getId(), packId);
        } catch (final IllegalArgumentException ex) {
            LOGGER.debug("Favorite toggle rejected clientId={} packId={}", Long.valueOf(user.getId()),
                    Long.valueOf(packId), ex);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return "redirect:/packs/" + packId;
    }
}
