<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
    <%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>

        <html>

        <head>
            <link rel="stylesheet"
                href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200&icon_names=star" />
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components.css">
        </head>

        <body>
            <paw:title text="${landingTitle}" />
            <h2>Hello ${greeting}!</h2>
            <paw:input id="searchField" label="Buscar" placeholder="Escribi una categoria" />
            <paw:input id="emailField" label="Email" type="email" placeholder="nombre@ejemplo.com" />
            <paw:button text="Button" type="primary" />
            <paw:button text="Button" type="secondary" />
            <paw:button text="Button" type="success" />
            <paw:button text="Button" type="danger" />
            <paw:card category="${cardCategory}" heading="${cardHeading}" rating="${cardRating}"
                imageUrl="${cardImageUrl}" />
            <paw:card category="${cardCategory2}" heading="${cardHeading2}" rating="${cardRating2}" />

            <paw:button text="Ver oferta" cssClass="btn-primary"
                onclick="document.getElementById('modal1').style.display='block'" />

            <paw:modal id="modal1" title="${modalTitle}" content="${modalContent}" />
        </body>

        </html>