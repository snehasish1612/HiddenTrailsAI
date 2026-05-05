package com.hiddentrails.model;

import java.math.BigDecimal;

public class ItineraryDay {
    private int        dayId;
    private int        itineraryId;
    private int        dayNumber;
    private String     dayTitle;
    private String     activity;
    private String     location;
    private BigDecimal estimatedCost;
    private Integer    hotelId;
    private Integer    transportId;
    private Integer    foodId;
    private String     notes;

    // Enriched option lists — populated by PricingService
    private java.util.List<Hotel>       hotelOptions;
    private java.util.List<Transport>   transportOptions;
    private java.util.List<FoodOption>  foodOptions;

    public ItineraryDay() {}

    public int        getDayId()           { return dayId; }
    public int        getItineraryId()     { return itineraryId; }
    public int        getDayNumber()       { return dayNumber; }
    public String     getDayTitle()        { return dayTitle; }
    public String     getActivity()        { return activity; }
    public String     getLocation()        { return location; }
    public BigDecimal getEstimatedCost()   { return estimatedCost; }
    public Integer    getHotelId()         { return hotelId; }
    public Integer    getTransportId()     { return transportId; }
    public Integer    getFoodId()          { return foodId; }
    public String     getNotes()           { return notes; }
    public java.util.List<Hotel>      getHotelOptions()     { return hotelOptions; }
    public java.util.List<Transport>  getTransportOptions() { return transportOptions; }
    public java.util.List<FoodOption> getFoodOptions()      { return foodOptions; }

    public void setDayId(int v)                    { this.dayId = v; }
    public void setItineraryId(int v)              { this.itineraryId = v; }
    public void setDayNumber(int v)                { this.dayNumber = v; }
    public void setDayTitle(String v)              { this.dayTitle = v; }
    public void setActivity(String v)              { this.activity = v; }
    public void setLocation(String v)              { this.location = v; }
    public void setEstimatedCost(BigDecimal v)     { this.estimatedCost = v; }
    public void setHotelId(Integer v)              { this.hotelId = v; }
    public void setTransportId(Integer v)          { this.transportId = v; }
    public void setFoodId(Integer v)               { this.foodId = v; }
    public void setNotes(String v)                 { this.notes = v; }
    public void setHotelOptions(java.util.List<Hotel> v)      { this.hotelOptions = v; }
    public void setTransportOptions(java.util.List<Transport> v){ this.transportOptions = v; }
    public void setFoodOptions(java.util.List<FoodOption> v)   { this.foodOptions = v; }
}