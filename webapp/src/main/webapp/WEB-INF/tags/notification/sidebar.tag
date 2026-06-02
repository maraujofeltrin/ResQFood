<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" uri="http://itba.edu.ar/paw/tags" %>

<spring:message code="notification.sidebar.title" var="notificationSidebarTitle"/>
<spring:message code="notification.sidebar.close" var="notificationSidebarClose"/>
<spring:message code="notification.action.markAllRead" var="notificationMarkAllRead"/>

<div id="notification-backdrop"
     class="notifications-backdrop fixed inset-0 z-[55] bg-on-surface/40 backdrop-blur-sm hidden"
     aria-hidden="true"></div>

<aside id="notification-sidebar"
       class="notifications-sidebar fixed top-0 right-0 h-full w-80 max-w-[min(100vw,20rem)] z-[60] flex flex-col bg-surface-container-lowest shadow-lifted"
       role="dialog"
       aria-modal="true"
       aria-labelledby="notification-sidebar-title"
       aria-hidden="true">
    <header class="notifications-sidebar__header shrink-0 p-4 bg-surface-container-low flex justify-between items-center gap-3">
        <h2 id="notification-sidebar-title" class="font-bold text-on-surface text-base m-0">
            <c:out value="${notificationSidebarTitle}"/>
        </h2>
        <div class="flex items-center gap-2 shrink-0">
            <button type="button"
                    class="text-xs text-primary font-semibold hover:underline whitespace-nowrap"
                    data-action="mark-all-read">
                <c:out value="${notificationMarkAllRead}"/>
            </button>
            <button type="button"
                    id="notification-sidebar-close"
                    class="notification-sidebar__close p-1 rounded-full text-on-surface-variant hover:bg-surface-container-high hover:text-on-surface transition-colors"
                    aria-label="${notificationSidebarClose}">
                <span class="material-symbols-outlined text-xl" data-icon="close">close</span>
            </button>
        </div>
    </header>

    <div class="notifications-sidebar__body flex-1 overflow-y-auto min-h-0">
        <paw:notificationItem read="${false}"
                              titleCode="notification.demo.outbid.title"
                              bodyCode="notification.demo.outbid.body"
                              timeCode="notification.demo.outbid.time"
                              demoId="demo-1"/>
        <paw:notificationItem read="${false}"
                              titleCode="notification.demo.code.title"
                              bodyCode="notification.demo.code.body"
                              timeCode="notification.demo.code.time"
                              demoId="demo-2"/>
        <paw:notificationItem read="${true}"
                              titleCode="notification.demo.rejected.title"
                              bodyCode="notification.demo.rejected.body"
                              timeCode="notification.demo.rejected.time"
                              demoId="demo-3"/>
    </div>
</aside>
