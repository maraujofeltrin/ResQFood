package ar.edu.itba.paw.webapp.controller.error;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ModelAndView handleMaxUploadSize(final MaxUploadSizeExceededException e,
            final RedirectAttributes redirectAttributes) {
        return new ModelAndView("redirect:/commerce/create-pack?error=maxUploadSize");
    }

    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public void handleMethodNotSupported(final org.springframework.web.HttpRequestMethodNotSupportedException e, final HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public void handleResponseStatus(final ResponseStatusException e,
            final HttpServletResponse response) throws IOException {
        response.sendError(e.getStatus().value());
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(final Exception e) {
        e.printStackTrace();
        return new ModelAndView("errors/500");
    }
}
