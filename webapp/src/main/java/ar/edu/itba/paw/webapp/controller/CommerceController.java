package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.models.Commerce;
import ar.edu.itba.paw.models.Pack;
import ar.edu.itba.paw.models.PackTag;
import ar.edu.itba.paw.services.CommerceService;
import ar.edu.itba.paw.services.PackService;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Collections;
import java.util.Set;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.webapp.auth.AuthUser;
import ar.edu.itba.paw.webapp.form.CreatePackForm;
import ar.edu.itba.paw.webapp.form.EditPackForm;

@Controller
@RequestMapping("/commerce")
public class CommerceController {

    private static final int PAGE_SIZE = 6;
    private static final Set<String> ALLOWED_IMAGE_TYPES = new HashSet<>(Arrays.asList(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    ));

    private final CommerceService commerceService;
    private final PackService packService;
    private final MessageSource messageSource;
    private final UserService userService;

    @Autowired
    public CommerceController(final CommerceService commerceService, final PackService packService,
                              final MessageSource messageSource, final UserService userService) {
        this.commerceService = commerceService;
        this.packService = packService;
        this.messageSource = messageSource;
        this.userService = userService;
    }

    /**
     * Entry point: renderiza el dashboard del commerce autenticado.
     */
    @GetMapping(value = "")
    public ModelAndView dashboard(@AuthenticationPrincipal final AuthUser principal,
                                  @RequestParam(value = "page", defaultValue = "1") final int page,
                                  @RequestParam(value = "tab", defaultValue = "items") final String tab) {
        final long id = getAuthenticatedUser(principal).getId();

        final java.util.Optional<Commerce> commerceOpt = commerceService.findByUserId(id);
        if (!commerceOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        final Commerce commerce = commerceOpt.get();
        final ModelAndView mav = new ModelAndView("commerce/dashboard");

        final List<Pack> allPacks = packService.findByCommerceId(id);
        
        // As per requirements: Items (all), Packs (packs only - currently all packs), Subasta (empty)
        final List<Pack> displayedPacks;
        if ("auctions".equalsIgnoreCase(tab)) {
            displayedPacks = java.util.Collections.emptyList();
        } else {
            displayedPacks = allPacks;
        }

        final int totalPages = Math.max(1, (int) Math.ceil((double) displayedPacks.size() / PAGE_SIZE));
        final int safePage = Math.max(1, Math.min(page, totalPages));
        final int fromIdx = (safePage - 1) * PAGE_SIZE;
        final int toIdx = Math.min(fromIdx + PAGE_SIZE, displayedPacks.size());

        mav.addObject("commerce", commerce);
        mav.addObject("packs", displayedPacks.subList(fromIdx, toIdx));
        mav.addObject("currentPage", safePage);
        mav.addObject("totalPages", totalPages);
        mav.addObject("commerceId", id);
        mav.addObject("currentTab", tab);
        mav.addObject("paginationBaseUrl", "/commerce?tab=" + tab);
        return mav;
    }

    @RequestMapping(value = "/create-pack", method = RequestMethod.GET)
    public ModelAndView createPackForm(@AuthenticationPrincipal final AuthUser principal,
                                      @ModelAttribute("createPackForm") final CreatePackForm form,
                                      @RequestParam(value = "error", required = false) final String error) {
        final long commerceId = getAuthenticatedUser(principal).getId();

        if (commerceService.findByUserId(commerceId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        final ModelAndView mav = new ModelAndView("commerce/createPack");
        mav.addObject("commerceId", commerceId);
        mav.addObject("availableTags", PackTag.values());
        if ("maxUploadSize".equals(error)) {
            mav.addObject("errorMessage",
                    messageSource.getMessage("commerce.createPack.validation.image.maxSize",
                            null, LocaleContextHolder.getLocale()));
        }
        return mav;
    }

    @RequestMapping(value = "/create-pack", method = RequestMethod.POST)
    public ModelAndView createPack(
            @AuthenticationPrincipal final AuthUser principal,
            @Valid @ModelAttribute("createPackForm") final CreatePackForm form,
            final BindingResult bindingResult) {

        final long commerceId = getAuthenticatedUser(principal).getId();

        if (commerceService.findByUserId(commerceId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (form.getOriginalPrice() != null && form.getFinalPrice() != null && form.getFinalPrice() > form.getOriginalPrice()) {
            bindingResult.rejectValue("finalPrice", "error.finalPrice", "El precio de venta no puede ser mayor al precio original");
        }

        // Server-side image type validation
        final MultipartFile image = form.getImage();
        if (image != null && !image.isEmpty()) {
            final String contentType = image.getContentType();
            if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
                bindingResult.rejectValue("image", "error.image.invalidType",
                        messageSource.getMessage("commerce.createPack.validation.image.invalidType",
                                null, LocaleContextHolder.getLocale()));
            }
        }

        if (bindingResult.hasErrors()) {
            final ModelAndView mav = new ModelAndView("commerce/createPack");
            mav.addObject("commerceId", commerceId);
            mav.addObject("availableTags", PackTag.values());
            return mav;
        }

        try {

            byte[] imageData = null;
            String imageContentType = null;
            if (image != null && !image.isEmpty()) {
                imageData = image.getBytes();
                imageContentType = image.getContentType();
            }

            packService.createPack(commerceId, form.getTitle(), form.getDescription(), 
                                   form.getOriginalPrice(), form.getFinalPrice(), form.getStock(),
                                   form.getTags() != null ? form.getTags() : Collections.emptyList(),
                                   imageData, imageContentType);
                                   
            return new ModelAndView("redirect:/commerce");

        } catch (IllegalArgumentException e) {
            final ModelAndView mav = new ModelAndView("commerce/createPack");
            mav.addObject("commerceId", commerceId);
            mav.addObject("availableTags", PackTag.values());
            mav.addObject("errorMessage", e.getMessage());
            return mav;
        } catch (IOException e) {
            final ModelAndView mav = new ModelAndView("commerce/createPack");
            mav.addObject("commerceId", commerceId);
            mav.addObject("availableTags", PackTag.values());
            mav.addObject("errorMessage",
                    messageSource.getMessage("commerce.createPack.validation.image.processError",
                            null, LocaleContextHolder.getLocale()));
            return mav;
        }
    }

    @RequestMapping(value = "/edit-pack/{packId}", method = RequestMethod.GET)
    public ModelAndView editPackForm(@PathVariable("packId") final long packId,
                                     @AuthenticationPrincipal final AuthUser principal,
                                     @ModelAttribute("editPackForm") final EditPackForm form,
                                     @RequestParam(value = "error", required = false) final String error) {
        final long commerceId = getAuthenticatedUser(principal).getId();

        if (commerceService.findByUserId(commerceId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        final java.util.Optional<Pack> packOpt = packService.findById(packId);
        if (!packOpt.isPresent() || packOpt.get().getCommerceId() != commerceId) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        final Pack pack = packOpt.get();

        if (form.getTitle() == null) {
            form.setTitle(pack.getTitle());
            form.setDescription(pack.getDescription());
            form.setTags(pack.getTags());
            form.setOriginalPrice(pack.getOriginalPrice());
            form.setFinalPrice(pack.getFinalPrice());
            form.setStock(pack.getStock());
        }

        final ModelAndView mav = new ModelAndView("commerce/editPack");
        mav.addObject("commerceId", commerceId);
        mav.addObject("packId", packId);
        mav.addObject("availableTags", PackTag.values());
        
        if ("maxUploadSize".equals(error)) {
            mav.addObject("errorMessage",
                    messageSource.getMessage("commerce.createPack.validation.image.maxSize",
                            null, LocaleContextHolder.getLocale()));
        }

        return mav;
    }

    @RequestMapping(value = "/edit-pack/{packId}", method = RequestMethod.POST)
    public ModelAndView editPack(
            @PathVariable("packId") final long packId,
            @AuthenticationPrincipal final AuthUser principal,
            @Valid @ModelAttribute("editPackForm") final EditPackForm form,
            final BindingResult bindingResult) {

        final long commerceId = getAuthenticatedUser(principal).getId();

        if (commerceService.findByUserId(commerceId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        final java.util.Optional<Pack> packOpt = packService.findById(packId);
        if (!packOpt.isPresent() || packOpt.get().getCommerceId() != commerceId) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (form.getOriginalPrice() != null && form.getFinalPrice() != null && form.getFinalPrice() > form.getOriginalPrice()) {
            bindingResult.rejectValue("finalPrice", "error.finalPrice", "El precio de venta no puede ser mayor al precio original");
        }

        final MultipartFile image = form.getImage();
        if (image != null && !image.isEmpty()) {
            final String contentType = image.getContentType();
            if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
                bindingResult.rejectValue("image", "error.image.invalidType",
                        messageSource.getMessage("commerce.createPack.validation.image.invalidType",
                                null, LocaleContextHolder.getLocale()));
            }
        }

        if (bindingResult.hasErrors()) {
            final ModelAndView mav = new ModelAndView("commerce/editPack");
            mav.addObject("commerceId", commerceId);
            mav.addObject("packId", packId);
            mav.addObject("availableTags", PackTag.values());
            return mav;
        }

        try {
            Pack packToUpdate = packOpt.get();
            packToUpdate.setTitle(form.getTitle());
            packToUpdate.setDescription(form.getDescription());
            packToUpdate.setOriginalPrice(form.getOriginalPrice());
            packToUpdate.setFinalPrice(form.getFinalPrice());
            packToUpdate.setStock(form.getStock());
            packToUpdate.setTags(form.getTags() != null ? form.getTags() : Collections.emptyList());

            packService.update(packToUpdate);

            if (image != null && !image.isEmpty()) {
                packService.updateImage(packToUpdate.getId(), image.getBytes(), image.getContentType());
            }

            return new ModelAndView("redirect:/commerce");

        } catch (IllegalArgumentException e) {
            final ModelAndView mav = new ModelAndView("commerce/editPack");
            mav.addObject("commerceId", commerceId);
            mav.addObject("packId", packId);
            mav.addObject("availableTags", PackTag.values());
            mav.addObject("errorMessage", e.getMessage());
            return mav;
        } catch (IOException e) {
            final ModelAndView mav = new ModelAndView("commerce/editPack");
            mav.addObject("commerceId", commerceId);
            mav.addObject("packId", packId);
            mav.addObject("availableTags", PackTag.values());
            mav.addObject("errorMessage",
                    messageSource.getMessage("commerce.createPack.validation.image.processError",
                            null, LocaleContextHolder.getLocale()));
            return mav;
        }
    }

    @RequestMapping(value = "/delete-pack/{packId}", method = RequestMethod.POST)
    public ModelAndView deletePack(
            @PathVariable("packId") final long packId,
            @AuthenticationPrincipal final AuthUser principal) {

        final long commerceId = getAuthenticatedUser(principal).getId();

        if (commerceService.findByUserId(commerceId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        final java.util.Optional<Pack> packOpt = packService.findById(packId);
        if (!packOpt.isPresent() || packOpt.get().getCommerceId() != commerceId) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        packService.deletePack(packId);

        return new ModelAndView("redirect:/commerce");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Devuelve el User del principal autenticado.
     * Si por algún motivo el principal es nulo lanza 401.
     */
    private User getAuthenticatedUser(final AuthUser principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return userService.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }


}
