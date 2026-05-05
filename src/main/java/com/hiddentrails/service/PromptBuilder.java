package com.hiddentrails.service;

import com.hiddentrails.model.ItineraryRequest;

import java.util.List;

/**
 * PromptBuilder — constructs the natural-language prompt
 * sent to the OpenAI / Gemini API.
 *
 * Appends a strict JSON schema instruction so the AI always
 * returns structured, parseable output.
 */
public class PromptBuilder {

    private PromptBuilder() {}

    public static String build(ItineraryRequest req) {

        // ── Compute trip duration ──────────────────────────────────
        long days = computeDays(req.getStartDate(), req.getEndDate());

        // ── Travel style sentence ──────────────────────────────────
        String styleText = listToSentence(req.getTravelStyle());
        String foodText  = listToSentence(req.getFood());

        // ── Pace description ───────────────────────────────────────
        String paceText = switch (req.getPace()) {
            case 1  -> "very relaxed with minimal daily activities";
            case 2  -> "relaxed with 2–3 activities per day";
            case 4  -> "active with 4–5 activities per day";
            case 5  -> "packed with maximum activities each day";
            default -> "moderate with 3–4 activities per day";
        };

        // ── Group description ──────────────────────────────────────
        String groupText = req.getAdults() + " adult"
            + (req.getAdults() > 1 ? "s" : "")
            + (req.getChildren() > 0
               ? " and " + req.getChildren() + " child"
                 + (req.getChildren() > 1 ? "ren" : "")
               : "");

        // ── Build the prompt ───────────────────────────────────────
        StringBuilder sb = new StringBuilder();
        sb.append("You are an expert travel planner for North Bengal and Sikkim, India.\n\n");
        sb.append("Plan a detailed ").append(days).append("-day travel itinerary for ")
          .append(groupText)
          .append(" visiting ").append(req.getDestination())
          .append(" from ").append(req.getStartDate())
          .append(" to ").append(req.getEndDate()).append(".\n\n");

        sb.append("TRIP DETAILS:\n");
        sb.append("- Entry point: ").append(req.getEntryPoint()).append("\n");
        sb.append("- Budget: ").append(req.getBudget()).append("\n");
        sb.append("- Accommodation: ").append(req.getAccommodation()).append("\n");
        sb.append("- Transport preference: ").append(req.getTransport()).append("\n");
        sb.append("- Travel style: ").append(styleText).append("\n");
        sb.append("- Food preference: ").append(foodText).append("\n");
        sb.append("- Travel pace: ").append(paceText).append("\n");

        if (req.getSpecialNotes() != null && !req.getSpecialNotes().isBlank()) {
            sb.append("- Special notes: ").append(req.getSpecialNotes()).append("\n");
        }

        sb.append("\nIMPORTANT CONSTRAINTS:\n");
        sb.append("- All locations must be real places in North Bengal or Sikkim, India.\n");
        sb.append("- Day 1 must start from ").append(req.getEntryPoint()).append(".\n");
        sb.append("- Include permit-required areas (Nathula, North Sikkim) only if time allows.\n");
        sb.append("- Respect altitude acclimatisation — do not go to high-altitude zones on Day 1.\n");
        sb.append("- Estimate realistic daily costs in Indian Rupees (INR) for ").append(groupText).append(".\n\n");

        // ── JSON schema instruction ────────────────────────────────
        sb.append("RESPOND ONLY WITH A VALID JSON OBJECT. No markdown, no explanation, no code fences.\n");
        sb.append("Use compact JSON. Keep every string on one line. Do not put line breaks inside string values.\n");
        sb.append("Keep activity under 35 words per day. Include exactly ").append(days).append(" day objects.\n");
        sb.append("The JSON must match this exact structure:\n\n");
        sb.append("{\n");
        sb.append("  \"title\": \"string — catchy trip name\",\n");
        sb.append("  \"totalDays\": number,\n");
        sb.append("  \"estimatedCostINR\": number,\n");
        sb.append("  \"aiConfidence\": number between 0 and 1,\n");
        sb.append("  \"days\": [\n");
        sb.append("    {\n");
        sb.append("      \"dayNumber\": number,\n");
        sb.append("      \"dayTitle\": \"string\",\n");
        sb.append("      \"location\": \"string — primary location for the day\",\n");
        sb.append("      \"activity\": \"string — detailed description of the day's plan\",\n");
        sb.append("      \"estimatedCostINR\": number,\n");
        sb.append("      \"highlights\": [\"string\", \"string\"]\n");
        sb.append("    }\n");
        sb.append("  ]\n");
        sb.append("}\n");

        return sb.toString();
    }

    // ── Helpers ───────────────────────────────────────────────────

    private static long computeDays(String startDate, String endDate) {
        try {
            java.time.LocalDate start = java.time.LocalDate.parse(startDate);
            java.time.LocalDate end   = java.time.LocalDate.parse(endDate);
            return java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
        } catch (Exception e) {
            return 7; // default fallback
        }
    }

    private static String listToSentence(List<String> items) {
        if (items == null || items.isEmpty()) return "any";
        if (items.size() == 1) return items.get(0);
        return String.join(", ", items.subList(0, items.size() - 1))
               + " and " + items.get(items.size() - 1);
    }
}
