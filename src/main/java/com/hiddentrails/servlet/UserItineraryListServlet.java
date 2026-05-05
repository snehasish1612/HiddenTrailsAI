package com.hiddentrails.servlet;

import com.hiddentrails.dao.ItineraryDAO;
import com.hiddentrails.model.Itinerary;
import com.hiddentrails.util.ResponseUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;

/**
 * UserItineraryListServlet — GET /api/itinerary/user/{userId}
 *
 * Returns all itineraries for a user (for the dashboard page).
 * A user can only fetch their own list; admins can fetch any user's list.
 */
@WebServlet("/api/itinerary/user/*")
public class UserItineraryListServlet extends HttpServlet {

    private final ItineraryDAO itineraryDAO = new ItineraryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        // Extract target userId from path: /api/itinerary/user/{userId}
        int targetUserId = extractUserId(req, resp);
        if (targetUserId < 0) return;

        int    requestingUserId = (int)    req.getAttribute("userId");
        String role             = (String) req.getAttribute("role");

        // Users can only see their own itineraries
        if (targetUserId != requestingUserId && !"admin".equals(role)) {
            ResponseUtil.sendError(resp, 403, "FORBIDDEN",
                "You can only view your own itineraries");
            return;
        }

        List<Itinerary> itineraries = itineraryDAO.findByUserId(targetUserId);

        // Build lightweight response (no full day details for list view)
        List<Map<String, Object>> result = new ArrayList<>();
        for (Itinerary it : itineraries) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("itineraryId",   it.getItineraryId());
            row.put("title",         it.getTitle());
            row.put("destination",   it.getDestination());
            row.put("startDate",     it.getStartDate());
            row.put("endDate",       it.getEndDate());
            row.put("totalDays",     it.getTotalDays());
            row.put("adults",        it.getAdults());
            row.put("children",      it.getChildren());
            row.put("budget",        it.getBudget());
            row.put("estimatedCost", it.getEstimatedCost());
            row.put("status",        it.getStatus());
            row.put("createdAt",     it.getCreatedAt());
            result.add(row);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("userId",      targetUserId);
        response.put("total",       result.size());
        response.put("itineraries", result);
        ResponseUtil.sendJson(resp, response);
    }

    private int extractUserId(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String pathInfo = req.getPathInfo(); // e.g. "/7"
        if (pathInfo == null || pathInfo.equals("/")) {
            ResponseUtil.sendError(resp, 400, "MISSING_ID", "User ID is required");
            return -1;
        }
        try {
            return Integer.parseInt(pathInfo.substring(1));
        } catch (NumberFormatException e) {
            ResponseUtil.sendError(resp, 400, "INVALID_ID",
                "User ID must be a number");
            return -1;
        }
    }
}