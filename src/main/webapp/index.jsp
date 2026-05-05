<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<% request.setAttribute("pageTitle", "Explore"); %>
<!DOCTYPE html>
<html>
<head>
    <jsp:include page="/WEB-INF/fragments/head.jsp" />
</head>
<body>
<jsp:include page="/WEB-INF/fragments/navbar.jsp" />
<main>
    <section class="hero">
        <div class="hero-copy">
            <p class="eyebrow">AI travel planner for the eastern Himalayas</p>
            <h1>HiddenTrailsAI</h1>
            <p>Build practical, scenic itineraries for Darjeeling, Kalimpong, Gangtok, Pelling, Dooars, and nearby hill routes.</p>
            <div class="hero-actions">
                <a class="btn primary" href="<%= request.getContextPath() %>/plan.jsp">Plan a trip</a>
                <a class="btn ghost" href="#destinations">Browse places</a>
            </div>
        </div>
        <div class="hero-panel" aria-label="Featured route">
            <span class="route-tag">Popular route</span>
            <h2>Bagdogra to Gangtok</h2>
            <p>Tea gardens, monasteries, mountain viewpoints, local food stops, and calmer transfer windows.</p>
            <div class="route-stats">
                <span><strong>5</strong> days</span>
                <span><strong>2</strong> travelers</span>
                <span><strong>Mid</strong> budget</span>
            </div>
        </div>
    </section>

    <section class="section" id="destinations">
        <div class="section-heading">
            <p class="eyebrow">Destinations</p>
            <h2>Start with a region</h2>
        </div>
        <div class="destination-grid" data-destination-grid>
            <article class="destination-card">
                <span class="skeleton block"></span>
                <span class="skeleton line"></span>
                <span class="skeleton line short"></span>
            </article>
        </div>
    </section>

    <section class="section split-band">
        <div>
            <p class="eyebrow">What the planner considers</p>
            <h2>Routes that respect time, people, and terrain</h2>
        </div>
        <div class="feature-list">
            <span>Entry point and transfer time</span>
            <span>Budget and accommodation type</span>
            <span>Food preferences and travel style</span>
            <span>Daily pace from relaxed to packed</span>
        </div>
    </section>
</main>
<jsp:include page="/WEB-INF/fragments/footer.jsp" />
<script src="<%= request.getContextPath() %>/assets/js/api.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/auth.js"></script>
</body>
</html>
