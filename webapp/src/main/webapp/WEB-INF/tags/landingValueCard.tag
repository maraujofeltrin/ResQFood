<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="description" required="true" %>
<%@ attribute name="icon" required="true" %>

<div class="p-8 rounded-3xl bg-surface-container border-none flex flex-col items-center text-center">
    <div class="w-16 h-16 rounded-2xl bg-white flex items-center justify-center mb-6 shadow-sm">
        <span class="material-symbols-outlined text-primary text-3xl" data-icon="${icon}">${icon}</span>
    </div>
    <h3 class="text-xl font-bold text-primary mb-3">${title}</h3>
    <p class="text-secondary">${description}</p>
</div>
