<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" uri="http://itba.edu.ar/paw/tags" %>
<!DOCTYPE html>
<html class="light" lang="${pageContext.response.locale.language}">
<paw:head titleSuffixCode="notification.pageTitle.suffix" />
<body class="bg-surface font-body text-on-surface antialiased flex flex-col min-h-screen">

<paw:navbar />

<c:url var="notificationsBackUrl" value="/"/>

<main class="notifications-page-main">
    <paw:backLink catalogUrl="${notificationsBackUrl}" backLabelCode="notification.page.back"/>

    <div class="max-w-2xl mx-auto w-full">
        <header class="notifications-page__header mb-8">
            <div class="flex items-center gap-3 mb-2">
                <span class="material-symbols-outlined text-primary text-3xl" data-icon="notifications">notifications</span>
                <h1 class="text-3xl md:text-4xl font-headline font-bold text-on-surface m-0">
                    <spring:message code="notification.page.title"/>
                </h1>
                <c:if test="${unreadCount > 0}">
                    <span class="notification-bell__badge notification-page__unread-badge" aria-hidden="true">${unreadCount}</span>
                </c:if>
            </div>
            <p class="text-secondary text-lg m-0">
                <spring:message code="notification.page.subtitle"/>
            </p>
            <c:if test="${not empty notificationRows}">
                <form action="${pageContext.request.contextPath}/notifications/read-all" method="post" class="mt-4 m-0">
                    <button type="submit"
                            class="text-sm text-primary font-semibold hover:underline bg-transparent border-0 cursor-pointer p-0">
                        <spring:message code="notification.action.markAllRead"/>
                    </button>
                </form>
            </c:if>
        </header>

        <spring:message code="notification.action.markRead" var="labelMarkRead"/>
        <spring:message code="notification.action.markUnread" var="labelMarkUnread"/>
        <spring:message code="notification.action.delete" var="labelDelete"/>

        <div class="notifications-page__list bg-surface-container-lowest rounded-2xl shadow-soft overflow-hidden border border-outline-variant/10">
            <c:choose>
                <c:when test="${not empty notificationRows}">
                    <c:forEach var="notif" items="${notificationRows}">
                        <article class="notification-item group/item p-4 flex gap-3${notif.read ? '' : ' notification-item--unread'}">
                            <span class="notification-item__dot w-2 h-2 rounded-full mt-2 shrink-0 ${notif.read ? 'bg-transparent' : 'bg-primary'}"
                                  aria-hidden="true"></span>
                            <div class="flex-1 min-w-0">
                                <div class="flex justify-between items-start gap-2 mb-1">
                                    <p class="text-sm m-0 ${notif.read ? 'text-on-surface opacity-70' : 'font-bold text-on-surface'}">
                                        <c:out value="${notif.title}"/>
                                    </p>
                                    <span class="text-[10px] text-on-surface-variant shrink-0">
                                        <c:out value="${notif.relativeTime}"/>
                                    </span>
                                </div>
                                <p class="text-xs text-on-surface-variant mb-2 m-0">
                                    <c:out value="${notif.body}"/>
                                </p>
                                <div class="notification-item__actions flex gap-2 opacity-0 group-hover/item:opacity-100 transition-opacity">
                                    <c:choose>
                                        <c:when test="${notif.read}">
                                            <form action="${pageContext.request.contextPath}/notifications/${notif.id}/unread" method="post" class="m-0">
                                                <button type="submit"
                                                        class="notification-item__action material-symbols-outlined text-sm text-on-surface-variant hover:text-primary"
                                                        title="${labelMarkUnread}" aria-label="${labelMarkUnread}">mark_as_unread</button>
                                            </form>
                                        </c:when>
                                        <c:otherwise>
                                            <form action="${pageContext.request.contextPath}/notifications/${notif.id}/read" method="post" class="m-0">
                                                <button type="submit"
                                                        class="notification-item__action material-symbols-outlined text-sm text-primary"
                                                        title="${labelMarkRead}" aria-label="${labelMarkRead}">check_circle</button>
                                            </form>
                                        </c:otherwise>
                                    </c:choose>
                                    <form action="${pageContext.request.contextPath}/notifications/${notif.id}/delete" method="post" class="m-0">
                                        <button type="submit"
                                                class="notification-item__action material-symbols-outlined text-sm text-on-surface-variant hover:text-error"
                                                title="${labelDelete}" aria-label="${labelDelete}">delete</button>
                                    </form>
                                </div>
                            </div>
                        </article>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <p class="p-8 text-center text-sm text-on-surface-variant m-0">
                        <spring:message code="notification.empty"/>
                    </p>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</main>

<paw:footer />
</body>
</html>
