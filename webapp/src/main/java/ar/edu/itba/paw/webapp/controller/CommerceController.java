package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.models.Commerce;
import ar.edu.itba.paw.models.Pack;
import ar.edu.itba.paw.models.PackTag;
import ar.edu.itba.paw.services.AuctionService;
import ar.edu.itba.paw.services.CommerceService;
import ar.edu.itba.paw.services.PackService;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Collections;
import java.util.Set;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.services.UserService;

import ar.edu.itba.paw.webapp.form.CreatePackForm;
import ar.edu.itba.paw.webapp.form.CreateAuctionForm;

@Controller
@RequestMapping("/commerce")
public class CommerceController {

    private static final int PAGE_SIZE = 6;
    private static final Set<String> ALLOWED_IMAGE_TYPES = new HashSet<>(Arrays.asList(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    ));
    private static final Set<String> COMMERCE_FIELDS = new HashSet<>(Arrays.asList(
            "name", "commercialName", "category", "street", "streetNumber",
            "postalCode", "city", "province", "openingTime", "closingTime"
    ));

    private final CommerceService commerceService;
    private final PackService packService;
    private final AuctionService auctionService;
    private final MessageSource messageSource;
    private final UserService userService;

    @Autowired
    public CommerceController(final CommerceService commerceService, final PackService packService,
                              final AuctionService auctionService, final MessageSource messageSource,
                              final UserService userService) {
        this.commerceService = commerceService;
        this.packService = packService;
        this.auctionService = auctionService;
        this.messageSource = messageSource;
        this.userService = userService;
    }

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

    // ── Create Pack ──────────────────────────────────────────

