package ar.edu.itba.paw.webapp.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HelloWorldController {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.!#$%&'*+/=?^`{|}~-]+@[\\w-]+(?:\\.[\\w-]+)+$");

    @GetMapping("/")
    public ModelAndView helloWorld() {
        final ModelAndView mav = buildBaseModel();
        mav.addObject("searchFieldValue", "");
        mav.addObject("emailFieldValue", "");
        mav.addObject("inputErrors", Map.of());
        return mav;
    }

    @PostMapping("/")
    public ModelAndView validateInputs(
            @RequestParam(name = "searchField", required = false) final String searchField,
            @RequestParam(name = "emailField", required = false) final String emailField) {
        final ModelAndView mav = buildBaseModel();
        final Map<String, String> inputErrors = new HashMap<>();

        final String normalizedSearchField = searchField != null ? searchField.trim() : "";
        final String normalizedEmailField = emailField != null ? emailField.trim() : "";

        if (normalizedSearchField.isBlank()) {
            inputErrors.put("searchField", "La categoría es obligatoria.");
        } else if (normalizedSearchField.length() < 3 || normalizedSearchField.length() > 40) {
            inputErrors.put("searchField", "La categoría debe tener entre 3 y 40 caracteres.");
        }

        if (normalizedEmailField.isBlank()) {
            inputErrors.put("emailField", "El email es obligatorio.");
        } else if (!EMAIL_PATTERN.matcher(normalizedEmailField).matches()) {
            inputErrors.put("emailField", "Ingresá un email válido.");
        }

        mav.addObject("searchFieldValue", normalizedSearchField);
        mav.addObject("emailFieldValue", normalizedEmailField);
        mav.addObject("inputErrors", inputErrors);
        return mav;
    }

    private ModelAndView buildBaseModel() {
        final ModelAndView mav = new ModelAndView("index");
        mav.addObject("greeting", "pancho");

        mav.addObject("landingTitle", "PAW-2026a-03");

        mav.addObject("cardCategory", "Comida rápida");
        mav.addObject("cardHeading", "McDonald's");
        mav.addObject("cardRating", 3.7);
        mav.addObject("cardImageUrl",
                "https://images.rappi.com.ar/restaurants_background/mcdonaldscol-1660251198623.jpg");

        mav.addObject("cardCategory2", "Restaurante");
        mav.addObject("cardHeading2", "Kansas");
        mav.addObject("cardRating2", 4.5);

        mav.addObject("modalTitle", "Pack sorpresa");
        mav.addObject("modalContent", "Puede incluir: hamburguesa, papas y bebida. Por $5000");
        return mav;
    }
}
