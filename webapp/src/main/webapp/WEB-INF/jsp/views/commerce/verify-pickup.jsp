<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" uri="http://itba.edu.ar/paw/tags" %>
<!DOCTYPE html>
<html class="light" lang="${pageContext.response.locale.language}">

<paw:head titleSuffixCode="commerce.verifyPickup.pageTitle" />

<body class="bg-surface font-body text-on-surface antialiased flex flex-col min-h-screen">

<paw:navbar />

<main class="pt-24 px-6 md:px-12 pb-20 max-w-7xl mx-auto flex-grow w-full">
    <header class="flex flex-col md:flex-row md:items-end justify-between gap-6 mb-12">
        <div>
            <h1 class="text-4xl md:text-5xl font-headline font-extrabold text-primary tracking-tight mb-2">
                <spring:message code="commerce.verifyPickup.title" />
            </h1>
            <p class="text-secondary font-body">
                <spring:message code="commerce.verifyPickup.subtitle" />
            </p>
        </div>
    </header>

    <section class="max-w-2xl mx-auto">
        <div class="bg-surface-container-lowest rounded-2xl p-8 sm:p-10">
            <paw:pickupConfirm
                    formAction="${pageContext.request.contextPath}/commerce/verify-pickup"
                    reservation="${confirmedReservation}"
                    pickupError="${pickupError}"
                    pickupSuccess="${pickupSuccess}"
                    confirmedPack="${confirmedPack}"
                    confirmedClientName="${confirmedClientName}"
                    submittedCode="${submittedCode}"
                    showCodeInput="${!pickupSuccess}" />
        </div>
    </section>
</main>

<paw:footer />

</body>
</html>
