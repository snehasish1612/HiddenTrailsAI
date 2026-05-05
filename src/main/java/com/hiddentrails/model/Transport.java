package com.hiddentrails.model;

import java.math.BigDecimal;

public class Transport {
    private int        transportId;
    private String     type;
    private String     routeFrom;
    private String     routeTo;
    private BigDecimal pricePerPerson;  // 0.00 if per-vehicle route
    private BigDecimal totalPrice;      // 0.00 if per-person route
    private int        durationMinutes;
    private String     provider;
    private boolean    isActive;

    public Transport() {}

    public int        getTransportId()    { return transportId; }
    public String     getType()           { return type; }
    public String     getRouteFrom()      { return routeFrom; }
    public String     getRouteTo()        { return routeTo; }
    public BigDecimal getPricePerPerson() { return pricePerPerson; }
    public BigDecimal getTotalPrice()     { return totalPrice; }
    public int        getDurationMinutes(){ return durationMinutes; }
    public String     getProvider()       { return provider; }
    public boolean    isActive()          { return isActive; }

    public void setTransportId(int v)           { this.transportId = v; }
    public void setType(String v)               { this.type = v; }
    public void setRouteFrom(String v)          { this.routeFrom = v; }
    public void setRouteTo(String v)            { this.routeTo = v; }
    public void setPricePerPerson(BigDecimal v) { this.pricePerPerson = v; }
    public void setTotalPrice(BigDecimal v)     { this.totalPrice = v; }
    public void setDurationMinutes(int v)       { this.durationMinutes = v; }
    public void setProvider(String v)           { this.provider = v; }
    public void setActive(boolean v)            { this.isActive = v; }
}