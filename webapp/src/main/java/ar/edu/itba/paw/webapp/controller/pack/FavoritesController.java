package ar.edu.itba.paw.webapp.controller.pack;

import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.services.commerce.CommerceService;
import ar.edu.itba.paw.services.pack.PackFavoriteService;
import ar.edu.itba.paw.webapp.controller.helpers.AuthenticatedUserResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class FavoritesController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FavoritesController.class);
    private static final int PAGE_SIZE = 9;

    private final PackFavoriteService packFavoriteService;
    private final CommerceService commerceService;
    private final AuthenticatedUserResolver authResolver;

    @Autowired
    public FavoritesController(final PackFavoriteService packFavoriteService,
                               final CommerceService commerceService,
                               final AuthenticatedUserResolver authResolver) {
        this.packFavoriteService = packFavoriteService;
        this.commerceService = commerceService;
        this.authResolver = authResolver;
    }

    @GetMapping("/favorites")
    public ModelAndView favorites(
            @RequestParam(value = "page", defaultValue = "1") final int page,
            final Authentication authentication) {

        final User user = authResolver.resolveUser(authentication);
        final long userId = user.getId();

        LOGGER.debug("Loading favorites page={} for userId={}", page, userId);

        final int totalItems = packFavoriteService.countActiveFavoritePacks(userId);
        final int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / PAGE_SIZE));
        final int safePage = Math.max(1, Math.min(page, totalPages));

        final List<Pack> packs;
        if (totalItems > 0) {
            packs = packFavoriteService.listActiveFavoritePacks(userId, safePage, PAGE_SIZE);
        } else {
            packs = Collections.emptyList();
        }

        // Enrich with commerce names
        final Map<Long, String> commerceNames = new HashMap<>();
        for (final Pack pack : packs) {
            commerceNames.putIfAbsent(
                    pack.getId(),
                    commerceService.findByUserId(pack.getCommerceId())
                            .map(Commerce::getCommercialName)
                            .orElse("—"));
        }

        final ModelAndView mav = new ModelAndView("favorites/favoritesView");
        mav.addObject("packs", packs);
        mav.addObject("commerceNames", commerceNames);
        mav.addObject("currentPage", safePage);
        mav.addObject("totalPages", totalPages);
        mav.addObject("totalFavorites", totalItems);

        return mav;
    }
}
