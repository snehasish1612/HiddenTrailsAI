var bookingTrip = null;

document.addEventListener("DOMContentLoaded", function () {
    if (!HiddenTrails.getSession()) {
        window.location.href = HiddenTrails.contextPath + "/login.jsp";
        return;
    }
    loadBookingTrip();
    initBookingForm();
});

function loadBookingTrip() {
    var id = HiddenTrails.qs("id");
    var summary = document.querySelector("[data-booking-summary]");

    if (!id) {
        summary.innerHTML = "<p class=\"form-message error\">Missing itinerary id.</p>";
        return;
    }

    HiddenTrails.request("/api/itinerary/" + id).then(function (trip) {
        bookingTrip = trip;
        summary.innerHTML = ""
            + "<div class=\"itinerary-hero\">"
            + "<span class=\"pill\">Booking summary</span>"
            + "<h1>" + escapeHtml(bookingTrip.title || bookingTrip.destination) + "</h1>"
            + "<p>" + escapeHtml(bookingTrip.destination) + " - " + HiddenTrails.date(bookingTrip.startDate) + " to " + HiddenTrails.date(bookingTrip.endDate) + "</p>"
            + "<div class=\"itinerary-meta\">"
            + "<span class=\"pill\">" + (bookingTrip.totalDays || "-") + " days</span>"
            + "<span class=\"pill\">" + HiddenTrails.money(bookingTrip.estimatedCost) + "</span>"
            + "</div>"
            + "</div>";
    }).catch(function (error) {
        summary.innerHTML = "<p class=\"form-message error\">" + escapeHtml(error.message) + "</p>";
    });
}

function initBookingForm() {
    var form = document.querySelector("[data-booking-form]");
    var msg = document.querySelector("[data-booking-message]");

    if (!form) {
        return;
    }

    form.addEventListener("submit", function (event) {
        var data;
        var payload;

        event.preventDefault();

        if (!bookingTrip) {
            return;
        }

        data = new FormData(form);
        payload = {
            paymentMethod: data.get("paymentMethod"),
            paymentToken: data.get("paymentToken"),
            totalAmount: bookingTrip.estimatedCost
        };

        HiddenTrails.message(msg, "Confirming booking...");

        HiddenTrails.request("/api/itinerary/" + bookingTrip.itineraryId + "/book", {
            method: "POST",
            body: JSON.stringify(payload)
        }).then(function (result) {
            HiddenTrails.message(msg, "Confirmed: " + result.confirmationNo, "success");
        }).catch(function (error) {
            HiddenTrails.message(msg, error.message, "error");
        });
    });
}

function escapeHtml(value) {
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
