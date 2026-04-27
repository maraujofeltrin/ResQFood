<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="paw" uri="http://itba.edu.ar/paw/tags" %>
<!DOCTYPE html>
<html class="light" lang="${pageContext.response.locale.language}">
<paw:head titleSuffixCode="profile.pageTitle"/>
<body class="bg-surface font-body text-on-surface antialiased flex flex-col min-h-screen">

<paw:navbar/>

<main class="pt-24 px-6 md:px-10 pb-20 flex-grow w-full">
    <div class="max-w-5xl mx-auto w-full">
        <header class="mb-10 text-center lg:text-left">
            <h1 class="text-4xl md:text-5xl font-headline font-extrabold text-primary tracking-tight mb-2">
                <spring:message code="profile.pageTitle"/>
            </h1>
            <p class="text-secondary font-body text-base max-w-xl mx-auto lg:mx-0">
                <spring:message code="profile.pageSubtitle"/>
            </p>
        </header>

        <c:if test="${not empty profilePasswordChangeSuccess}">
            <div class="mb-8 max-w-2xl mx-auto lg:mx-0 rounded-2xl bg-primary-container/50 px-4 py-3 text-on-primary-container font-body text-sm font-medium text-center lg:text-left"
                 role="status">
                <spring:message code="profile.passwordChange.flashSuccess"/>
            </div>
        </c:if>
        <c:if test="${not empty profilePhotoUpdateSuccess}">
            <div class="mb-8 max-w-2xl mx-auto lg:mx-0 rounded-2xl bg-primary-container/50 px-4 py-3 text-on-primary-container font-body text-sm font-medium text-center lg:text-left"
                 role="status">
                <spring:message code="profile.photo.flashSuccess"/>
            </div>
        </c:if>
        <c:if test="${not empty profileLocaleUpdateSuccess}">
            <div class="mb-8 max-w-2xl mx-auto lg:mx-0 rounded-2xl bg-primary-container/50 px-4 py-3 text-on-primary-container font-body text-sm font-medium text-center lg:text-left"
                 role="status">
                <spring:message code="profile.locale.flashSuccess"/>
            </div>
        </c:if>
        <c:if test="${not empty profileLocaleUpdateError}">
            <div class="mb-8 max-w-2xl mx-auto lg:mx-0 rounded-2xl bg-error-container/40 px-4 py-3 text-on-error-container font-body text-sm font-medium text-center lg:text-left"
                 role="alert">
                <spring:message code="profile.locale.flashError"/>
            </div>
        </c:if>

        <div class="grid grid-cols-1 lg:grid-cols-12 gap-8 lg:gap-10 items-start">
            <!-- Sidebar -->
            <aside class="lg:col-span-3 w-full max-w-md mx-auto lg:max-w-none lg:mx-0">
                <spring:message code="profile.logout.confirm.title" var="profileLogoutConfirmTitle"/>
                <spring:message code="profile.sidebar.navLabel" var="profileSidebarNavLabel"/>
                <nav class="flex flex-col gap-2" aria-label="${profileSidebarNavLabel}">
                    <spring:message code="profile.nav.profile" var="navProfileLabel"/>
                    <a class="flex items-center gap-3 px-4 py-3 rounded-2xl transition-all group no-underline font-headline text-sm md:text-base ${profileNavSection == 'profile' ? 'bg-surface-container-highest text-primary font-bold shadow-soft' : 'text-on-surface-variant hover:bg-surface-container-low font-medium'}"
                       href="${pageContext.request.contextPath}/profile">
                        <span class="material-symbols-outlined text-xl shrink-0" data-icon="person">person</span>
                        <span>${navProfileLabel}</span>
                    </a>
                    <spring:message code="profile.nav.settings" var="navSettingsLabel"/>
                    <a class="flex items-center gap-3 px-4 py-3 rounded-2xl transition-all group no-underline font-headline text-sm md:text-base ${profileNavSection == 'settings' ? 'bg-surface-container-highest text-primary font-bold shadow-soft' : 'text-on-surface-variant hover:bg-surface-container-low font-medium'}"
                       href="${pageContext.request.contextPath}/profile/settings">
                        <span class="material-symbols-outlined text-xl shrink-0" data-icon="settings">settings</span>
                        <span>${navSettingsLabel}</span>
                    </a>
                </nav>
                <div class="mt-4 pt-4 border-t border-outline-variant/15">
                    <button type="button"
                            class="flex items-center gap-3 px-4 py-3 rounded-2xl transition-all group no-underline font-headline text-sm md:text-base w-full text-left border-0 cursor-pointer bg-transparent text-on-surface-variant hover:bg-surface-container-low font-medium"
                            onclick="document.getElementById('profileLogoutModal').style.display='flex'"
                            aria-haspopup="dialog"
                            aria-controls="profileLogoutModal">
                        <span class="material-symbols-outlined text-xl shrink-0" data-icon="logout">logout</span>
                        <spring:message code="layout.nav.logout"/>
                    </button>
                </div>
                <paw:modal id="profileLogoutModal" title="${profileLogoutConfirmTitle}">
                    <p class="font-body text-on-surface"><spring:message code="profile.logout.confirm.message"/></p>
                    <form action="${pageContext.request.contextPath}/logout" method="post" class="m-0">
                        <div class="flex justify-center gap-3 mt-4 flex-wrap">
                            <button type="button"
                                    class="btn btn-cancel"
                                    onclick="document.getElementById('profileLogoutModal').style.display='none'">
                                <spring:message code="profile.logout.confirm.cancel"/>
                            </button>
                            <button type="submit" class="btn btn-danger">
                                <spring:message code="profile.logout.confirm.confirm"/>
                            </button>
                        </div>
                    </form>
                </paw:modal>
            </aside>

            <!-- Content -->
            <div class="lg:col-span-9 w-full max-w-3xl mx-auto lg:max-w-none">
                <c:choose>
                    <c:when test="${profileNavSection == 'settings'}">
                        <section class="bg-surface-container-lowest p-8 md:p-10 rounded-2xl shadow-soft">
                            <h2 class="text-2xl md:text-3xl font-headline font-bold tracking-tight text-on-surface mb-2 text-center sm:text-left">
                                <spring:message code="profile.section.settings"/>
                            </h2>
                            <p class="text-secondary text-sm font-body mb-8 max-w-xl mx-auto sm:mx-0 text-center sm:text-left">
                                <spring:message code="profile.section.settingsHint"/>
                            </p>
                            <h3 class="text-lg font-headline font-bold text-on-surface mb-4 max-w-xl mx-auto sm:mx-0">
                                <spring:message code="profile.section.language"/>
                            </h3>
                            <form action="${pageContext.request.contextPath}/profile/settings/locale" method="post"
                                  class="space-y-4 max-w-xl mx-auto m-0">
                                <div>
                                    <label class="text-xs font-bold uppercase tracking-wider text-on-surface-variant ml-1 font-label block mb-2"
                                           for="profile-language">
                                        <spring:message code="profile.label.preferredLanguage"/>
                                    </label>
                                    <div class="relative">
                                        <select id="profile-language" name="lang"
                                                class="w-full appearance-none bg-none bg-surface-container px-4 py-4 pr-12 rounded-2xl border-none focus:ring-2 focus:ring-primary text-on-surface font-bold outline-none cursor-pointer [background-image:none]">
                                            <c:forEach var="lang" items="${profile.languageCodes}">
                                                <c:set var="langMsgCode" value="profile.lang.${lang}"/>
                                                <option value="${lang}" ${lang == profile.selectedLanguageCode ? 'selected' : ''}>
                                                    <spring:message code="${langMsgCode}"/>
                                                </option>
                                            </c:forEach>
                                        </select>
                                        <div class="pointer-events-none absolute right-4 top-1/2 -translate-y-1/2 text-on-surface-variant">
                                            <span class="material-symbols-outlined" data-icon="expand_more">expand_more</span>
                                        </div>
                                    </div>
                                </div>
                                <p class="text-xs text-on-surface-variant leading-relaxed font-body">
                                    <spring:message code="profile.language.help"/>
                                </p>
                                <button type="submit"
                                        class="inline-flex items-center justify-center gap-2 px-6 py-3 rounded-full bg-primary text-on-primary font-semibold shadow-soft hover:brightness-110 transition-colors border-0 cursor-pointer font-headline">
                                    <spring:message code="profile.settings.saveLanguage"/>
                                </button>
                            </form>
                        </section>
                    </c:when>
                    <c:otherwise>
                        <section class="bg-surface-container-lowest p-8 md:p-10 rounded-2xl shadow-soft">
                            <h2 class="text-2xl md:text-3xl font-headline font-bold tracking-tight text-on-surface mb-8 text-center sm:text-left">
                                <spring:message code="profile.section.accountDetails"/>
                            </h2>

                            <form:form modelAttribute="profilePhotoForm"
                                       cssClass="flex flex-col sm:flex-row gap-8 md:gap-10 items-center sm:items-start max-w-2xl mx-auto sm:mx-0 w-full m-0"
                                       action="${pageContext.request.contextPath}/profile/photo"
                                       method="post"
                                       enctype="multipart/form-data">
                                <div class="shrink-0 w-full sm:w-auto flex flex-col items-center sm:items-start">
                                    <p class="text-xs font-bold uppercase tracking-wider text-on-surface-variant font-label mb-3 text-center sm:text-left w-full">
                                        <spring:message code="profile.label.profilePhoto"/>
                                    </p>
                                    <spring:message code="profile.avatar.alt" var="profileAvatarAlt"/>
                                    <div class="w-36 h-36 md:w-40 md:h-40 rounded-2xl overflow-hidden shadow-soft bg-surface-container">
                                        <c:choose>
                                            <c:when test="${not empty profile.profileImageId}">
                                                <img src="${pageContext.request.contextPath}/images/${profile.profileImageId}"
                                                     alt="${profileAvatarAlt}"
                                                     class="w-full h-full object-cover pointer-events-none select-none"
                                                     width="160"
                                                     height="160"
                                                     loading="lazy"
                                                     draggable="false"/>
                                            </c:when>
                                            <c:otherwise>
                                                <img src="${pageContext.request.contextPath}/images/${profile.profileImageFileName}"
                                                     alt="${profileAvatarAlt}"
                                                     class="w-full h-full object-cover pointer-events-none select-none"
                                                     width="160"
                                                     height="160"
                                                     loading="lazy"
                                                     draggable="false"/>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <c:if test="${not empty profilePhotoUpdateError}">
                                        <p class="mt-3 text-xs text-error font-body max-w-[14rem] text-center sm:text-left leading-relaxed" role="alert">
                                            <spring:message code="profile.photo.updateFailed"/>
                                        </p>
                                    </c:if>
                                    <spring:hasBindErrors name="profilePhotoForm">
                                        <form:errors path="photo" cssClass="mt-3 text-xs text-error font-body max-w-[14rem] text-center sm:text-left block" element="p"/>
                                    </spring:hasBindErrors>
                                    <label class="text-xs font-bold uppercase tracking-wider text-on-surface-variant font-label sr-only"
                                           for="profile-photo-input">
                                        <spring:message code="profile.label.profilePhoto"/>
                                    </label>
                                    <input id="profile-photo-input" type="file" name="photo"
                                           accept="image/jpeg,image/png,image/webp,image/gif"
                                           class="mt-4 block w-full max-w-[14rem] text-xs text-on-surface file:mr-3 file:py-2 file:px-4 file:rounded-full file:border-0 file:bg-primary file:text-on-primary file:font-semibold file:cursor-pointer cursor-pointer"/>
                                    <p class="mt-3 text-xs text-on-surface-variant font-body max-w-[14rem] text-center sm:text-left leading-relaxed">
                                        <spring:message code="profile.avatar.viewOnlyNote"/>
                                    </p>
                                </div>

                                <div class="space-y-6 flex-1 w-full min-w-0">
                                    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                                        <div class="space-y-1.5">
                                            <label class="text-xs font-bold uppercase tracking-wider text-on-surface-variant ml-1 font-label"
                                                   for="profile-full-name">
                                                <spring:message code="profile.label.fullName"/>
                                            </label>
                                            <input id="profile-full-name"
                                                   class="w-full bg-surface-container-low px-4 py-3 rounded-xl border-none text-on-surface font-medium outline-none cursor-not-allowed opacity-90"
                                                   type="text" value="<c:out value='${profile.fullName}'/>" readonly tabindex="-1"/>
                                        </div>
                                        <div class="space-y-1.5">
                                            <label class="text-xs font-bold uppercase tracking-wider text-on-surface-variant ml-1 font-label"
                                                   for="profile-phone">
                                                <spring:message code="profile.label.phone"/>
                                            </label>
                                            <input id="profile-phone"
                                                   class="w-full bg-surface-container-low px-4 py-3 rounded-xl border-none text-on-surface font-medium outline-none cursor-not-allowed opacity-90"
                                                   type="tel" value="<c:out value='${profile.phone}'/>" readonly tabindex="-1"/>
                                        </div>
                                    </div>
                                    <div class="space-y-1.5">
                                        <label class="text-xs font-bold uppercase tracking-wider text-on-surface-variant ml-1 font-label"
                                               for="profile-email">
                                            <spring:message code="profile.label.email"/>
                                        </label>
                                        <input id="profile-email"
                                               class="w-full bg-surface-container-low px-4 py-3 rounded-xl border-none text-on-surface font-medium outline-none cursor-not-allowed opacity-90"
                                               type="email" value="<c:out value='${profile.email}'/>" readonly tabindex="-1"/>
                                    </div>
                                    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pt-4">
                                        <a class="text-primary font-bold text-sm flex items-center gap-1 hover:gap-2 transition-all no-underline order-2 sm:order-1 justify-center sm:justify-start"
                                           href="${pageContext.request.contextPath}/profile/change-password">
                                            <spring:message code="profile.action.changePassword"/>
                                            <span class="material-symbols-outlined text-sm" data-icon="arrow_forward">arrow_forward</span>
                                        </a>
                                        <button class="order-1 sm:order-2 inline-flex items-center justify-center bg-primary text-on-primary px-8 py-3 rounded-full font-semibold shadow-soft hover:brightness-110 active:scale-[0.98] transition-all border-0 cursor-pointer font-headline w-full sm:w-auto"
                                                type="submit">
                                            <spring:message code="profile.action.saveChanges"/>
                                        </button>
                                    </div>
                                </div>
                            </form:form>
                        </section>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</main>

</body>
</html>
