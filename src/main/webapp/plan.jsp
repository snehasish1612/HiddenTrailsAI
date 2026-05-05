<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<% request.setAttribute("pageTitle", "Plan"); %>
<!DOCTYPE html>
<html>
<head>
    <jsp:include page="/WEB-INF/fragments/head.jsp" />
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/plan.css">
</head>
<body>
<jsp:include page="/WEB-INF/fragments/navbar.jsp" />
<main class="planner-shell">
    <section class="planner-intro">
        <p class="eyebrow">Trip builder</p>
        <h1>Create an AI itinerary</h1>
        <p>Tell HiddenTrailsAI your destination, dates, people, pace, and preferences.</p>
    </section>
    <form class="planner-form" data-plan-form>
        <div class="form-grid">
            <label>Destination
                <input name="destination" list="destination-list" placeholder="Gangtok" required>
                <datalist id="destination-list"></datalist>
            </label>
            <label>Entry point
                <select name="entryPoint">
                    <option value="Bagdogra">Bagdogra</option>
                    <option value="NJP">NJP</option>
                    <option value="Siliguri">Siliguri</option>
                    <option value="Gangtok">Gangtok</option>
                </select>
            </label>
            <label>Start date <input type="date" name="startDate" required></label>
            <label>End date <input type="date" name="endDate" required></label>
            <label>Adults <input type="number" name="adults" min="1" value="2" required></label>
            <label>Children <input type="number" name="children" min="0" value="0"></label>
            <label>Budget
                <select name="budget">
                    <option value="budget">Budget</option>
                    <option value="mid-range" selected>Mid-range</option>
                    <option value="premium">Premium</option>
                    <option value="luxury">Luxury</option>
                </select>
            </label>
            <label>Accommodation
                <select name="accommodation">
                    <option value="homestay">Homestay</option>
                    <option value="3-star" selected>3-star hotel</option>
                    <option value="boutique">Boutique stay</option>
                    <option value="luxury">Luxury resort</option>
                </select>
            </label>
            <label>Transport
                <select name="transport">
                    <option value="shared-jeep">Shared jeep</option>
                    <option value="private-cab" selected>Private cab</option>
                    <option value="mix">Mixed transport</option>
                </select>
            </label>
            <label>Pace
                <input type="range" name="pace" min="1" max="5" value="3">
            </label>
        </div>
        <fieldset>
            <legend>Travel style</legend>
            <label><input type="checkbox" name="travelStyle" value="nature" checked> Nature</label>
            <label><input type="checkbox" name="travelStyle" value="culture"> Culture</label>
            <label><input type="checkbox" name="travelStyle" value="adventure"> Adventure</label>
            <label><input type="checkbox" name="travelStyle" value="slow-travel"> Slow travel</label>
        </fieldset>
        <fieldset>
            <legend>Food</legend>
            <label><input type="checkbox" name="food" value="local" checked> Local</label>
            <label><input type="checkbox" name="food" value="tibetan"> Tibetan</label>
            <label><input type="checkbox" name="food" value="vegetarian"> Vegetarian</label>
            <label><input type="checkbox" name="food" value="cafes"> Cafes</label>
        </fieldset>
        <label>Special notes
            <textarea name="specialNotes" rows="4" placeholder="Senior travelers, honeymoon, avoid long drives, food restrictions..."></textarea>
        </label>
        <button class="btn primary" type="submit">Generate itinerary</button>
        <p class="form-message" data-plan-message></p>
    </form>
</main>
<script src="<%= request.getContextPath() %>/assets/js/api.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/wizard.js"></script>
</body>
</html>
