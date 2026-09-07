<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="currentPage" required="true" type="java.lang.Integer" %>
<%@ attribute name="totalPages" required="true" type="java.lang.Integer" %>
<%@ attribute name="baseUrl" required="false" %>
<%@ attribute name="formId" required="false" type="java.lang.String" %>
<%@ attribute name="pageParam" required="false" type="java.lang.String" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="paramName" value="${empty pageParam ? 'page' : pageParam}" />
<c:set var="useForm" value="${not empty formId}" />
<c:if test="${not useForm}">
    <c:set var="pageSep" value="${fn:contains(baseUrl, '?') ? '&' : '?'}" />
</c:if>

<c:if test="${totalPages > 1}">
    <div class="flex items-center justify-center mt-20 gap-2">
        <%-- Previous page --%>
        <c:if test="${currentPage > 1}">
            <c:choose>
                <c:when test="${useForm}">
                    <button type="submit" name="${paramName}" value="${currentPage - 1}" form="${formId}"
                            class="w-10 h-10 flex items-center justify-center rounded-lg hover:bg-surface-container-high text-on-surface-variant transition-colors cursor-pointer bg-transparent border-none">
                        <span class="material-symbols-outlined">chevron_left</span>
                    </button>
                </c:when>
                <c:otherwise>
                    <a href="<c:out value='${baseUrl}${pageSep}${paramName}=${currentPage - 1}'/>" class="w-10 h-10 flex items-center justify-center rounded-lg hover:bg-surface-container-high text-on-surface-variant transition-colors">
                        <span class="material-symbols-outlined">chevron_left</span>
                    </a>
                </c:otherwise>
            </c:choose>
        </c:if>

        <%-- Page numbers --%>
        <c:forEach var="i" begin="1" end="${totalPages}">
            <c:choose>
                <c:when test="${i == currentPage}">
                    <span class="w-10 h-10 flex items-center justify-center rounded-lg bg-primary text-on-primary font-bold"><c:out value="${i}" /></span>
                </c:when>
                <c:otherwise>
                    <c:choose>
                        <c:when test="${useForm}">
                            <button type="submit" name="${paramName}" value="${i}" form="${formId}"
                                    class="w-10 h-10 flex items-center justify-center rounded-lg hover:bg-surface-container-high text-on-surface-variant transition-colors cursor-pointer bg-transparent border-none"><c:out value="${i}" /></button>
                        </c:when>
                        <c:otherwise>
                            <a href="<c:out value='${baseUrl}${pageSep}${paramName}=${i}'/>" class="w-10 h-10 flex items-center justify-center rounded-lg hover:bg-surface-container-high text-on-surface-variant transition-colors"><c:out value="${i}" /></a>
                        </c:otherwise>
                    </c:choose>
                </c:otherwise>
            </c:choose>
        </c:forEach>

        <%-- Next page --%>
        <c:if test="${currentPage < totalPages}">
            <c:choose>
                <c:when test="${useForm}">
                    <button type="submit" name="${paramName}" value="${currentPage + 1}" form="${formId}"
                            class="w-10 h-10 flex items-center justify-center rounded-lg hover:bg-surface-container-high text-on-surface-variant transition-colors cursor-pointer bg-transparent border-none">
                        <span class="material-symbols-outlined">chevron_right</span>
                    </button>
                </c:when>
                <c:otherwise>
                    <a href="<c:out value='${baseUrl}${pageSep}${paramName}=${currentPage + 1}'/>" class="w-10 h-10 flex items-center justify-center rounded-lg hover:bg-surface-container-high text-on-surface-variant transition-colors">
                        <span class="material-symbols-outlined">chevron_right</span>
                    </a>
                </c:otherwise>
            </c:choose>
        </c:if>
    </div>
</c:if>
