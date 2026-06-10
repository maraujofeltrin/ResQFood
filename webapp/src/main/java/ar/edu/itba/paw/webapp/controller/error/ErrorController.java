package ar.edu.itba.paw.webapp.controller.error;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/errors")
public class ErrorController {

    @RequestMapping("/400")
    public String badRequest(final HttpServletRequest request) {
        request.setAttribute("javax.servlet.error.status_code", Integer.valueOf(400));
        return "errors/error";
    }

    @RequestMapping("/403")
    public String forbidden(final HttpServletRequest request) {
        request.setAttribute("javax.servlet.error.status_code", Integer.valueOf(403));
        return "errors/error";
    }

    @RequestMapping("/404")
    public String notFound(final HttpServletRequest request) {
        request.setAttribute("javax.servlet.error.status_code", Integer.valueOf(404));
        return "errors/error";
    }

    @RequestMapping("/500")
    public String internalServerError(final HttpServletRequest request) {
        request.setAttribute("javax.servlet.error.status_code", Integer.valueOf(500));
        return "errors/error";
    }
}
