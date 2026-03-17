<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>

<html>
<body>
<paw:title text="${landingTitle}" />
<h2>Hello ${greeting}!</h2>
<paw:button text="Primary" type="primary" />
</body>
</html>