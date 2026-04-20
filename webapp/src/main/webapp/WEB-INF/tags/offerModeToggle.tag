<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<div class="offer-mode-toggle m-0 p-1 bg-surface-container flex shrink-0 rounded-full" style="height: 36px; min-width: auto;" id="offer-mode-toggle">
    <spring:message code="commerce.createOffer.toggle.pack" var="togglePackLabel"/>
    <spring:message code="commerce.createOffer.toggle.auction" var="toggleAuctionLabel"/>
    <button type="button" class="offer-mode-toggle__option offer-mode-toggle__option--pack offer-mode-toggle__option--active px-3 py-1 m-0 h-full flex items-center gap-1 font-semibold rounded-full"
            id="toggle-pack-btn" data-mode="pack" style="font-size: 13px;">
        <span class="material-symbols-outlined" style="font-size:16px;">inventory_2</span>
        ${togglePackLabel}
    </button>
    <button type="button" class="offer-mode-toggle__option offer-mode-toggle__option--auction px-3 py-1 m-0 h-full flex items-center gap-1 font-semibold text-secondary hover:text-auction rounded-full"
            id="toggle-auction-btn" data-mode="auction" style="font-size: 13px;">
        <span class="material-symbols-outlined" style="font-size:16px;">gavel</span>
        ${toggleAuctionLabel}
    </button>
</div>
