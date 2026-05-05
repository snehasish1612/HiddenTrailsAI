<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<header class="site-header">
    <a class="brand" href="<%= request.getContextPath() %>/index.jsp" aria-label="HiddenTrailsAI home">
        <span class="brand-mark">HT</span>
        <span>HiddenTrailsAI</span>
    </a>
    <button class="nav-toggle" type="button" aria-label="Open navigation" data-nav-toggle>
        <span></span>
        <span></span>
        <span></span>
    </button>
    <nav class="site-nav" data-site-nav>
        <a href="<%= request.getContextPath() %>/index.jsp">Explore</a>
        <a href="<%= request.getContextPath() %>/plan.jsp">Plan</a>
        <a href="<%= request.getContextPath() %>/dashboard.jsp">Dashboard</a>
        <a href="<%= request.getContextPath() %>/admin.jsp" data-admin-link>Admin</a>
        <a class="nav-auth" href="<%= request.getContextPath() %>/login.jsp" data-auth-link>Login</a>
        <button class="nav-logout hidden" type="button" data-logout>Logout</button>
    </nav>
</header>
