<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<spring:message code="notification.sidebar.title" var="sidebarTitle"/>
<spring:message code="notification.sidebar.close" var="sidebarClose"/>
<spring:message code="notification.action.markAllRead" var="labelMarkAllRead"/>
<spring:message code="notification.action.markRead" var="labelMarkRead"/>
<spring:message code="notification.action.markUnread" var="labelMarkUnread"/>
<spring:message code="notification.action.delete" var="labelDelete"/>
<spring:message code="notification.empty" var="labelEmpty"/>
<spring:message code="notification.time.justNow" var="labelJustNow"/>

<spring:message code="notification.type.RESERVATION_REQUESTED_COMMERCE.title" var="titleReservationRequestedCommerce"/>
<spring:message code="notification.type.RESERVATION_CODE_CLIENT.title" var="titleReservationCodeClient"/>
<spring:message code="notification.type.AUCTION_WINNER_CLIENT.title" var="titleAuctionWinnerClient"/>
<spring:message code="notification.type.AUCTION_WINNER_COMMERCE.title" var="titleAuctionWinnerCommerce"/>
<spring:message code="notification.type.RESERVATION_REJECTED_CLIENT.title" var="titleReservationRejectedClient"/>
<spring:message code="notification.type.AUCTION_OUTBID_CLIENT.title" var="titleAuctionOutbidClient"/>

<div id="notification-backdrop"
     class="notifications-backdrop fixed inset-0 z-[55] bg-on-surface/40 backdrop-blur-sm hidden"
     aria-hidden="true"></div>

<aside id="notification-sidebar"
       class="notifications-sidebar fixed top-0 right-0 h-full w-80 max-w-[min(100vw,20rem)] z-[60] flex flex-col bg-surface-container-lowest shadow-lifted"
       role="dialog"
       aria-modal="true"
       aria-labelledby="notification-sidebar-title"
       aria-hidden="true"
       data-context-path="${pageContext.request.contextPath}"
       data-label-mark-read="${labelMarkRead}"
       data-label-mark-unread="${labelMarkUnread}"
       data-label-delete="${labelDelete}"
       data-label-just-now="${labelJustNow}"
       data-title-RESERVATION_REQUESTED_COMMERCE="${titleReservationRequestedCommerce}"
       data-title-RESERVATION_CODE_CLIENT="${titleReservationCodeClient}"
       data-title-AUCTION_WINNER_CLIENT="${titleAuctionWinnerClient}"
       data-title-AUCTION_WINNER_COMMERCE="${titleAuctionWinnerCommerce}"
       data-title-RESERVATION_REJECTED_CLIENT="${titleReservationRejectedClient}"
       data-title-AUCTION_OUTBID_CLIENT="${titleAuctionOutbidClient}">
    <header class="notifications-sidebar__header shrink-0 p-4 bg-surface-container-low flex justify-between items-center gap-3">
        <h2 id="notification-sidebar-title" class="font-bold text-on-surface text-base m-0">
            <c:out value="${sidebarTitle}"/>
        </h2>
        <div class="flex items-center gap-2 shrink-0">
            <button type="button"
                    class="text-xs text-primary font-semibold hover:underline whitespace-nowrap"
                    data-action="mark-all-read">
                <c:out value="${labelMarkAllRead}"/>
            </button>
            <button type="button"
                    id="notification-sidebar-close"
                    class="notification-sidebar__close p-1 rounded-full text-on-surface-variant hover:bg-surface-container-high hover:text-on-surface transition-colors"
                    aria-label="${sidebarClose}">
                <span class="material-symbols-outlined text-xl" data-icon="close">close</span>
            </button>
        </div>
    </header>

    <div class="notifications-sidebar__body flex-1 overflow-y-auto min-h-0" id="notification-list">
        <p class="p-6 text-center text-sm text-on-surface-variant hidden" id="notification-empty">
            <c:out value="${labelEmpty}"/>
        </p>
    </div>
</aside>
