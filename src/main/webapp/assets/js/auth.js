document.addEventListener("DOMContentLoaded", function () {
    loadDestinations();
    initAuthForms();
});

function loadDestinations() {
    var grid = document.querySelector("[data-destination-grid]");

    if (!grid) {
        return;
    }

    HiddenTrails.request("/api/destinations").then(function (data) {
        var destinations = data.destinations || [];

        if (!destinations.length) {
            grid.innerHTML = fallbackDestinations();
            return;
        }

        grid.innerHTML = destinations.map(function (item) {
            var name = item.name || item.destinationName || item.destination || "Destination";
            var region = item.region || item.state || "North Bengal and Sikkim";
            var description = item.description || item.bestFor || "Scenic routes, local stays, food stops, and mountain viewpoints.";

            return ""
                + "<article class=\"destination-card\">"
                + "<span class=\"pill\">" + escapeHtml(region) + "</span>"
                + "<h3>" + escapeHtml(name) + "</h3>"
                + "<p>" + escapeHtml(description) + "</p>"
                + "<a class=\"btn\" href=\"" + HiddenTrails.contextPath + "/plan.jsp?destination=" + encodeURIComponent(name) + "\">Plan this</a>"
                + "</article>";
        }).join("");
    }).catch(function () {
        grid.innerHTML = fallbackDestinations();
    });
}

function initAuthForms() {
    var tabs = document.querySelectorAll("[data-auth-tab]");
    var loginForm = document.querySelector("[data-login-form]");
    var registerForm = document.querySelector("[data-register-form]");
    var msg = document.querySelector("[data-auth-message]");
    var i;

    for (i = 0; i < tabs.length; i += 1) {
        tabs[i].addEventListener("click", function () {
            var mode = this.getAttribute("data-auth-tab");
            var j;

            for (j = 0; j < tabs.length; j += 1) {
                tabs[j].classList.remove("active");
            }

            this.classList.add("active");

            if (loginForm) {
                loginForm.classList.toggle("hidden", mode !== "login");
            }
            if (registerForm) {
                registerForm.classList.toggle("hidden", mode !== "register");
            }

            HiddenTrails.message(msg, "");
        });
    }

    if (loginForm) {
        loginForm.addEventListener("submit", function (event) {
            event.preventDefault();
            submitAuth("/api/auth/login", new FormData(loginForm), msg);
        });
    }

    if (registerForm) {
        registerForm.addEventListener("submit", function (event) {
            event.preventDefault();
            submitAuth("/api/auth/register", new FormData(registerForm), msg);
        });
    }
}

function submitAuth(path, formData, msg) {
    var payload = {};

    formData.forEach(function (value, key) {
        payload[key] = value;
    });

    HiddenTrails.message(msg, "Signing you in...");

    HiddenTrails.request(path, {
        method: "POST",
        body: JSON.stringify(payload)
    }).then(function (session) {
        HiddenTrails.setSession(session);
        HiddenTrails.message(msg, "Success. Opening your dashboard.", "success");
        window.setTimeout(function () {
            window.location.href = HiddenTrails.contextPath + "/dashboard.jsp";
        }, 500);
    }).catch(function (error) {
        HiddenTrails.message(msg, error.message, "error");
    });
}

function fallbackDestinations() {
    var items = [
        ["Darjeeling", "Tea gardens, toy train views, sunrise points, and heritage walks."],
        ["Gangtok", "Monasteries, cafes, clean city routes, and day trips toward Tsomgo."],
        ["Dooars", "Forest stays, river belts, wildlife zones, and slower family trips."],
        ["Pelling", "Kanchenjunga viewpoints, monasteries, waterfalls, and quiet stays."]
    ];

    return items.map(function (item) {
        return ""
            + "<article class=\"destination-card\">"
            + "<span class=\"pill\">Featured</span>"
            + "<h3>" + item[0] + "</h3>"
            + "<p>" + item[1] + "</p>"
            + "<a class=\"btn\" href=\"" + HiddenTrails.contextPath + "/plan.jsp?destination=" + encodeURIComponent(item[0]) + "\">Plan this</a>"
            + "</article>";
    }).join("");
}

function escapeHtml(value) {
    return String(value).replace(/[&<>"']/g, function (char) {
        return {
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            "\"": "&quot;",
            "'": "&#039;"
        }[char];
    });
}
