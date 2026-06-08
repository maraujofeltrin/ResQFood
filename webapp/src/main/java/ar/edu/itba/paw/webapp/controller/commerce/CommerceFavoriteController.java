package ar.edu.itba.paw.webapp.controller.commerce;

import ar.edu.itba.paw.services.commerce.CommerceFavoriteService;
import ar.edu.itba.paw.webapp.auth.AuthUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class CommerceFavoriteController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommerceFavoriteController.class);

    private final CommerceFavoriteService commerceFavoriteService;

    @Autowired
    public CommerceFavoriteController(final CommerceFavoriteService commerceFavoriteService) {
        this.commerceFavoriteService = commerceFavoriteService;
    }

    @PostMapping("/commerces/{commerceId}/favorite")
    public String toggleFavorite(
            @PathVariable("commerceId") final long commerceId,
            @RequestHeader(value = "Referer", required = false) final String referer,
            @AuthenticationPrincipal final AuthUser principal) {
        final long clientUserId = principal.getId();
        try {
            commerceFavoriteService.toggleFavorite(clientUserId, commerceId);
        } catch (final IllegalArgumentException ex) {
            LOGGER.debug("Commerce favorite toggle rejected clientId={} commerceId={}", Long.valueOf(clientUserId),
                    Long.valueOf(commerceId), ex);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (referer != null && !referer.isEmpty()) {
            try {
                final java.net.URL refUrl = new java.net.URL(referer);
                return "redirect:" + refUrl.getFile();
            } catch (final java.net.MalformedURLException e) {
                // Fallback to default
            }
        }
        return "redirect:/commerces/" + commerceId;
    }
}
