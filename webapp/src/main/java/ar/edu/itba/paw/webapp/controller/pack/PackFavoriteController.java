package ar.edu.itba.paw.webapp.controller.pack;

import ar.edu.itba.paw.models.pack.FavoriteToggleException;
import ar.edu.itba.paw.services.pack.PackFavoriteService;
import ar.edu.itba.paw.webapp.auth.AuthUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PackFavoriteController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PackFavoriteController.class);

    private final PackFavoriteService packFavoriteService;

    @Autowired
    public PackFavoriteController(final PackFavoriteService packFavoriteService) {
        this.packFavoriteService = packFavoriteService;
    }

    @PostMapping("/packs/{packId}/favorite")
    public String toggleFavorite(
            @PathVariable("packId") final long packId,
            @AuthenticationPrincipal final AuthUser principal,
            final RedirectAttributes redirectAttributes) {
        final long clientUserId = principal.getId();
        try {
            packFavoriteService.toggleFavorite(clientUserId, packId);
        } catch (final FavoriteToggleException ex) {
            LOGGER.debug("Favorite toggle rejected clientId={} packId={} reason={}", Long.valueOf(clientUserId),
                    Long.valueOf(packId), ex.getReason(), ex);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return "redirect:/packs/" + packId;
    }
}
