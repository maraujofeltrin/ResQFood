package ar.edu.itba.paw.webapp.controller.error;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ar.edu.itba.paw.services.security.OwnershipResourceNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ModelAndView handleMaxUploadSize(final MaxUploadSizeExceededException e,
            final HttpServletRequest request,
            final RedirectAttributes redirectAttributes) {
        LOGGER.warn("Max upload size exceeded: {} {}", request.getMethod(), request.getRequestURI());
        return new ModelAndView("redirect:/commerce/create-offer?error=maxUploadSize");
    }

    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public void handleMethodNotSupported(final org.springframework.web.HttpRequestMethodNotSupportedException e,
            final HttpServletRequest request,
            final HttpServletResponse response) throws IOException {
        final java.util.Collection<org.springframework.http.HttpMethod> supported = e.getSupportedHttpMethods();
        LOGGER.debug("HTTP method not supported: {} {} (supported={})", request.getMethod(), request.getRequestURI(),
                supported);
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public void handleResponseStatus(final ResponseStatusException e,
            final HttpServletRequest request,
            final HttpServletResponse response) throws IOException {
        LOGGER.debug("Response status {} for {} {}", Integer.valueOf(e.getStatus().value()), request.getMethod(),
                request.getRequestURI(), e);
        response.sendError(e.getStatus().value());
    }

    @ExceptionHandler(org.springframework.beans.TypeMismatchException.class)
    public void handleTypeMismatch(final org.springframework.beans.TypeMismatchException e,
            final HttpServletRequest request,
            final HttpServletResponse response) throws IOException {
        LOGGER.debug("Type mismatch for {} {}: {}", request.getMethod(), request.getRequestURI(), e.getMessage());
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public void handleAccessDenied(final AccessDeniedException e,
            final HttpServletRequest request,
            final HttpServletResponse response) throws IOException {
        LOGGER.debug("Access denied for {} {}", request.getMethod(), request.getRequestURI());
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    @ExceptionHandler(OwnershipResourceNotFoundException.class)
    public void handleOwnershipResourceNotFound(final OwnershipResourceNotFoundException e,
            final HttpServletRequest request,
            final HttpServletResponse response) throws IOException {
        LOGGER.debug("Ownership resource not found for {} {}: {}", request.getMethod(), request.getRequestURI(),
                e.getMessage());
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(final Exception e, final HttpServletRequest request) {
        LOGGER.error("Unhandled exception handling {} {}", request.getMethod(), request.getRequestURI(), e);
        return new ModelAndView("errors/500");
    }
}
