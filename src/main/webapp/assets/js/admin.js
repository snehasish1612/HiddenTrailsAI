document.addEventListener("DOMContentLoaded", function () {
    var session = HiddenTrails.getSession();

    if (!session || session.role !== "admin") {
        window.location.href = HiddenTrails.contextPath + "/dashboard.jsp";
        return;
    }

    loadStats();
    loadBookings();

    HiddenTrails.each("[data-admin-tab]", function (tab) {
        tab.addEventListener("click", function () {
            HiddenTrails.each("[data-admin-tab]", function (item) {
                item.classList.remove("active");
            });

            tab.classList.add("active");

            if (tab.getAttribute("data-admin-tab") === "users") {
                loadUsers();
            } else {
                loadBookings();
            }
        });
    });
});

function loadStats() {
    var container = document.querySelector("[data-admin-stats]");

    HiddenTrails.request("/api/admin/dashboard").then(function (stats) {
        var items = [
            ["Users", stats.totalUsers],
            ["Itineraries", stats.totalItineraries],
            ["Confirmed", stats.confirmedBookings],
            ["Pending", stats.pendingBookings],
            ["Revenue", HiddenTrails.money(stats.totalRevenue)]
        ];

        container.innerHTML = items.map(function (item) {
            return ""
                + "<article class=\"stat-card\">"
                + "<p>" + item[0] + "</p>"
                + "<strong>" + (item[1] == null ? 0 : item[1]) + "</strong>"
                + "</article>";
        }).join("");
    }).catch(function (error) {
        container.innerHTML = "<p class=\"form-message error\">" + escapeHtml(error.message) + "</p>";
    });
}

function loadBookings() {
    var table = document.querySelector("[data-admin-table]");

    HiddenTrails.request("/api/admin/bookings").then(function (data) {
        var bookings = data.bookings || [];
        var rows = bookings.map(function (item) {
            return ""
                + "<tr>"
                + "<td>" + valueOrDash(item.bookingId) + "</td>"
                + "<td>" + valueOrDash(item.itineraryId) + "</td>"
                + "<td>" + HiddenTrails.money(item.totalPrice) + "</td>"
                + "<td>" + escapeHtml(item.bookingStatus || "-") + "</td>"
                + "<td>" + escapeHtml(item.paymentStatus || "-") + "</td>"
                + "<td>" + escapeHtml(item.confirmationNo || "-") + "</td>"
                + "</tr>";
        }).join("");

        if (!rows) {
            rows = "<tr><td colspan=\"6\">No bookings found.</td></tr>";
        }

        table.innerHTML = ""
            + "<table>"
            + "<thead><tr><th>ID</th><th>Itinerary</th><th>Total</th><th>Booking</th><th>Payment</th><th>Confirmation</th></tr></thead>"
            + "<tbody>" + rows + "</tbody>"
            + "</table>";
    }).catch(function (error) {
        table.innerHTML = "<p class=\"form-message error\">" + escapeHtml(error.message) + "</p>";
    });
}

function loadUsers() {
    var table = document.querySelector("[data-admin-table]");

    HiddenTrails.request("/api/admin/users").then(function (data) {
        var users = data.users || [];
        var rows = users.map(function (item) {
            return ""
                + "<tr>"
                + "<td>" + valueOrDash(item.userId) + "</td>"
                + "<td>" + escapeHtml(item.name || "-") + "</td>"
                + "<td>" + escapeHtml(item.email || "-") + "</td>"
                + "<td>" + escapeHtml(item.phone || "-") + "</td>"
                + "<td>" + escapeHtml(item.role || "-") + "</td>"
                + "<td>" + (item.isActive ? "Yes" : "No") + "</td>"
                + "</tr>";
        }).join("");

        if (!rows) {
            rows = "<tr><td colspan=\"6\">No users found.</td></tr>";
        }

        table.innerHTML = ""
            + "<table>"
            + "<thead><tr><th>ID</th><th>Name</th><th>Email</th><th>Phone</th><th>Role</th><th>Active</th></tr></thead>"
            + "<tbody>" + rows + "</tbody>"
            + "</table>";
    }).catch(function (error) {
        table.innerHTML = "<p class=\"form-message error\">" + escapeHtml(error.message) + "</p>";
    });
}

function valueOrDash(value) {
    return value == null ? "-" : value;
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
