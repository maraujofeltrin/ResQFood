<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="value" required="false" type="java.lang.String" %>
<%@ attribute name="placeholder" required="false" type="java.lang.String" %>
<%@ attribute name="classes" required="false" type="java.lang.String" %>

<div class="${classes != null ? classes : 'relative w-full max-w-md'}">
    <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-secondary pointer-events-none">search</span>
    <input 
        name="q" 
        value="<c:out value="${value}"/>"
        class="pl-10 pr-4 py-3 bg-surface-container-low text-on-surface placeholder:text-secondary rounded-xl border-none focus:ring-2 focus:ring-primary/20 w-full text-sm font-medium outline-none transition-all duration-300" 
        placeholder="${placeholder != null ? placeholder : 'Search...'}" 
        type="search"
    />
</div>
