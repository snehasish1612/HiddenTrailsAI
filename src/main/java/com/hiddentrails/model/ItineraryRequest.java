package com.hiddentrails.model;

import java.util.List;

/**
 * ItineraryRequest — maps the JSON body from POST /api/itinerary/generate.
 * Not a DB entity — used only for Gson deserialisation in
 * ItineraryGenerateServlet and PromptBuilder.
 */
public class ItineraryRequest {
    private String       destination;
    private String       startDate;      // "yyyy-MM-dd"
    private String       endDate;        // "yyyy-MM-dd"
    private int          adults;
    private int          children;
    private String       budget;         // budget | mid-range | premium | luxury
    private List<String> travelStyle;    // ["adventure","nature"]
    private String       accommodation;  // 3-star | homestay | luxury etc.
    private String       transport;      // shared-jeep | private-cab | mix
    private List<String> food;           // ["local","tibetan"]
    private int          pace;           // 1–5
    private String       specialNotes;
    private String       entryPoint;     // NJP | Bagdogra | Siliguri

    public ItineraryRequest() {}

    public String       getDestination()   { return destination; }
    public String       getStartDate()     { return startDate; }
    public String       getEndDate()       { return endDate; }
    public int          getAdults()        { return adults; }
    public int          getChildren()      { return children; }
    public String       getBudget()        { return budget; }
    public List<String> getTravelStyle()   { return travelStyle; }
    public String       getAccommodation() { return accommodation; }
    public String       getTransport()     { return transport; }
    public List<String> getFood()          { return food; }
    public int          getPace()          { return pace; }
    public String       getSpecialNotes()  { return specialNotes; }
    public String       getEntryPoint()    { return entryPoint; }

    public void setDestination(String v)         { this.destination = v; }
    public void setStartDate(String v)           { this.startDate = v; }
    public void setEndDate(String v)             { this.endDate = v; }
    public void setAdults(int v)                 { this.adults = v; }
    public void setChildren(int v)               { this.children = v; }
    public void setBudget(String v)              { this.budget = v; }
    public void setTravelStyle(List<String> v)   { this.travelStyle = v; }
    public void setAccommodation(String v)       { this.accommodation = v; }
    public void setTransport(String v)           { this.transport = v; }
    public void setFood(List<String> v)          { this.food = v; }
    public void setPace(int v)                   { this.pace = v; }
    public void setSpecialNotes(String v)        { this.specialNotes = v; }
    public void setEntryPoint(String v)          { this.entryPoint = v; }

    /** Basic validation — returns an error message or null if valid. */
    public String validate() {
        if (destination == null || destination.isBlank())
            return "destination is required";
        if (startDate == null || startDate.isBlank())
            return "startDate is required";
        if (endDate == null || endDate.isBlank())
            return "endDate is required";
        if (adults < 1)
            return "adults must be at least 1";
        if (startDate.compareTo(endDate) > 0)
            return "startDate must be before endDate";
        return null; // valid
    }
}