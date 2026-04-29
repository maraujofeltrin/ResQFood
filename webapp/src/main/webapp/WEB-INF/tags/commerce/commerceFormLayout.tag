<%@ tag body-content="scriptless" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="paw" uri="http://itba.edu.ar/paw/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ attribute name="pageTitleCode" required="true" type="java.lang.String" %>
<%@ attribute name="titleCode" required="true" type="java.lang.String" %>
<%@ attribute name="subtitleCode" required="true" type="java.lang.String" %>
<%@ attribute name="backLabelCode" required="false" type="java.lang.String" %>
<!DOCTYPE html>
<html class="light" lang="${pageContext.response.locale.language}">

<paw:head titleSuffixCode="${pageTitleCode}" />

<body class="bg-surface font-body text-on-surface antialiased flex flex-col min-h-screen">

    <paw:navbar />

    <main class="pack-detail-main">
        <div class="flex items-center gap-2 mb-8 text-secondary">
            <a href="${pageContext.request.contextPath}/commerce/products"
                class="group flex items-center font-bold">
                <span class="material-symbols-outlined text-xl mr-1">arrow_back</span>
                <span class="group-hover:underline">
                    <c:choose>
                        <c:when test="${not empty backLabelCode}"><spring:message code="${backLabelCode}"/></c:when>
                        <c:otherwise><spring:message code="commerce.createPack.backToDashboard"/></c:otherwise>
                    </c:choose>
                </span>
            </a>
        </div>

        <header class="mb-10 text-center md:text-left">
            <h1 class="pack-detail-title mb-3"><spring:message code="${titleCode}"/></h1>
            <p class="text-secondary text-lg"><spring:message code="${subtitleCode}"/></p>
        </header>

        <c:if test="${not empty errorMessage}">
            <div class="pack-feedback pack-feedback--error mb-8 flex items-center gap-2">
                <span class="material-symbols-outlined">error</span>
                <c:out value="${errorMessage}" />
            </div>
        </c:if>

        <jsp:doBody />
    </main>

    <paw:footer />

</body>

</html>
