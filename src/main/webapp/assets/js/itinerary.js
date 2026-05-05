document.addEventListener("DOMContentLoaded", function () {
    if (document.querySelector("[data-dashboard-list]")) {
        loadDashboard();
    }
    if (document.querySelector("[data-itinerary-detail]")) {
        loadItineraryDetail();
    }
});

function loadDashboard() {
    var session = HiddenTrails.getSession();
    var list = document.querySelector("[data-dashboard-list]");
    var msg = document.querySelector("[data-dashboard-message]");

    if (!session) {
        window.location.href = HiddenTrails.contextPath + "/login.jsp";
        return;
    }

    HiddenTrails.request("/api/itinerary/user/" + session.userId).then(function (data) {
        var itineraries = data.itineraries || [];

        if (!itineraries.length) {
            list.innerHTML = "<article class=\"trip-card\"><h3>No trips yet</h3><p>Create your first AI itinerary and it will appear here.</p></article>";
            return;
        }

        list.innerHTML = itineraries.map(function (trip) {
            return ""
                + "<article class=\"trip-card\">"
                + "<span class=\"pill\">" + safe(trip.status || "draft") + "</span>"
                + "<h3>" + safe(trip.title || trip.destination) + "</h3>"
                + "<p>" + safe(trip.destination) + " - " + HiddenTrails.date(trip.startDate) + " to " + HiddenTrails.date(trip.endDate) + "</p>"
                + "<p>" + (trip.totalDays || "-") + " days - " + HiddenTrails.money(trip.estimatedCost) + "</p>"
                + "<a class=\"btn\" href=\"" + HiddenTrails.contextPath + "/itinerary.jsp?id=" + trip.itineraryId + "\">Open</a>"
                + "</article>";
        }).join("");
    }).catch(function (error) {
        HiddenTrails.message(msg, error.message, "error");
    });
}

function loadItineraryDetail() {
    var container = document.querySelector("[data-itinerary-detail]");
    var id = HiddenTrails.qs("id");

    if (!HiddenTrails.getSession()) {
        window.location.href = HiddenTrails.contextPath + "/login.jsp";
        return;
    }

    if (!id) {
        container.innerHTML = "<p class=\"form-message error\">Missing itinerary id.</p>";
        return;
    }

    HiddenTrails.request("/api/itinerary/" + id).then(function (trip) {
        var days = trip.days || [];
        var bookLink = "";

        if (trip.status !== "booked") {
            bookLink = "<a class=\"btn primary\" href=\"" + HiddenTrails.contextPath + "/booking.jsp?id=" + trip.itineraryId + "\">Book this trip</a>";
        }

        container.innerHTML = ""
            + "<div class=\"itinerary-hero\">"
            + "<span class=\"pill\">" + safe(trip.status || "draft") + "</span>"
            + "<h1>" + safe(trip.title || trip.destination) + "</h1>"
            + "<p>" + safe(trip.destination) + " - " + HiddenTrails.date(trip.startDate) + " to " + HiddenTrails.date(trip.endDate) + "</p>"
            + "<div class=\"itinerary-meta\">"
            + "<span class=\"pill\">" + (trip.totalDays || days.length || "-") + " days</span>"
            + "<span class=\"pill\">" + (trip.adults || 1) + " adults</span>"
            + "<span class=\"pill\">" + HiddenTrails.money(trip.estimatedCost) + "</span>"
            + "</div>"
            + bookLink
            + "</div>"
            + "<div class=\"day-list\">"
            + (days.length ? days.map(renderDay).join("") : renderMissingDaysMessage(trip))
            + "</div>";
    }).catch(function (error) {
        container.innerHTML = "<p class=\"form-message error\">" + safe(error.message) + "</p>";
    });
}

function renderMissingDaysMessage(trip) {
    return ""
        + "<article class=\"day-card\">"
        + "<h3>Day details are missing</h3>"
        + "<p>This draft was created before the AI day plan was saved. Please create a new itinerary for "
        + safe(trip.destination || "this destination")
        + ".</p>"
        + "<a class=\"btn primary\" href=\"" + HiddenTrails.contextPath + "/plan.jsp?destination="
        + encodeURIComponent(trip.destination || "")
        + "\">Generate again</a>"
        + "</article>";
}

function renderDay(day, index) {
    var title = day.title || day.dayTitle || "Day " + (day.dayNumber || index + 1);
    var activity = day.activities || day.activity || day.summary || day.description || "";
    var hotel = day.hotelName || day.hotel || day.stay || "";
    var food = day.foodOptions || day.meals || day.food || "";
    var transport = day.transportMode || day.transport || "";
    var html = "";

    html += "<article class=\"day-card\">";
    html += "<h3>" + safe(title) + "</h3>";
    if (activity) {
        html += "<p>" + safe(activity) + "</p>";
    }
    if (transport) {
        html += "<p><strong>Transport:</strong> " + safe(transport) + "</p>";
    }
    if (hotel) {
        html += "<p><strong>Stay:</strong> " + safe(hotel) + "</p>";
    }
    if (food) {
        html += "<p><strong>Food:</strong> " + safe(food) + "</p>";
    }
    html += "</article>";

    return html;
}

function safe(value) {
    return String(value == null ? "" : value).replace(/[&<>"']/g, function (char) {
        return {
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            "\"": "&quot;",
            "'": "&#039;"
        }[char];
    });
}
