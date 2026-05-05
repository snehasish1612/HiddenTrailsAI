package com.hiddentrails.service;

import com.google.gson.*;
import com.hiddentrails.dao.*;
import com.hiddentrails.model.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;

/**
 * PricingService — takes the AI-generated Itinerary (with days)
 * and enriches each day with:
 *   - Top hotel options for that day's location
 *   - Transport options from previous location → current location
 *   - Food options for that day's location
 *   - A revised per-day estimated cost based on real DB prices
 *
 * Also builds the ItineraryDay list from the raw AI JSON response.
 */
public class PricingService {

    private static final Logger LOGGER = Logger.getLogger(PricingService.class.getName());

    private static final int MAX_OPTIONS = 2; // Show top 2 options per category

    private final HotelDAO       hotelDAO       = new HotelDAO();
    private final TransportDAO   transportDAO   = new TransportDAO();
    private final FoodOptionDAO  foodOptionDAO  = new FoodOptionDAO();

    /**
     * Parses the raw AI JSON string into a list of ItineraryDay objects,
     * then enriches each day with real pricing data from the database.
     *
     * @param itineraryId  the saved itinerary ID (already persisted)
     * @param aiJson       raw JSON string from AIService
     * @param adults       number of adults (for cost calculation)
     * @return             enriched list of ItineraryDay objects
     */
    public List<ItineraryDay> parseAndEnrich(int itineraryId,
                                              String aiJson,
                                              int adults) {
        JsonObject root = JsonParser.parseString(aiJson).getAsJsonObject();
        JsonArray  daysArray = root.getAsJsonArray("days");

        List<ItineraryDay> days = new java.util.ArrayList<>();
        String prevLocation = null;

        for (JsonElement el : daysArray) {
            JsonObject dayJson = el.getAsJsonObject();

            ItineraryDay day = new ItineraryDay();
            day.setItineraryId(itineraryId);
            day.setDayNumber  (dayJson.get("dayNumber").getAsInt());
            day.setDayTitle   (dayJson.get("dayTitle").getAsString());
            day.setLocation   (dayJson.get("location").getAsString());
            day.setActivity   (dayJson.get("activity").getAsString());

            BigDecimal aiCost = BigDecimal.valueOf(
                dayJson.get("estimatedCostINR").getAsDouble());
            day.setEstimatedCost(aiCost);

            // ── Fetch hotel options for this day's location ────────
            String location = day.getLocation();
            List<Hotel> hotels = hotelDAO.findByLocation(location);
            if (hotels.isEmpty()) {
                // Fallback: try first word of location (e.g. "Darjeeling Hills" → "Darjeeling")
                String fallback = location.split("\\s+")[0];
                hotels = hotelDAO.findByLocation(fallback);
            }
            List<Hotel> topHotels = hotels.stream()
                    .limit(MAX_OPTIONS).toList();
            day.setHotelOptions(topHotels);
            if (!topHotels.isEmpty()) {
                day.setHotelId(topHotels.get(0).getHotelId());
            }

            // ── Fetch transport options: prev location → this location
            if (prevLocation != null && !prevLocation.equalsIgnoreCase(location)) {
                List<Transport> transports = transportDAO.findByRoute(prevLocation, location);
                if (transports.isEmpty()) {
                    transports = transportDAO.findByOrigin(prevLocation);
                }
                List<Transport> topTransports = transports.stream()
                        .limit(MAX_OPTIONS).toList();
                day.setTransportOptions(topTransports);
                if (!topTransports.isEmpty()) {
                    day.setTransportId(topTransports.get(0).getTransportId());
                }
            }

            // ── Fetch food options for this day's location ─────────
            List<FoodOption> foods = foodOptionDAO.findByLocation(location);
            if (foods.isEmpty()) {
                String fallback = location.split("\\s+")[0];
                foods = foodOptionDAO.findByLocation(fallback);
            }
            List<FoodOption> topFoods = foods.stream()
                    .limit(MAX_OPTIONS).toList();
            day.setFoodOptions(topFoods);
            if (!topFoods.isEmpty()) {
                day.setFoodId(topFoods.get(0).getFoodId());
            }

            // ── Recalculate cost using real DB prices ──────────────
            BigDecimal realCost = calculateDayCost(day, adults);
            if (realCost.compareTo(BigDecimal.ZERO) > 0) {
                day.setEstimatedCost(realCost);
            }

            days.add(day);
            prevLocation = location;
        }

        return days;
    }

    /**
     * Sums the cost of the cheapest selected hotel + transport + food
     * for one day, multiplied by the number of adults.
     */
    public BigDecimal calculateDayCost(ItineraryDay day, int adults) {
        BigDecimal cost = BigDecimal.ZERO;
        BigDecimal adultBD = BigDecimal.valueOf(adults);

        // Hotel cost (per night, per room — assume 1 room per 2 adults)
        if (day.getHotelOptions() != null && !day.getHotelOptions().isEmpty()) {
            BigDecimal hotelPrice = day.getHotelOptions().get(0).getPricePerNight();
            int rooms = (int) Math.ceil(adults / 2.0);
            cost = cost.add(hotelPrice.multiply(BigDecimal.valueOf(rooms)));
        }

        // Transport cost
        if (day.getTransportOptions() != null && !day.getTransportOptions().isEmpty()) {
            Transport t = day.getTransportOptions().get(0);
            BigDecimal pp = t.getPricePerPerson();
            BigDecimal total = t.getTotalPrice();
            if (pp != null && pp.compareTo(BigDecimal.ZERO) > 0) {
                cost = cost.add(pp.multiply(adultBD));
            } else if (total != null && total.compareTo(BigDecimal.ZERO) > 0) {
                cost = cost.add(total);
            }
        }

        // Food cost (per person × adults)
        if (day.getFoodOptions() != null && !day.getFoodOptions().isEmpty()) {
            BigDecimal foodPrice = day.getFoodOptions().get(0).getPricePerPerson();
            cost = cost.add(foodPrice.multiply(adultBD));
        }

        return cost;
    }

    /**
     * Sums estimatedCost across all days to get the total trip cost.
     */
    public BigDecimal calculateTotalCost(List<ItineraryDay> days) {
        return days.stream()
                .map(ItineraryDay::getEstimatedCost)
                .filter(c -> c != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Extracts the title from raw AI JSON.
     */
    public String extractTitle(String aiJson) {
        try {
            JsonObject root = JsonParser.parseString(aiJson).getAsJsonObject();
            return root.has("title")
                    ? root.get("title").getAsString()
                    : "Hidden Trails Itinerary";
        } catch (Exception e) {
            return "Hidden Trails Itinerary";
        }
    }

    /**
     * Extracts the AI confidence score from raw AI JSON.
     */
    public BigDecimal extractConfidence(String aiJson) {
        try {
            JsonObject root = JsonParser.parseString(aiJson).getAsJsonObject();
            return root.has("aiConfidence")
                    ? root.get("aiConfidence").getAsBigDecimal()
                    : new BigDecimal("0.85");
        } catch (Exception e) {
            return new BigDecimal("0.85");
        }
    }
}