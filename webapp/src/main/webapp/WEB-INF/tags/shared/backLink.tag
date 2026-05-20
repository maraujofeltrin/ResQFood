<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="catalogUrl" required="true" %>
<%@ attribute name="backLabelCode" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<div class="flex items-center gap-2 mb-8 text-secondary">
    <a href="${catalogUrl}"
       onclick="if (window.history.length > 1 && document.referrer && document.referrer.indexOf(window.location.host) !== -1 && document.referrer !== window.location.href) { window.history.back(); return false; }"
       class="group flex items-center font-bold">
        <span class="material-symbols-outlined text-xl mr-1">arrow_back</span>
        <span class="group-hover:underline"><spring:message code="${backLabelCode}"/></span>
    </a>
</div>
