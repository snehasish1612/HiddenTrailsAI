<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<% request.setAttribute("pageTitle", "Error"); %>
<!DOCTYPE html>
<html>
<head>
    <jsp:include page="/WEB-INF/fragments/head.jsp" />
</head>
<body>
<jsp:include page="/WEB-INF/fragments/navbar.jsp" />
<main class="empty-state">
    <p class="eyebrow">Something went wrong</p>
    <h1>We could not load that page.</h1>
    <p>Please go back to the planner or dashboard and try again.</p>
    <a class="btn primary" href="<%= request.getContextPath() %>/index.jsp">Back home</a>
</main>
</body>
</html>
