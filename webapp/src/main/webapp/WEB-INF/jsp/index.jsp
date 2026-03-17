<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>

<html>
    <head>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components.css">
    </head>
    <body>
        <paw:title text="${landingTitle}" />
            <h2>Hello ${greeting}!</h2>
        <paw:button text="Button" type="primary" />
        <paw:button text="Button" type="secondary" />
        <paw:button text="Button" type="success" />
        <paw:button text="Button" type="danger" />
    </body>
</html>