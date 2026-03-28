<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Crear Pack Sorpresa</title>
</head>
<body>
    <h1>Crear Nuevo Pack</h1>
    <a href="<c:url value='/commerce' />">Volver al Dashboard</a>

    <c:if test="${not empty errorMessage}">
        <div style="color: red;">
            <strong>Error:</strong> <c:out value="${errorMessage}" />
        </div>
    </c:if>

    <form action="<c:url value='/commerce/create-pack' />" method="post">
        
        <h2>1. Identidad del Usuario</h2>
        <p><i>(Si ya existís en el sistema, tus datos de Comercio definidos previamente se mantendrán)</i></p>
        <label>Email de Acceso:</label>
        <input type="email" name="email" required /><br/>
        <label>Contraseña:</label>
        <input type="password" name="password" required /><br/>
        <label>Nombre del Titular:</label>
        <input type="text" name="name" required /><br/>

        <hr/>
        
        <h2>2. Datos del Local (Solo si es tu primera vez)</h2>
        <label>Nombre Comercial:</label>
        <input type="text" name="commercialName" /><br/>
        <label>Categoría:</label>
        <select name="category">
            <option value="PANADERIA">Panadería</option>
            <option value="RESTAURANTE">Restaurante</option>
            <option value="ETC">Otros</option>
        </select><br/>
        <label>Calle:</label>
        <input type="text" name="street" /><br/>
        <label>Número:</label>
        <input type="number" name="streetNumber" /><br/>
        <label>Ciudad:</label>
        <input type="text" name="city" /><br/>
        <label>Provincia:</label>
        <input type="text" name="province" /><br/>
        <label>Cod. Postal:</label>
        <input type="text" name="postalCode" /><br/>
        <label>Horario Apertura (ej. 08:00):</label>
        <input type="time" name="openingTime" /><br/>
        <label>Horario Cierre (ej. 20:00):</label>
        <input type="time" name="closingTime" /><br/>

        <hr/>
        
        <h2>3. Datos del Pack a publicar</h2>
        <label>Título del Pack:</label>
        <input type="text" name="title" required /><br/>
        <label>Descripción:</label>
        <textarea name="description" required></textarea><br/>
        <label>Precio Original ($):</label>
        <input type="number" step="0.01" name="originalPrice" required /><br/>
        <label>Precio con Descuento ($):</label>
        <input type="number" step="0.01" name="finalPrice" required /><br/>
        <label>Stock Disponible:</label>
        <input type="number" name="stock" required /><br/>
        
        <br/>
        <button type="submit">Crear Pack y Publicar</button>

    </form>
</body>
</html>
