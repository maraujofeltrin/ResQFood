<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="paw" uri="http://itba.edu.ar/paw/tags" %>
<!DOCTYPE html>
<html class="light" lang="${pageContext.response.locale.language}">
<spring:message code="profile.changePassword.pageTitle" var="changePwdTitle"/>
<paw:head title="${changePwdTitle}"/>
<body class="bg-surface text-on-surface antialiased flex flex-col min-h-screen font-body">

<paw:navbar/>

<main class="pt-24 flex flex-col items-center justify-center flex-grow py-12 px-4">
    <div class="w-full max-w-md bg-surface-container-lowest rounded-2xl shadow-soft p-8">
        <h1 class="font-headline text-3xl font-bold text-center text-primary mb-2">
            <spring:message code="profile.changePassword.pageTitle"/>
        </h1>
        <p class="text-secondary text-sm text-center mb-6 font-body">
            <spring:message code="profile.changePassword.subtitle"/>
        </p>
        <form:form modelAttribute="profileChangePasswordForm" method="post"
                   action="${pageContext.request.contextPath}/profile/change-password"
                   cssClass="space-y-5">
            <div>
                <label for="currentPassword" class="block font-label text-sm font-medium text-secondary mb-1">
                    <spring:message code="profile.changePassword.label.current"/>
                </label>
                <spring:message code="profile.changePassword.placeholder.current" var="currentPwdPh"/>
                <form:password id="currentPassword" path="currentPassword" autocomplete="current-password"
                               cssClass="w-full px-4 py-3 rounded-xl border border-outline hover:border-primary focus:border-primary focus:ring-2 focus:ring-primary/20 transition-colors bg-surface-container-lowest text-on-surface outline-none"
                               placeholder="${currentPwdPh}"/>
                <form:errors path="currentPassword" cssClass="text-error text-sm mt-1 block"/>
            </div>
            <div>
                <label for="newPassword" class="block font-label text-sm font-medium text-secondary mb-1">
                    <spring:message code="profile.changePassword.label.new"/>
                </label>
                <spring:message code="passwordReset.change.placeholder.newPassword" var="newPwdPh"/>
                <form:password id="newPassword" path="newPassword" autocomplete="new-password"
                               cssClass="w-full px-4 py-3 rounded-xl border border-outline hover:border-primary focus:border-primary focus:ring-2 focus:ring-primary/20 transition-colors bg-surface-container-lowest text-on-surface outline-none"
                               placeholder="${newPwdPh}"/>
                <form:errors path="newPassword" cssClass="text-error text-sm mt-1 block"/>
            </div>
            <div>
                <label for="confirmPassword" class="block font-label text-sm font-medium text-secondary mb-1">
                    <spring:message code="profile.changePassword.label.confirm"/>
                </label>
                <spring:message code="passwordReset.change.placeholder.confirmPassword" var="confirmPwdPh"/>
                <form:password id="confirmPassword" path="confirmPassword" autocomplete="new-password"
                               cssClass="w-full px-4 py-3 rounded-xl border border-outline hover:border-primary focus:border-primary focus:ring-2 focus:ring-primary/20 transition-colors bg-surface-container-lowest text-on-surface outline-none"
                               placeholder="${confirmPwdPh}"/>
                <form:errors path="confirmPassword" cssClass="text-error text-sm mt-1 block"/>
            </div>
            <button type="submit"
                    class="w-full bg-primary text-on-primary font-semibold py-3 px-4 rounded-full transition-colors shadow-soft hover:brightness-110 mt-4 h-12 flex items-center justify-center border-0 cursor-pointer font-headline">
                <spring:message code="profile.changePassword.submit"/>
            </button>
        </form:form>
        <p class="mt-6 text-center">
            <a href="${pageContext.request.contextPath}/profile"
               class="text-primary font-semibold text-sm hover:underline">
                <spring:message code="profile.changePassword.backToProfile"/>
            </a>
        </p>
    </div>
</main>

<paw:footer/>
</body>
</html>
