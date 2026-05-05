var HiddenTrails = (function () {
    var contextPath = window.HIDDEN_TRAILS_CONTEXT || "";
    var storageKey = "hiddenTrailsSession";

    function each(selector, callback) {
        var items = document.querySelectorAll(selector);
        for (var i = 0; i < items.length; i += 1) {
            callback(items[i], i);
        }
    }

    function getSession() {
        try {
            return JSON.parse(localStorage.getItem(storageKey)) || null;
        } catch (error) {
            return null;
        }
    }

    function setSession(session) {
        localStorage.setItem(storageKey, JSON.stringify(session));
        refreshNav();
    }

    function clearSession() {
        localStorage.removeItem(storageKey);
        refreshNav();
    }

    function request(path, options) {
        var session = getSession();
        var requestOptions = options || {};
        var headers = {
            "Content-Type": "application/json"
        };
        var optionHeaders = requestOptions.headers || {};
        var key;

        for (key in optionHeaders) {
            if (Object.prototype.hasOwnProperty.call(optionHeaders, key)) {
                headers[key] = optionHeaders[key];
            }
        }

        if (session && session.token) {
            headers.Authorization = "Bearer " + session.token;
        }

        requestOptions.headers = headers;

        return fetch(contextPath + path, requestOptions).then(function (response) {
            return response.json().catch(function () {
                return {};
            }).then(function (data) {
                if (!response.ok) {
                    throw new Error(data.message || data.error || "Request failed (" + response.status + ")");
                }
                return data;
            });
        });
    }

    function money(value) {
        var number = Number(value || 0);
        return new Intl.NumberFormat("en-IN", {
            style: "currency",
            currency: "INR",
            maximumFractionDigits: 0
        }).format(number);
    }

    function date(value) {
        if (!value) {
            return "-";
        }
        return new Intl.DateTimeFormat("en-IN", {
            day: "2-digit",
            month: "short",
            year: "numeric"
        }).format(new Date(value));
    }

    function qs(name) {
        return new URLSearchParams(window.location.search).get(name);
    }

    function message(element, text, type) {
        if (!element) {
            return;
        }
        element.textContent = text || "";
        element.classList.remove("error", "success");
        if (type) {
            element.classList.add(type);
        }
    }

    function refreshNav() {
        var session = getSession();

        each("[data-auth-link]", function (link) {
            link.textContent = session ? "Dashboard" : "Login";
            link.href = session ? contextPath + "/dashboard.jsp" : contextPath + "/login.jsp";
        });

        each("[data-logout]", function (button) {
            button.classList.toggle("hidden", !session);
        });

        each("[data-admin-link]", function (link) {
            link.classList.toggle("hidden", !session || session.role !== "admin");
        });
    }

    document.addEventListener("DOMContentLoaded", function () {
        var navToggle = document.querySelector("[data-nav-toggle]");
        var siteNav = document.querySelector("[data-site-nav]");

        refreshNav();

        if (navToggle && siteNav) {
            navToggle.addEventListener("click", function () {
                siteNav.classList.toggle("open");
            });
        }

        each("[data-logout]", function (button) {
            button.addEventListener("click", function () {
                clearSession();
                window.location.href = contextPath + "/index.jsp";
            });
        });
    });

    return {
        clearSession: clearSession,
        contextPath: contextPath,
        date: date,
        each: each,
        getSession: getSession,
        message: message,
        money: money,
        qs: qs,
        request: request,
        setSession: setSession
    };
}());
