package ar.edu.itba.paw.webapp.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        if (!LOGGER.isDebugEnabled()) {
            return;
        }
        final Long startNano = (Long) request.getAttribute(START_NANO_ATTR);
        final long elapsedMs = startNano == null ? -1L : (System.nanoTime() - startNano.longValue()) / 1_000_000L;
        LOGGER.debug("{} {} completed status={} in {} ms", request.getMethod(), request.getRequestURI(),
                Integer.valueOf(response.getStatus()), Long.valueOf(elapsedMs));
    }
}
