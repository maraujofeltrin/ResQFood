package ar.edu.itba.paw.webapp.controller.pack;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import ar.edu.itba.paw.webapp.controller.helpers.PackCatalogModelBuilder;
import ar.edu.itba.paw.webapp.form.CatalogFilterForm;

@Controller
public class PackCatalogController {

    private final PackCatalogModelBuilder packCatalogModelBuilder;

    @Autowired
    public PackCatalogController(final PackCatalogModelBuilder packCatalogModelBuilder) {
        this.packCatalogModelBuilder = packCatalogModelBuilder;
    }

    @GetMapping("/packs")
    public ModelAndView listPacks(final CatalogFilterForm form) {
        return packCatalogModelBuilder.buildPackCatalog(form);
    }
}
