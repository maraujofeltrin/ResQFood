package ar.edu.itba.paw.webapp.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

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
