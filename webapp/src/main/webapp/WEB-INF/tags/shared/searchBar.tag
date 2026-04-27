<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="value" required="false" type="java.lang.String" %>
<%@ attribute name="placeholder" required="false" type="java.lang.String" %>
<%@ attribute name="classes" required="false" type="java.lang.String" %>

<c:set var="resolvedClasses" value="${classes != null ? classes : 'relative w-full max-w-md'}" />
<c:set var="resolvedPlaceholder" value="${placeholder != null ? placeholder : 'Search...'}" />

<div class="<c:out value='${resolvedClasses}'/>">
    <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-secondary pointer-events-none">search</span>
    <input 
        name="q" 
        value="<c:out value='${value}'/>"
        class="pl-10 pr-4 py-3 bg-surface-container-low text-on-surface placeholder:text-secondary rounded-xl border-none focus:ring-2 focus:ring-primary/20 w-full text-sm font-medium outline-none transition-all duration-300" 
        placeholder="<c:out value='${resolvedPlaceholder}'/>" 
        type="search"
    />
</div>
