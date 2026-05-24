<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="currentPage" required="true" type="java.lang.Integer" %>
<%@ attribute name="totalPages" required="true" type="java.lang.Integer" %>
<%@ attribute name="baseUrl" required="true" %>
<%@ attribute name="pageParam" required="false" type="java.lang.String" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="pageSep" value="${fn:contains(baseUrl, '?') ? '&' : '?'}" />
<c:set var="paramName" value="${empty pageParam ? 'page' : pageParam}" />

<c:if test="${totalPages > 1}">
    <div class="flex items-center justify-center mt-20 gap-2">
        <c:if test="${currentPage > 1}">
            <a href="<c:out value='${baseUrl}${pageSep}${paramName}=${currentPage - 1}'/>" class="w-10 h-10 flex items-center justify-center rounded-lg hover:bg-surface-container-high text-on-surface-variant transition-colors">
                <span class="material-symbols-outlined">chevron_left</span>
            </a>
        </c:if>

        <c:forEach var="i" begin="1" end="${totalPages}">
            <c:choose>
                <c:when test="${i == currentPage}">
                    <span class="w-10 h-10 flex items-center justify-center rounded-lg bg-primary text-on-primary font-bold"><c:out value="${i}" /></span>
                </c:when>
                <c:otherwise>
                    <a href="<c:out value='${baseUrl}${pageSep}${paramName}=${i}'/>" class="w-10 h-10 flex items-center justify-center rounded-lg hover:bg-surface-container-high text-on-surface-variant transition-colors"><c:out value="${i}" /></a>
                </c:otherwise>
            </c:choose>
        </c:forEach>

        <c:if test="${currentPage < totalPages}">
            <a href="<c:out value='${baseUrl}${pageSep}${paramName}=${currentPage + 1}'/>" class="w-10 h-10 flex items-center justify-center rounded-lg hover:bg-surface-container-high text-on-surface-variant transition-colors">
                <span class="material-symbols-outlined">chevron_right</span>
            </a>
        </c:if>
    </div>
</c:if>
