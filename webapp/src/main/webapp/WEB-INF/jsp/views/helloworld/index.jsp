<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
    <%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>

        <html>

        <head>
            <link rel="icon" href="${pageContext.request.contextPath}/images/favicon.ico" type="image/x-icon">
            <link rel="stylesheet"
                href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200&icon_names=star" />
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components.css">
        </head>

        <body>
            <paw:title text="${landingTitle}" />
            <h2>Hello ${message}!</h2>

            <h1>Create User</h1>
        </body>

        </html>