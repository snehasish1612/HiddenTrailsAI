<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<% request.setAttribute("pageTitle", "Itinerary"); %>
<!DOCTYPE html>
<html>
<head>
    <jsp:include page="/WEB-INF/fragments/head.jsp" />
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/itinerary.css">
</head>
<body>
<jsp:include page="/WEB-INF/fragments/navbar.jsp" />
<main class="page-shell">
    <section class="itinerary-detail" data-itinerary-detail>
        <p class="form-message">Loading itinerary...</p>
    </section>
</main>
<script src="<%= request.getContextPath() %>/assets/js/api.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/itinerary.js"></script>
</body>
</html>
