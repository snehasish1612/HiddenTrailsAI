package com.hiddentrails.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

// ─────────────────────────────────────────────────────────────────
//  Itinerary — maps to the itineraries table
// ─────────────────────────────────────────────────────────────────
public class Itinerary {
    private int        itineraryId;
    private int        userId;
    private String     destination;
    private String     title;
    private Date       startDate;
    private Date       endDate;
    private int        totalDays;
    private int        adults;
    private int        children;
    private String     budget;
    private String     travelStyle;    // JSON array string
    private String     accommodation;
    private String     transportPref;
    private String     foodPref;       // JSON array string
    private int        pace;
    private String     entryPoint;
    private String     specialNotes;
    private String     aiPrompt;
    private String     aiResponse;
    private BigDecimal estimatedCost;
    private BigDecimal aiConfidence;
    private String     status;
    private Timestamp  createdAt;
    private Timestamp  updatedAt;

    // Days are populated by ItineraryDAO.findById()
    private List<ItineraryDay> days;

    public Itinerary() {}

    public int        getItineraryId()   { return itineraryId; }
    public int        getUserId()        { return userId; }
    public String     getDestination()   { return destination; }
    public String     getTitle()         { return title; }
    public Date       getStartDate()     { return startDate; }
    public Date       getEndDate()       { return endDate; }
    public int        getTotalDays()     { return totalDays; }
    public int        getAdults()        { return adults; }
    public int        getChildren()      { return children; }
    public String     getBudget()        { return budget; }
    public String     getTravelStyle()   { return travelStyle; }
    public String     getAccommodation() { return accommodation; }
    public String     getTransportPref() { return transportPref; }
    public String     getFoodPref()      { return foodPref; }
    public int        getPace()          { return pace; }
    public String     getEntryPoint()    { return entryPoint; }
    public String     getSpecialNotes()  { return specialNotes; }
    public String     getAiPrompt()      { return aiPrompt; }
    public String     getAiResponse()    { return aiResponse; }
    public BigDecimal getEstimatedCost() { return estimatedCost; }
    public BigDecimal getAiConfidence()  { return aiConfidence; }
    public String     getStatus()        { return status; }
    public Timestamp  getCreatedAt()     { return createdAt; }
    public Timestamp  getUpdatedAt()     { return updatedAt; }
    public List<ItineraryDay> getDays()  { return days; }

    public void setItineraryId(int v)          { this.itineraryId = v; }
    public void setUserId(int v)               { this.userId = v; }
    public void setDestination(String v)       { this.destination = v; }
    public void setTitle(String v)             { this.title = v; }
    public void setStartDate(Date v)           { this.startDate = v; }
    public void setEndDate(Date v)             { this.endDate = v; }
    public void setTotalDays(int v)            { this.totalDays = v; }
    public void setAdults(int v)               { this.adults = v; }
    public void setChildren(int v)             { this.children = v; }
    public void setBudget(String v)            { this.budget = v; }
    public void setTravelStyle(String v)       { this.travelStyle = v; }
    public void setAccommodation(String v)     { this.accommodation = v; }
    public void setTransportPref(String v)     { this.transportPref = v; }
    public void setFoodPref(String v)          { this.foodPref = v; }
    public void setPace(int v)                 { this.pace = v; }
    public void setEntryPoint(String v)        { this.entryPoint = v; }
    public void setSpecialNotes(String v)      { this.specialNotes = v; }
    public void setAiPrompt(String v)          { this.aiPrompt = v; }
    public void setAiResponse(String v)        { this.aiResponse = v; }
    public void setEstimatedCost(BigDecimal v) { this.estimatedCost = v; }
    public void setAiConfidence(BigDecimal v)  { this.aiConfidence = v; }
    public void setStatus(String v)            { this.status = v; }
    public void setCreatedAt(Timestamp v)      { this.createdAt = v; }
    public void setUpdatedAt(Timestamp v)      { this.updatedAt = v; }
    public void setDays(List<ItineraryDay> v)  { this.days = v; }
}