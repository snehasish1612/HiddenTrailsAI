<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<% request.setAttribute("pageTitle", "Login"); %>
<!DOCTYPE html>
<html>
<head>
    <jsp:include page="/WEB-INF/fragments/head.jsp" />
</head>
<body class="auth-page">
<jsp:include page="/WEB-INF/fragments/navbar.jsp" />
<main class="auth-shell">
    <section class="auth-copy">
        <p class="eyebrow">Welcome back</p>
        <h1>Plan, save, and book your mountain itinerary.</h1>
        <p>Use one account to generate routes, compare trip costs, and keep your confirmed bookings together.</p>
    </section>
    <section class="auth-card">
        <div class="tabs" role="tablist">
            <button class="tab active" type="button" data-auth-tab="login">Login</button>
            <button class="tab" type="button" data-auth-tab="register">Register</button>
        </div>
        <form data-login-form class="form-stack">
            <label>Email <input type="email" name="email" required autocomplete="email"></label>
            <label>Password <input type="password" name="password" required autocomplete="current-password"></label>
            <button class="btn primary full" type="submit">Login</button>
        </form>
        <form data-register-form class="form-stack hidden">
            <label>Name <input type="text" name="name" required autocomplete="name"></label>
            <label>Email <input type="email" name="email" required autocomplete="email"></label>
            <label>Phone <input type="tel" name="phone" autocomplete="tel"></label>
            <label>Password <input type="password" name="password" required minlength="6" autocomplete="new-password"></label>
            <button class="btn primary full" type="submit">Create account</button>
        </form>
        <p class="form-message" data-auth-message></p>
    </section>
</main>
<script src="<%= request.getContextPath() %>/assets/js/api.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/auth.js"></script>
</body>
</html>
