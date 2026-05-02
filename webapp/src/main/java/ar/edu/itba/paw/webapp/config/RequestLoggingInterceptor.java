package ar.edu.itba.paw.webapp.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestLoggingInterceptor.class);
    private static final String START_NANO_ATTR = RequestLoggingInterceptor.class.getName() + ".startNano";

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response,
            final Object handler) {
        request.setAttribute(START_NANO_ATTR, Long.valueOf(System.nanoTime()));
        return true;
    }

    @Override
    public void afterCompletion(final HttpServletRequest request, final HttpServletResponse response,
            final Object handler, final Exception ex) {
        final Long startNano = (Long) request.getAttribute(START_NANO_ATTR);
        final long elapsedMs = startNano == null ? -1L : (System.nanoTime() - startNano.longValue()) / 1_000_000L;
        
        final String method = request.getMethod();
        final int status = response.getStatus();
        final String uri = request.getRequestURI();
        
        // Obtener el usuario del contexto de seguridad de forma liviana (sin DB hit)
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        final String user = (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) 
                ? " [User:" + auth.getName() + "]" : "";

        // Determinamos si es una acción de cambio de estado (POST, PUT, DELETE, PATCH)
        final boolean isAction = !"GET".equalsIgnoreCase(method) && !"HEAD".equalsIgnoreCase(method) && !"OPTIONS".equalsIgnoreCase(method);

        if (isAction) {
            LOGGER.info("{}{} {} completed status={} in {} ms", method, user, uri, Integer.valueOf(status), Long.valueOf(elapsedMs));
        } else if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{}{} {} completed status={} in {} ms", method, user, uri, Integer.valueOf(status), Long.valueOf(elapsedMs));
        }
    }
}
