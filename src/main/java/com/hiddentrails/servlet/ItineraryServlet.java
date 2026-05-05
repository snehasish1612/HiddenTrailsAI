package com.hiddentrails.servlet;

import com.hiddentrails.dao.ItineraryDAO;
import com.hiddentrails.model.Itinerary;
import com.hiddentrails.util.ResponseUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * ItineraryServlet
 *
 * GET    /api/itinerary/{id}  → fetch full itinerary with days
 * DELETE /api/itinerary/{id}  → delete a draft itinerary (owner only)
 */
@WebServlet("/api/itinerary/*")
public class ItineraryServlet extends HttpServlet {

    private final ItineraryDAO itineraryDAO = new ItineraryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        Integer itineraryId = extractId(req, resp);
        if (itineraryId == null) return;

        int userId = (int) req.getAttribute("userId");

        Itinerary it = itineraryDAO.findById(itineraryId);

        if (it == null) {
            ResponseUtil.sendError(resp, 404, "NOT_FOUND",
                "Itinerary " + itineraryId + " not found");
            return;
        }

        // Ownership check
        if (it.getUserId() != userId && !"admin".equals(req.getAttribute("role"))) {
            ResponseUtil.sendError(resp, 403, "FORBIDDEN",
                "You do not have access to this itinerary");
            return;
        }

        ResponseUtil.sendJson(resp, it);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        Integer itineraryId = extractId(req, resp);
        if (itineraryId == null) return;

        int userId = (int) req.getAttribute("userId");

        boolean deleted = itineraryDAO.delete(itineraryId, userId);

        if (!deleted) {
            ResponseUtil.sendError(resp, 400, "DELETE_FAILED",
                "Cannot delete itinerary " + itineraryId
                + ". It may not exist, not belong to you, or is not a draft.");
            return;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", "Itinerary " + itineraryId + " deleted successfully");
        ResponseUtil.sendJson(resp, result);
    }

    // ── Extract numeric ID from /api/itinerary/{id} ───────────────
    private Integer extractId(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String pathInfo = req.getPathInfo(); // e.g. "/1042" or "/user/7"
        if (pathInfo == null || pathInfo.equals("/")) {
            ResponseUtil.sendError(resp, 400, "MISSING_ID",
                "Itinerary ID is required in the path");
            return null;
        }
        try {
            // Strip leading slash and parse int
            return Integer.parseInt(pathInfo.substring(1));
        } catch (NumberFormatException e) {
            ResponseUtil.sendError(resp, 400, "INVALID_ID",
                "Itinerary ID must be a number");
            return null;
        }
    }
}