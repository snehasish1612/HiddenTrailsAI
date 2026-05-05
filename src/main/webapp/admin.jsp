<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<% request.setAttribute("pageTitle", "Admin"); %>
<!DOCTYPE html>
<html>
<head>
    <jsp:include page="/WEB-INF/fragments/head.jsp" />
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/admin.css">
</head>
<body>
<jsp:include page="/WEB-INF/fragments/navbar.jsp" />
<main class="admin-shell">
    <section class="section-heading">
        <p class="eyebrow">Admin</p>
        <h1>Operations dashboard</h1>
    </section>
    <section class="stat-grid" data-admin-stats></section>
    <section class="admin-panel">
        <div class="tabs">
            <button class="tab active" type="button" data-admin-tab="bookings">Bookings</button>
            <button class="tab" type="button" data-admin-tab="users">Users</button>
        </div>
        <div class="table-wrap" data-admin-table></div>
    </section>
    <p class="form-message" data-admin-message></p>
</main>
<script src="<%= request.getContextPath() %>/assets/js/api.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/admin.js"></script>
</body>
</html>
