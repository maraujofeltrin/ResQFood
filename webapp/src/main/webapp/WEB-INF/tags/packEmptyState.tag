<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="icon"        required="true" %>
<%@ attribute name="title"       required="true" %>
<%@ attribute name="description" required="true" %>

<div class="text-center py-16 bg-surface-container-low rounded-3xl border border-dashed border-outline-variant">
    <span class="material-symbols-outlined text-5xl mb-4 text-outline"
          style="font-variation-settings: 'wght' 200;">${icon}</span>
    <h3 class="text-2xl font-headline font-bold text-on-surface">${title}</h3>
    <p class="text-secondary mt-2 text-lg">${description}</p>
</div>