    @RequestMapping(value = "/create-pack", method = RequestMethod.GET)
    public ModelAndView createPackForm(@ModelAttribute("createPackForm") final CreatePackForm form,
                                      @RequestParam(value = "error", required = false) final String error) {
        final ModelAndView mav = new ModelAndView("commerce/createPack");
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
            @Valid @ModelAttribute("createPackForm") final CreatePackForm form,
            final BindingResult bindingResult) {

        if (form.getOriginalPrice() != null && form.getFinalPrice() != null && form.getFinalPrice() > form.getOriginalPrice()) {
            bindingResult.rejectValue("finalPrice", "error.finalPrice", "El precio de venta no puede ser mayor al precio original");
        }

        validateImage(form.getImage(), bindingResult);

        final BindingResult finalBindingResult = filterCommerceErrors(form.getEmail(), form, "createPackForm", bindingResult);

        if (finalBindingResult.hasErrors()) {
            final ModelAndView mav = new ModelAndView("commerce/createPack");
            mav.addObject("availableTags", PackTag.values());
            mav.addObject(BindingResult.MODEL_KEY_PREFIX + "createPackForm", finalBindingResult);
            return mav;
        }

        try {
            Commerce commerce = commerceService.getOrCreateCommerce(
                    form.getEmail(), "mvp", form.getName(), form.getCommercialName(), form.getCategory(), form.getStreet(), form.getStreetNumber(), 
                    form.getCity(), form.getProvince(), form.getPostalCode(), form.getOpeningTime(), form.getClosingTime()
            );

            byte[] imageData = null;
            String imageContentType = null;
            final MultipartFile image = form.getImage();
            if (image != null && !image.isEmpty()) {
                imageData = image.getBytes();
                imageContentType = image.getContentType();
            }

            packService.createPack(commerce.getUserId(), form.getTitle(), form.getDescription(), 
                                   form.getOriginalPrice(), form.getFinalPrice(), form.getStock(),
                                   form.getTags() != null ? form.getTags() : Collections.emptyList(),
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
            mav.addObject("errorMessage",
                    messageSource.getMessage("commerce.createPack.validation.image.processError",
                            null, LocaleContextHolder.getLocale()));
            return mav;
        }
    }

    // ── Create Auction ───────────────────────────────────────

    @RequestMapping(value = "/create-auction", method = RequestMethod.GET)
    public ModelAndView createAuctionForm(@ModelAttribute("createAuctionForm") final CreateAuctionForm form,
                                         @RequestParam(value = "error", required = false) final String error) {
        final ModelAndView mav = new ModelAndView("commerce/createAuction");
        mav.addObject("availableTags", PackTag.values());
        if ("maxUploadSize".equals(error)) {
            mav.addObject("errorMessage",
                    messageSource.getMessage("commerce.createPack.validation.image.maxSize",
                            null, LocaleContextHolder.getLocale()));
        }
        return mav;
    }

    @RequestMapping(value = "/create-auction", method = RequestMethod.POST)
    public ModelAndView createAuction(
            @Valid @ModelAttribute("createAuctionForm") final CreateAuctionForm form,
            final BindingResult bindingResult) {

        // Validate initial price does not exceed original price
        if (form.getOriginalPrice() != null && form.getInitialPrice() != null
                && form.getInitialPrice() > form.getOriginalPrice()) {
            bindingResult.rejectValue("initialPrice", "error.initialPrice",
                    messageSource.getMessage("commerce.createAuction.validation.initialPrice.exceedsOriginal",
                            null, LocaleContextHolder.getLocale()));
        }

        validateImage(form.getImage(), bindingResult);

        // Parse and validate auction end date/time
        LocalDateTime endDateTime = null;
        if (form.getEndDate() != null && !form.getEndDate().isBlank()
                && form.getEndTime() != null && !form.getEndTime().isBlank()) {
            try {
                endDateTime = LocalDateTime.parse(form.getEndDate() + "T" + form.getEndTime());
                if (endDateTime.isBefore(LocalDateTime.now())) {
                    bindingResult.rejectValue("endDate", "error.endDate",
                            messageSource.getMessage("commerce.createAuction.validation.endDateTime.future",
                                    null, LocaleContextHolder.getLocale()));
                }
            } catch (Exception e) {
                bindingResult.rejectValue("endDate", "error.endDate",
                        messageSource.getMessage("commerce.createAuction.validation.endDateTime.invalid",
                                null, LocaleContextHolder.getLocale()));
            }
        }

        final BindingResult finalBindingResult = filterCommerceErrors(form.getEmail(), form, "createAuctionForm", bindingResult);

        if (finalBindingResult.hasErrors()) {
            final ModelAndView mav = new ModelAndView("commerce/createAuction");
            mav.addObject("availableTags", PackTag.values());
            mav.addObject(BindingResult.MODEL_KEY_PREFIX + "createAuctionForm", finalBindingResult);
            return mav;
        }

        try {
            Commerce commerce = commerceService.getOrCreateCommerce(
                    form.getEmail(), "mvp", form.getName(), form.getCommercialName(),
                    form.getCategory(), form.getStreet(), form.getStreetNumber(),
                    form.getCity(), form.getProvince(), form.getPostalCode(),
                    form.getOpeningTime(), form.getClosingTime()
            );

            byte[] imageData = null;
            String imageContentType = null;
            final MultipartFile image = form.getImage();
            if (image != null && !image.isEmpty()) {
                imageData = image.getBytes();
                imageContentType = image.getContentType();
            }

            // Create the underlying pack (stock=1, finalPrice=initialPrice as floor)
            Pack pack = packService.createPack(
                    commerce.getUserId(), form.getTitle(), form.getDescription(),
                    form.getOriginalPrice(), form.getInitialPrice(), 1,
                    form.getTags() != null ? form.getTags() : Collections.emptyList(),
                    imageData, imageContentType
            );

            // Create the auction wrapping the pack
            auctionService.createAuction(pack.getId(), form.getInitialPrice(), endDateTime);

            return new ModelAndView("redirect:/commerce");

        } catch (IllegalArgumentException e) {
            final ModelAndView mav = new ModelAndView("commerce/createAuction");
            mav.addObject("availableTags", PackTag.values());
            mav.addObject("errorMessage", e.getMessage());
            return mav;
        } catch (IOException e) {
            final ModelAndView mav = new ModelAndView("commerce/createAuction");
            mav.addObject("availableTags", PackTag.values());
            mav.addObject("errorMessage",
                    messageSource.getMessage("commerce.createPack.validation.image.processError",
                            null, LocaleContextHolder.getLocale()));
            return mav;
        }
    }

    // ── Helpers ───────────────────────────────────────────────

    /**
     * Server-side image type validation.
     */
    private void validateImage(final MultipartFile image, final BindingResult bindingResult) {
        if (image != null && !image.isEmpty()) {
            final String contentType = image.getContentType();
            if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
                bindingResult.rejectValue("image", "error.image.invalidType",
                        messageSource.getMessage("commerce.createPack.validation.image.invalidType",
                                null, LocaleContextHolder.getLocale()));
            }
        }
    }

    /**
     * If the user's email corresponds to an existing commerce, filters out validation
     * errors for commerce-specific fields (name, address, hours, etc.), since those
     * fields are ignored for returning users.
     */
    private BindingResult filterCommerceErrors(final String email, final Object target,
                                               final String objectName, final BindingResult bindingResult) {
        boolean isExistingCommerce = false;
        if (email != null) {
            java.util.Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isPresent() && userOpt.get().getRole() == User.Role.COMMERCE) {
                if (commerceService.findByUserId(userOpt.get().getId()).isPresent()) {
                    isExistingCommerce = true;
                }
            }
        }

        if (!isExistingCommerce || !bindingResult.hasErrors()) {
            return bindingResult;
        }

        BeanPropertyBindingResult filteredResult = new BeanPropertyBindingResult(target, objectName);
        for (FieldError error : bindingResult.getFieldErrors()) {
            if (!COMMERCE_FIELDS.contains(error.getField())) {
                filteredResult.addError(error);
            }
        }
        for (ObjectError error : bindingResult.getGlobalErrors()) {
            filteredResult.addError(error);
        }
        return filteredResult;
    }
}
