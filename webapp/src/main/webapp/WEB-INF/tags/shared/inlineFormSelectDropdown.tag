<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ attribute name="selectName" required="true" type="java.lang.String" %>
<%@ attribute name="options" required="true" type="java.lang.Object" %>
<%@ attribute name="selectedValue" required="false" type="java.lang.String" %>
<%@ attribute name="optionMessageCodePrefix" required="true" type="java.lang.String" %>
<%@ attribute name="emptyOptionMessageCode" required="false" type="java.lang.String" %>
<%@ attribute name="icon" required="false" type="java.lang.String" %>
<%@ attribute name="classes" required="false" type="java.lang.String" %>

<c:set var="resolvedIcon" value="${empty icon ? 'swap_vert' : icon}" />
<c:set var="outerClasses" value="${classes != null ? classes : 'relative inline-flex items-center max-w-full'}" />

<div class="<c:out value='${outerClasses}'/>">
    <div class="inline-flex items-center rounded-full py-1.5 px-3 text-sm font-semibold transition-colors duration-200 bg-surface-container-low text-on-surface hover:bg-surface-container-high relative w-full overflow-hidden shrink-0 lg:shrink w-full">
        <span class="material-symbols-outlined text-base mr-1 pointer-events-none shrink-0"><c:out value="${resolvedIcon}"/></span>
        <select name="<c:out value='${selectName}'/>" onchange="this.form.submit()"
                class="appearance-none bg-transparent outline-none cursor-pointer text-sm font-semibold text-on-surface w-full pr-8 focus:outline-none focus:ring-0 truncate"
                style="outline: none !important; box-shadow: none !important; border: none !important; text-overflow: ellipsis;">
            <c:if test="${not empty emptyOptionMessageCode}">
                <option value=""><spring:message code="${emptyOptionMessageCode}" /></option>
            </c:if>
            <c:forEach var="opt" items="${options}">
                <option value="<c:out value='${opt.name()}'/>" ${selectedValue == opt.name() ? 'selected' : ''}>
                    <spring:message code="${optionMessageCodePrefix}${opt.name()}" />
                </option>
            </c:forEach>
        </select>
    </div>
</div>
