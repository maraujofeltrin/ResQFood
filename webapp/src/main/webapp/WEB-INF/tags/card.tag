<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="category" required="true" %>
<%@ attribute name="heading" required="true" %>
<%@ attribute name="rating" required="true" type="java.lang.Double" %>
<%@ attribute name="imageUrl" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<c:set var="cardImageUrl" value="${not empty imageUrl ? imageUrl : 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRd2NAjCcjjk7ac57mKCQvgWVTmP0ysxnzQnQ&s'}"/>

<div class="card">
    <div class="card-content">
        <div class="card-image">
            <img src="<c:out value='${cardImageUrl}'/>" alt="Card image">
        </div>
        <div class="heading"><c:out value="${heading}"/></div>
        <div class="card-footer">
            <div class="category"><c:out value="${category}"/></div>
            <div class="card-rating"><span class="material-symbols-outlined card-rating-star">star</span> <fmt:formatNumber value="${rating}" minFractionDigits="1" maxFractionDigits="1"/></div>
        </div>
    </div>
</div>
