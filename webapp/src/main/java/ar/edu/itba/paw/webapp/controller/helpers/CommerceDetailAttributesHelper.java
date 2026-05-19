package ar.edu.itba.paw.webapp.controller.helpers;

import ar.edu.itba.paw.models.user.Commerce;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import java.time.ZoneId;
import java.util.Optional;

@Component
public class CommerceDetailAttributesHelper {

    private final ZoneId businessZone;

    public CommerceDetailAttributesHelper(final ZoneId businessZone) {
        this.businessZone = businessZone;
    }

    public void addCommerceDetailAttributes(final ModelAndView mav, final Optional<Commerce> commerceOpt) {
        final Commerce commerce = commerceOpt.orElse(null);
        final String commercialName = commerce != null && commerce.getCommercialName() != null
                && !commerce.getCommercialName().isBlank()
                ? commerce.getCommercialName().trim() : "—";
        mav.addObject("commerceCommercialName", commercialName);
        mav.addObject("commerceStreetLine", commerce != null ? commerce.getFullStreetLine() : "—");
        mav.addObject("commerceLocationLine", commerce != null ? commerce.getCityProvincePostal() : "—");
        mav.addObject("commerceOpeningTime", dashIfBlank(commerce != null ? commerce.getOpeningTime() : null));
        mav.addObject("commerceClosingTime", dashIfBlank(commerce != null ? commerce.getClosingTime() : null));
        mav.addObject("commerceOpenNow", Boolean.valueOf(commerce != null && commerce.isOpenNow(businessZone)));
    }

    private static String dashIfBlank(final String value) {
        if (value == null || value.isBlank()) {
            return "—";
        }
        return value.trim();
    }
}
