<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="catalogUrl" required="true" %>
<%@ attribute name="backLabelCode" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<div class="flex items-center gap-2 mb-8 text-secondary">
    <a href="${catalogUrl}"
       class="group flex items-center font-bold">
        <span class="material-symbols-outlined text-xl mr-1">arrow_back</span>
        <span class="group-hover:underline"><spring:message code="${backLabelCode}"/></span>
    </a>
</div>

<script>
(function() {
    var currentPath = window.location.pathname;
    var referrer = document.referrer;
    var host = window.location.host;

    if (referrer && referrer.indexOf(host) !== -1) {
        try {
            var referrerPath = new URL(referrer).pathname;
            if (referrerPath === currentPath) {
                var count = parseInt(sessionStorage.getItem('selfRedirectCount') || '0', 10);
                sessionStorage.setItem('selfRedirectCount', (count + 1).toString());
            } else {
                sessionStorage.setItem('selfRedirectCount', '0');
            }
        } catch (e) {
            sessionStorage.setItem('selfRedirectCount', '0');
        }
    } else {
        sessionStorage.setItem('selfRedirectCount', '0');
    }

    var currentScript = document.currentScript;
    if (currentScript && currentScript.previousElementSibling) {
        var backLink = currentScript.previousElementSibling.querySelector('a');
        if (backLink) {
            backLink.addEventListener('click', function(e) {
                if (window.history.length > 1 && referrer && referrer.indexOf(host) !== -1) {
                    e.preventDefault();
                    var selfRedirectCount = parseInt(sessionStorage.getItem('selfRedirectCount') || '0', 10);
                    window.history.go(-1 - selfRedirectCount);
                }
            });
        }
    }
})();
</script>
