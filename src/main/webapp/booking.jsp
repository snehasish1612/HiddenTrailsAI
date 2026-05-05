<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<% request.setAttribute("pageTitle", "Booking"); %>
<!DOCTYPE html>
<html>
<head>
    <jsp:include page="/WEB-INF/fragments/head.jsp" />
</head>
<body>
<jsp:include page="/WEB-INF/fragments/navbar.jsp" />
<main class="booking-shell">
    <section class="booking-summary" data-booking-summary></section>
    <form class="booking-card form-stack" data-booking-form>
        <h1>Confirm booking</h1>
        <label>Payment method
            <select name="paymentMethod">
                <option value="razorpay">Razorpay</option>
                <option value="upi">UPI</option>
                <option value="card">Card</option>
            </select>
        </label>
        <label>Payment reference
            <input name="paymentToken" placeholder="Demo payment reference" required>
        </label>
        <button class="btn primary full" type="submit">Confirm booking</button>
        <p class="form-message" data-booking-message></p>
    </form>
</main>
<script src="<%= request.getContextPath() %>/assets/js/api.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/booking.js"></script>
</body>
</html>
