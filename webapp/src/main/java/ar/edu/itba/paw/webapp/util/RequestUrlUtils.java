package ar.edu.itba.paw.webapp.util;

import javax.servlet.http.HttpServletRequest;

public final class RequestUrlUtils {

    private RequestUrlUtils() {
    }

    public static String buildBaseUrl(final HttpServletRequest request) {
        final String scheme = request.getScheme();
        final String serverName = request.getServerName();
        final int port = request.getServerPort();
        final String contextPath = request.getContextPath();
        final String portPart = (port == 80 || port == 443) ? "" : ":" + port;
        return scheme + "://" + serverName + portPart + contextPath;
    }
}