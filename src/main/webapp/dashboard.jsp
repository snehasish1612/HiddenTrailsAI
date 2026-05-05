<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<% request.setAttribute("pageTitle", "Dashboard"); %>
<!DOCTYPE html>
<html>
<head>
    <jsp:include page="/WEB-INF/fragments/head.jsp" />
</head>
<body>
<jsp:include page="/WEB-INF/fragments/navbar.jsp" />
<main class="page-shell">
    <section class="section-heading">
        <p class="eyebrow">My trips</p>
        <h1>Your itineraries</h1>
    </section>
    <div class="toolbar">
        <a class="btn primary" href="<%= request.getContextPath() %>/plan.jsp">New itinerary</a>
    </div>
    <section class="itinerary-grid" data-dashboard-list></section>
    <p class="form-message" data-dashboard-message></p>
</main>
<script src="<%= request.getContextPath() %>/assets/js/api.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/auth.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/itinerary.js"></script>
</body>
</html>
