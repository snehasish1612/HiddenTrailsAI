package com.hiddentrails.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Booking {
    private int        bookingId;
    private int        userId;
    private int        itineraryId;
    private BigDecimal totalPrice;
    private String     paymentMethod;
    private String     paymentToken;
    private String     paymentStatus;   // pending | paid | failed | refunded
    private String     bookingStatus;   // pending | confirmed | cancelled | completed
    private String     confirmationNo;
    private Timestamp  bookedAt;
    private Timestamp  updatedAt;

    public Booking() {}

    public int        getBookingId()      { return bookingId; }
    public int        getUserId()         { return userId; }
    public int        getItineraryId()    { return itineraryId; }
    public BigDecimal getTotalPrice()     { return totalPrice; }
    public String     getPaymentMethod()  { return paymentMethod; }
    public String     getPaymentToken()   { return paymentToken; }
    public String     getPaymentStatus()  { return paymentStatus; }
    public String     getBookingStatus()  { return bookingStatus; }
    public String     getConfirmationNo() { return confirmationNo; }
    public Timestamp  getBookedAt()       { return bookedAt; }
    public Timestamp  getUpdatedAt()      { return updatedAt; }

    public void setBookingId(int v)            { this.bookingId = v; }
    public void setUserId(int v)               { this.userId = v; }
    public void setItineraryId(int v)          { this.itineraryId = v; }
    public void setTotalPrice(BigDecimal v)    { this.totalPrice = v; }
    public void setPaymentMethod(String v)     { this.paymentMethod = v; }
    public void setPaymentToken(String v)      { this.paymentToken = v; }
    public void setPaymentStatus(String v)     { this.paymentStatus = v; }
    public void setBookingStatus(String v)     { this.bookingStatus = v; }
    public void setConfirmationNo(String v)    { this.confirmationNo = v; }
    public void setBookedAt(Timestamp v)       { this.bookedAt = v; }
    public void setUpdatedAt(Timestamp v)      { this.updatedAt = v; }
}