<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="read" required="true" type="java.lang.Boolean" %>
<%@ attribute name="titleCode" required="true" type="java.lang.String" %>
<%@ attribute name="bodyCode" required="true" type="java.lang.String" %>
<%@ attribute name="timeCode" required="true" type="java.lang.String" %>
<%@ attribute name="demoId" required="false" type="java.lang.String" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<spring:message code="${titleCode}" var="notificationItemTitle"/>
<spring:message code="${bodyCode}" var="notificationItemBody"/>
<spring:message code="${timeCode}" var="notificationItemTime"/>
<spring:message code="notification.action.markRead" var="notificationMarkReadTitle"/>
<spring:message code="notification.action.markUnread" var="notificationMarkUnreadTitle"/>
<spring:message code="notification.action.delete" var="notificationDeleteTitle"/>

<c:set var="itemRead" value="${read ne null ? read : false}"/>

<article class="notification-item group/item p-4 flex gap-3 ${itemRead ? '' : 'notification-item--unread'}"
         <c:if test="${not empty demoId}">data-notification-id="${demoId}"</c:if>>
    <span class="notification-item__dot w-2 h-2 rounded-full mt-2 shrink-0 ${itemRead ? 'bg-transparent' : 'bg-primary'}"
          aria-hidden="true"></span>
    <div class="flex-1 min-w-0">
        <div class="flex justify-between items-start gap-2 mb-1">
            <p class="text-sm ${itemRead ? 'text-on-surface opacity-70' : 'font-bold text-on-surface'}">
                <c:out value="${notificationItemTitle}"/>
            </p>
            <span class="text-[10px] text-on-surface-variant shrink-0">
                <c:out value="${notificationItemTime}"/>
            </span>
        </div>
        <p class="text-xs text-on-surface-variant mb-2">
            <c:out value="${notificationItemBody}"/>
        </p>
        <div class="notification-item__actions flex gap-2 opacity-0 group-hover/item:opacity-100 transition-opacity">
            <c:choose>
                <c:when test="${itemRead}">
                    <button type="button"
                            class="notification-item__action material-symbols-outlined text-sm text-on-surface-variant hover:text-primary"
                            title="${notificationMarkUnreadTitle}"
                            data-action="mark-unread"
                            aria-label="${notificationMarkUnreadTitle}">
                        mark_as_unread
                    </button>
                </c:when>
                <c:otherwise>
                    <button type="button"
                            class="notification-item__action material-symbols-outlined text-sm text-primary"
                            title="${notificationMarkReadTitle}"
                            data-action="mark-read"
                            aria-label="${notificationMarkReadTitle}">
                        check_circle
                    </button>
                </c:otherwise>
            </c:choose>
            <button type="button"
                    class="notification-item__action material-symbols-outlined text-sm text-on-surface-variant hover:text-error"
                    title="${notificationDeleteTitle}"
                    data-action="delete"
                    aria-label="${notificationDeleteTitle}">
                delete
            </button>
        </div>
    </div>
</article>
