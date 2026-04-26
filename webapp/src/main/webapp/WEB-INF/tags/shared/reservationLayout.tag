<%@ tag body-content="scriptless" pageEncoding="UTF-8" %>
<%@ attribute name="title" required="true" %>
<%@ taglib prefix="paw" uri="http://itba.edu.ar/paw/tags" %>
<!DOCTYPE html>
<html class="light" lang="${pageContext.response.locale.language}">
<paw:head title="${title}" />
<body class="min-h-screen bg-appBg font-body text-appText antialiased">
    <div class="relative min-h-screen overflow-hidden">
        <jsp:doBody />
    </div>
</body>
</html>
