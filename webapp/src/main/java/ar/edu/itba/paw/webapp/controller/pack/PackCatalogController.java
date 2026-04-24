package ar.edu.itba.paw.webapp.controller.pack;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import ar.edu.itba.paw.webapp.controller.utils.PackCatalogUtils;

@Controller
public class PackCatalogController {

    private final PackCatalogUtils packCatalogUtils;

    @Autowired
    public PackCatalogController(final PackCatalogUtils packCatalogUtils) {
        this.packCatalogUtils = packCatalogUtils;
    }

    @GetMapping("/packs")
    public ModelAndView listPacks(
            @RequestParam(value = "q", required = false) final String query,
            @RequestParam(value = "tags", required = false) final List<String> tagNames,
            @RequestParam(value = "sort", required = false) final String sort,
            @RequestParam(value = "types", required = false) final List<String> types,
            @RequestParam(value = "auctionSort", required = false) final String auctionSort,
            @RequestParam(value = "location", required = false) final String locationParam,
            @RequestParam(value = "timeRange", required = false) final List<String> timeRange,
            @RequestParam(value = "page", defaultValue = "1") final int page) {
        return packCatalogUtils.buildPackCatalog(query, tagNames, sort, types, auctionSort, locationParam, timeRange, page);
    }
}
