<%@ tag body-content="scriptless" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>

<%@ attribute name="pageTitle" required="true" type="java.lang.String" %>
<%@ attribute name="blurColorTopLeft" required="false" type="java.lang.String" %>
<%@ attribute name="blurColorBottomRight" required="false" type="java.lang.String" %>
<%@ attribute name="badgeText" required="true" type="java.lang.String" %>
<%@ attribute name="badgeVariant" required="false" type="java.lang.String"
        description="primary (default), success, error" %>

<c:if test="${empty blurColorTopLeft}">
    <c:set var="blurColorTopLeft" value="bg-primary-container/30" />
</c:if>
<c:if test="${empty blurColorBottomRight}">
    <c:set var="blurColorBottomRight" value="bg-primary/15" />
</c:if>
<c:if test="${empty badgeVariant}">
    <c:set var="badgeVariant" value="primary" />
</c:if>

<c:choose>
    <c:when test="${badgeVariant == 'success'}">
        <c:set var="badgeClass" value="border-primary/25 bg-primary-container/50 text-primary" />
    </c:when>
    <c:when test="${badgeVariant == 'error'}">
        <c:set var="badgeClass" value="border-error/25 bg-error-container/50 text-error" />
    </c:when>
    <c:otherwise>
        <c:set var="badgeClass" value="border-primary/20 bg-primary-container/30 text-primary" />
    </c:otherwise>
</c:choose>

<paw:reservationLayout title="${pageTitle}">
    <div class="pointer-events-none absolute -top-28 -left-20 h-80 w-80 rounded-full ${blurColorTopLeft} blur-3xl"></div>
    <div class="pointer-events-none absolute -bottom-24 -right-16 h-72 w-72 rounded-full ${blurColorBottomRight} blur-3xl"></div>

    <main class="relative mx-auto flex min-h-screen w-full max-w-4xl items-center px-5 py-10 sm:px-8">
        <section class="w-full rounded-2xl bg-surface-container-lowest/90 p-7 shadow-soft backdrop-blur sm:p-10">
            <div class="mb-6 inline-flex items-center gap-2 rounded-full border ${badgeClass} px-3 py-1 text-xs font-semibold uppercase tracking-[0.16em]">
                <c:out value="${badgeText}" />
            </div>

            <jsp:doBody />
        </section>
    </main>
</paw:reservationLayout>
