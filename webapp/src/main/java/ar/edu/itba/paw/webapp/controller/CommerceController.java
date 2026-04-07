package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.models.Commerce;
import ar.edu.itba.paw.models.Pack;
import ar.edu.itba.paw.models.PackTag;
import ar.edu.itba.paw.services.CommerceService;
import ar.edu.itba.paw.services.PackService;
import java.io.IOException;
import java.util.List;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/commerce")
public class CommerceController {

    private final CommerceService commerceService;
    private final PackService packService;

    @Autowired
    public CommerceController(final CommerceService commerceService, final PackService packService) {
        this.commerceService = commerceService;
        this.packService = packService;
    }

    private static final int PAGE_SIZE = 6;

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView dashboard(@RequestParam(value = "page", defaultValue = "1") final int page) {
        final ModelAndView mav = new ModelAndView("commerce/dashboard");

        final List<Pack> allPacks = packService.findAll();
        final int totalPages = Math.max(1, (int) Math.ceil((double) allPacks.size() / PAGE_SIZE));
        final int safePage = Math.max(1, Math.min(page, totalPages));
        final int fromIdx = (safePage - 1) * PAGE_SIZE;
        final int toIdx = Math.min(fromIdx + PAGE_SIZE, allPacks.size());

        mav.addObject("packs", allPacks.subList(fromIdx, toIdx));
        mav.addObject("currentPage", safePage);
        mav.addObject("totalPages", totalPages);
        mav.addObject("paginationBaseUrl", "/commerce");
        return mav;
    }

    @RequestMapping(value = "/create-pack", method = RequestMethod.GET)
    public ModelAndView createPackForm() {
        final ModelAndView mav = new ModelAndView("commerce/createPack");
        mav.addObject("availableTags", PackTag.values());
        return mav;
    }

    @RequestMapping(value = "/create-pack", method = RequestMethod.POST)
    public ModelAndView createPack(
            @RequestParam("email") final String email,
            @RequestParam("password") final String password,
            @RequestParam("name") final String name,
            @RequestParam(value = "commercialName", required = false) final String commercialName,
            @RequestParam(value = "category", required = false) final Commerce.Category category,
            @RequestParam(value = "street", required = false) final String street,
            @RequestParam(value = "streetNumber", required = false) final Integer streetNumber,
            @RequestParam(value = "city", required = false) final String city,
            @RequestParam(value = "province", required = false) final String province,
            @RequestParam(value = "postalCode", required = false) final String postalCode,
            @RequestParam(value = "openingTime", required = false) final String openingTime,
            @RequestParam(value = "closingTime", required = false) final String closingTime,
            @RequestParam("title") final String packTitle,
            @RequestParam("description") final String packDescription,
            @RequestParam("originalPrice") final Double originalPrice,
            @RequestParam("finalPrice") final Double finalPrice,
            @RequestParam("stock") final Integer stock,
            @RequestParam(value = "tags", required = false) final List<PackTag> tags,
            @RequestParam(value = "image", required = false) final MultipartFile image) {

        try {
            Commerce commerce = commerceService.getOrCreateCommerce(
                    email, password, name, commercialName, category, street, streetNumber, 
                    city, province, postalCode, openingTime, closingTime
            );

            byte[] imageData = null;
            String imageContentType = null;
            if (image != null && !image.isEmpty()) {
                imageData = image.getBytes();
                imageContentType = image.getContentType();
            }

            packService.createPack(commerce.getUserId(), packTitle, packDescription, 
                                   originalPrice, finalPrice, stock,
                                   tags != null ? tags : Collections.emptyList(),
                                   imageData, imageContentType);
                                   
            return new ModelAndView("redirect:/commerce");

        } catch (IllegalArgumentException e) {
            final ModelAndView mav = new ModelAndView("commerce/createPack");
            mav.addObject("availableTags", PackTag.values());
            mav.addObject("errorMessage", e.getMessage());
            return mav;
        } catch (IOException e) {
            final ModelAndView mav = new ModelAndView("commerce/createPack");
            mav.addObject("availableTags", PackTag.values());
            mav.addObject("errorMessage", "Error al procesar la imagen. Intente nuevamente.");
            return mav;
        }
    }
}
