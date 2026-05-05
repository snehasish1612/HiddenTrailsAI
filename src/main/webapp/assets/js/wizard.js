document.addEventListener("DOMContentLoaded", function () {
    var session = HiddenTrails.getSession();

    if (!session) {
        window.location.href = HiddenTrails.contextPath + "/login.jsp";
        return;
    }

    initPlanner();
    loadDestinationNames();
});

function initPlanner() {
    var form = document.querySelector("[data-plan-form]");
    var msg = document.querySelector("[data-plan-message]");
    var prefillDestination = HiddenTrails.qs("destination");

    if (prefillDestination && form && form.destination) {
        form.destination.value = prefillDestination;
    }

    if (!form) {
        return;
    }

    form.addEventListener("submit", function (event) {
        var payload;

        event.preventDefault();
        payload = buildPayload(form);
        HiddenTrails.message(msg, "Generating your itinerary. This can take a moment...");

        HiddenTrails.request("/api/itinerary/generate", {
            method: "POST",
            body: JSON.stringify(payload)
        }).then(function (itinerary) {
            HiddenTrails.message(msg, "Itinerary ready.", "success");
            window.location.href = HiddenTrails.contextPath + "/itinerary.jsp?id=" + itinerary.itineraryId;
        }).catch(function (error) {
            HiddenTrails.message(msg, error.message, "error");
        });
    });
}

function buildPayload(form) {
    var data = new FormData(form);

    return {
        destination: data.get("destination"),
        startDate: data.get("startDate"),
        endDate: data.get("endDate"),
        adults: Number(data.get("adults") || 1),
        children: Number(data.get("children") || 0),
        budget: data.get("budget"),
        travelStyle: data.getAll("travelStyle"),
        accommodation: data.get("accommodation"),
        transport: data.get("transport"),
        food: data.getAll("food"),
        pace: Number(data.get("pace") || 3),
        specialNotes: data.get("specialNotes"),
        entryPoint: data.get("entryPoint")
    };
}

function loadDestinationNames() {
    var list = document.querySelector("#destination-list");

    if (!list) {
        return;
    }

    HiddenTrails.request("/api/destinations").then(function (data) {
        list.innerHTML = (data.destinations || []).map(function (item) {
            var name = item.name || item.destinationName || item.destination;
            return name ? "<option value=\"" + escapeHtml(name) + "\"></option>" : "";
        }).join("");
    }).catch(function () {
        list.innerHTML = ["Darjeeling", "Gangtok", "Kalimpong", "Pelling", "Dooars"].map(function (name) {
            return "<option value=\"" + name + "\"></option>";
        }).join("");
    });
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
