<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Panel de Comercio - Mis Packs</title>
</head>
<body>
    <h1>Dashboard del Comercio</h1>
    <a href="<c:url value='/commerce/create-pack' />">Crear Nuevo Pack</a>
    
    <h2>Listado de Packs (MVP)</h2>
    <table border="1">
        <thead>
            <tr>
                <th>ID</th>
                <th>Comercio (UserId)</th>
                <th>Título</th>
                <th>Descripción</th>
                <th>Precio Or.</th>
                <th>Precio F.</th>
                <th>Stock</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="pack" items="${packs}">
                <tr>
                    <td><c:out value="${pack.id}" /></td>
                    <td><c:out value="${pack.commerceId}" /></td>
                    <td><c:out value="${pack.title}" /></td>
                    <td><c:out value="${pack.description}" /></td>
                    <td>$<c:out value="${pack.originalPrice}" /></td>
                    <td>$<c:out value="${pack.finalPrice}" /></td>
                    <td><c:out value="${pack.stock}" /></td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</body>
</html>
