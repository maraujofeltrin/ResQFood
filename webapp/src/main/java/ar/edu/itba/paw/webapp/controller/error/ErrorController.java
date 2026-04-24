package ar.edu.itba.paw.webapp.controller.error;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/errors")
public class ErrorController {

    @RequestMapping("/400")
    public String badRequest() {
        return "errors/400";
    }

    @RequestMapping("/403")
    public String forbidden() {
        return "errors/403";
    }

    @RequestMapping("/404")
    public String notFound() {
        return "errors/404";
    }

    @RequestMapping("/500")
    public String internalServerError() {
        return "errors/500";
    }
}
