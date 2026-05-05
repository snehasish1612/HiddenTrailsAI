package com.hiddentrails.servlet;

import com.google.gson.*;
import com.hiddentrails.dao.ItineraryDAO;
import com.hiddentrails.exception.AIServiceException;
import com.hiddentrails.model.*;
import com.hiddentrails.service.*;
import com.hiddentrails.util.ResponseUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ItineraryGenerateServlet — POST /api/itinerary/generate
 *
 * Full lifecycle:
 *  1. Parse & validate JSON request body → ItineraryRequest
 *  2. Read userId from request attribute (set by AuthFilter)
 *  3. Build AI prompt via PromptBuilder
 *  4. Call AIService → raw JSON string
 *  5. Parse & enrich days via PricingService
 *  6. Persist Itinerary + ItineraryDays to MySQL
 *  7. Return full itinerary JSON to client
 */
@WebServlet("/api/itinerary/generate")
public class ItineraryGenerateServlet extends HttpServlet {

    private static final Logger LOGGER =
        Logger.getLogger(ItineraryGenerateServlet.class.getName());

    private final AIService       aiService       = new AIService();
    private final PricingService  pricingService  = new PricingService();
    private final ItineraryDAO    itineraryDAO    = new ItineraryDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        // ── 1. Parse request body ──────────────────────────────────
        ItineraryRequest request = parseRequest(req);
        if (request == null) {
            ResponseUtil.sendError(resp, 400, "INVALID_JSON",
                "Request body must be valid JSON");
            return;
        }

        // ── 2. Validate fields ─────────────────────────────────────
        String validationError = request.validate();
        if (validationError != null) {
            ResponseUtil.sendError(resp, 400, "VALIDATION_ERROR", validationError);
            return;
        }

        // ── 3. Get userId from AuthFilter attribute ────────────────
        int userId = (int) req.getAttribute("userId");

        try {
            // ── 4. Build AI prompt ─────────────────────────────────
            String prompt = PromptBuilder.build(request);
            LOGGER.info("Generating itinerary for userId=" + userId
                        + " destination=" + request.getDestination());

            // ── 5. Call AI API ─────────────────────────────────────
            String aiJson = aiService.generateItinerary(prompt);

            // ── 6. Extract title and confidence from AI response ───
            String     title      = pricingService.extractTitle(aiJson);
            BigDecimal confidence = pricingService.extractConfidence(aiJson);
            List<ItineraryDay> days = pricingService.parseAndEnrich(
                0, aiJson, request.getAdults());

            // ── 7. Build Itinerary entity (without days yet) ───────
            Itinerary itinerary = buildItinerary(request, userId,
                                                  title, prompt,
                                                  aiJson, confidence);

            // ── 8. Save itinerary to DB → get generated ID ─────────
            int itineraryId = itineraryDAO.save(itinerary);
            itinerary.setItineraryId(itineraryId);

            // ── 9. Parse + enrich days with pricing data ───────────
            for (ItineraryDay day : days) {
                day.setItineraryId(itineraryId);
            }

            // ── 10. Save days to DB ────────────────────────────────
            itineraryDAO.saveDays(days);

            // ── 11. Calculate total cost and update itinerary ──────
            BigDecimal totalCost = pricingService.calculateTotalCost(days);
            itineraryDAO.updateCostAndStatus(itineraryId, totalCost, "draft");
            itinerary.setEstimatedCost(totalCost);
            itinerary.setDays(days);

            // ── 12. Build and send response ────────────────────────
            Map<String, Object> response = buildResponse(itinerary, days);
            ResponseUtil.sendJson(resp, response);

        } catch (AIServiceException e) {
            LOGGER.log(Level.WARNING, "AI service error", e);
            ResponseUtil.sendError(resp, 503, "AI_SERVICE_ERROR", e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error in generate servlet", e);
            ResponseUtil.sendError(resp, 500, "SERVER_ERROR",
                "An unexpected error occurred. Please try again.");
        }
    }

    // ── Parse request body into ItineraryRequest POJO ─────────────
    private ItineraryRequest parseRequest(HttpServletRequest req) {
        try {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = req.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
            }
            Gson gson = ResponseUtil.getGson();
            return gson.fromJson(sb.toString(), ItineraryRequest.class);
        } catch (Exception e) {
            return null;
        }
    }

    // ── Build Itinerary entity from request + AI output ───────────
    private Itinerary buildItinerary(ItineraryRequest req,
                                      int userId,
                                      String title,
                                      String prompt,
                                      String aiJson,
                                      BigDecimal confidence) {
        Itinerary it = new Itinerary();
        it.setUserId      (userId);
        it.setDestination (req.getDestination());
        it.setTitle       (title);
        it.setStartDate   (Date.valueOf(req.getStartDate()));
        it.setEndDate     (Date.valueOf(req.getEndDate()));
        it.setAdults      (req.getAdults());
        it.setChildren    (req.getChildren());
        it.setBudget      (req.getBudget());
        it.setAccommodation(req.getAccommodation());
        it.setTransportPref(req.getTransport());
        it.setEntryPoint  (req.getEntryPoint());
        it.setSpecialNotes(req.getSpecialNotes());
        it.setPace        (req.getPace());
        it.setAiPrompt    (prompt);
        it.setAiResponse  (aiJson);
        it.setAiConfidence(confidence);
        it.setStatus      ("draft");
        it.setEstimatedCost(BigDecimal.ZERO);

        // Compute totalDays
        long days = java.time.temporal.ChronoUnit.DAYS.between(
            java.time.LocalDate.parse(req.getStartDate()),
            java.time.LocalDate.parse(req.getEndDate())) + 1;
        it.setTotalDays((int) days);

        // Serialise list fields to JSON strings
        Gson gson = ResponseUtil.getGson();
        if (req.getTravelStyle() != null)
            it.setTravelStyle(gson.toJson(req.getTravelStyle()));
        if (req.getFood() != null)
            it.setFoodPref(gson.toJson(req.getFood()));

        return it;
    }

    // ── Build the final JSON response map ─────────────────────────
    private Map<String, Object> buildResponse(Itinerary it,
                                               List<ItineraryDay> days) {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("itineraryId",   it.getItineraryId());
        resp.put("title",         it.getTitle());
        resp.put("destination",   it.getDestination());
        resp.put("startDate",     it.getStartDate());
        resp.put("endDate",       it.getEndDate());
        resp.put("totalDays",     it.getTotalDays());
        resp.put("adults",        it.getAdults());
        resp.put("children",      it.getChildren());
        resp.put("budget",        it.getBudget());
        resp.put("status",        it.getStatus());
        resp.put("estimatedCost", it.getEstimatedCost());
        resp.put("aiConfidence",  it.getAiConfidence());
        resp.put("days",          days);
        return resp;
    }
}
