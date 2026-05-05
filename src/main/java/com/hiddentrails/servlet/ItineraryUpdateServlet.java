package com.hiddentrails.servlet;

import com.google.gson.*;
import com.hiddentrails.dao.ItineraryDAO;
import com.hiddentrails.model.Itinerary;
import com.hiddentrails.model.ItineraryDay;
import com.hiddentrails.service.PricingService;
import com.hiddentrails.util.ResponseUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * ItineraryUpdateServlet — PUT /api/itinerary/{id}/update
 *
 * Called when the user changes hotel, transport, or food selection
 * on the itinerary result page.
 *
 * Request body: { dayNumber, hotelId, transportId, foodId }
 * Response:     { itineraryId, updatedCost, status }
 */
@WebServlet("/api/itinerary/*/update")
public class ItineraryUpdateServlet extends HttpServlet {

    private final ItineraryDAO   itineraryDAO  = new ItineraryDAO();
    private final PricingService pricingService = new PricingService();

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        // Extract itinerary ID from URL: /api/itinerary/{id}/update
        int itineraryId = extractItineraryId(req, resp);
        if (itineraryId < 0) return;

        int userId = (int) req.getAttribute("userId");

        // Ownership check
        Itinerary it = itineraryDAO.findById(itineraryId);
        if (it == null) {
            ResponseUtil.sendError(resp, 404, "NOT_FOUND",
                "Itinerary not found");
            return;
        }
        if (it.getUserId() != userId) {
            ResponseUtil.sendError(resp, 403, "FORBIDDEN",
                "You do not have access to this itinerary");
            return;
        }

        // Parse request body
        JsonObject body = parseBody(req);
        if (!body.has("dayNumber")) {
            ResponseUtil.sendError(resp, 400, "VALIDATION_ERROR",
                "dayNumber is required");
            return;
        }

        int     dayNumber   = body.get("dayNumber").getAsInt();
        Integer hotelId     = body.has("hotelId")     && !body.get("hotelId").isJsonNull()
                              ? body.get("hotelId").getAsInt()     : null;
        Integer transportId = body.has("transportId") && !body.get("transportId").isJsonNull()
                              ? body.get("transportId").getAsInt() : null;
        Integer foodId      = body.has("foodId")      && !body.get("foodId").isJsonNull()
                              ? body.get("foodId").getAsInt()      : null;

        // Update selections in DB
        itineraryDAO.updateDaySelections(
            itineraryId, dayNumber, hotelId, transportId, foodId);

        // Recalculate total cost from all days
        List<ItineraryDay> updatedDays =
            itineraryDAO.findDaysByItineraryId(itineraryId);
        BigDecimal newTotal = updatedDays.stream()
            .map(ItineraryDay::getEstimatedCost)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Update itinerary status to "customised"
        itineraryDAO.updateCostAndStatus(itineraryId, newTotal, "customised");

        // Build response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("itineraryId",  itineraryId);
        response.put("updatedCost",  newTotal);
        response.put("status",       "customised");
        response.put("dayNumber",    dayNumber);
        ResponseUtil.sendJson(resp, response);
    }

    private int extractItineraryId(HttpServletRequest req,
                                    HttpServletResponse resp)
            throws IOException {
        // URI: /api/itinerary/{id}/update
        String[] parts = req.getRequestURI().split("/");
        for (int i = 0; i < parts.length; i++) {
            if ("itinerary".equals(parts[i]) && i + 1 < parts.length) {
                try {
                    return Integer.parseInt(parts[i + 1]);
                } catch (NumberFormatException e) {
                    break;
                }
            }
        }
        ResponseUtil.sendError(resp, 400, "INVALID_ID",
            "Cannot extract itinerary ID from path");
        return -1;
    }

    private JsonObject parseBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        try {
            return JsonParser.parseString(sb.toString()).getAsJsonObject();
        } catch (Exception e) {
            return new JsonObject();
        }
    }
}