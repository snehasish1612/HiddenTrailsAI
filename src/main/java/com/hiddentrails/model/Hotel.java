package com.hiddentrails.model;

import java.math.BigDecimal;

public class Hotel {
    private int        hotelId;
    private String     name;
    private String     location;
    private String     address;
    private BigDecimal pricePerNight;
    private int        rating;
    private String     category;
    private String     amenities;
    private String     imageUrl;
    private boolean    isActive;

    public Hotel() {}

    public int        getHotelId()       { return hotelId; }
    public String     getName()          { return name; }
    public String     getLocation()      { return location; }
    public String     getAddress()       { return address; }
    public BigDecimal getPricePerNight() { return pricePerNight; }
    public int        getRating()        { return rating; }
    public String     getCategory()      { return category; }
    public String     getAmenities()     { return amenities; }
    public String     getImageUrl()      { return imageUrl; }
    public boolean    isActive()         { return isActive; }

    public void setHotelId(int v)              { this.hotelId = v; }
    public void setName(String v)              { this.name = v; }
    public void setLocation(String v)          { this.location = v; }
    public void setAddress(String v)           { this.address = v; }
    public void setPricePerNight(BigDecimal v) { this.pricePerNight = v; }
    public void setRating(int v)               { this.rating = v; }
    public void setCategory(String v)          { this.category = v; }
    public void setAmenities(String v)         { this.amenities = v; }
    public void setImageUrl(String v)          { this.imageUrl = v; }
    public void setActive(boolean v)           { this.isActive = v; }
}