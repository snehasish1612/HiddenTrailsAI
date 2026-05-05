package com.hiddentrails.servlet;

import com.hiddentrails.dao.DestinationDAO;
import com.hiddentrails.util.ResponseUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;

/**
 * DestinationServlet — GET /api/destinations
 *
 * Public endpoint (no auth required).
 * Returns the list of active North Bengal & Sikkim destinations
 * used to populate the landing page destination cards.
 */
@WebServlet("/api/destinations")
public class DestinationServlet extends HttpServlet {

    private final DestinationDAO destinationDAO = new DestinationDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        try {
            List<Map<String, Object>> destinations = destinationDAO.findAll();

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("total",        destinations.size());
            response.put("destinations", destinations);

            ResponseUtil.sendJson(resp, response);

        } catch (Exception e) {
            ResponseUtil.sendError(resp, 500, "SERVER_ERROR",
                "Could not load destinations");
        }
    }
}