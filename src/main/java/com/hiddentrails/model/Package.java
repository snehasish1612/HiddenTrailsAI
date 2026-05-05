package com.hiddentrails.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

/**
 * Package — maps to the packages table.
 * Represents a pre-built travel package created by admins.
 * Each package bundles specific hotel, transport, and food items
 * via the package_items table (represented here as PackageItem list).
 */
public class Package {

    private int        packageId;
    private String     title;
    private String     destination;
    private String     description;
    private BigDecimal basePrice;
    private int        durationDays;
    private int        maxPersons;
    private String     imageUrl;
    private String     status;         // active | inactive | draft
    private Integer    createdBy;      // admin user_id (nullable)
    private Timestamp  createdAt;
    private Timestamp  updatedAt;

    // Items bundled in this package — populated by PackageDAO
    private List<PackageItem> items;

    public Package() {}

    // ── Getters ─────────────────────────────────────────────────
    public int        getPackageId()   { return packageId; }
    public String     getTitle()       { return title; }
    public String     getDestination() { return destination; }
    public String     getDescription() { return description; }
    public BigDecimal getBasePrice()   { return basePrice; }
    public int        getDurationDays(){ return durationDays; }
    public int        getMaxPersons()  { return maxPersons; }
    public String     getImageUrl()    { return imageUrl; }
    public String     getStatus()      { return status; }
    public Integer    getCreatedBy()   { return createdBy; }
    public Timestamp  getCreatedAt()   { return createdAt; }
    public Timestamp  getUpdatedAt()   { return updatedAt; }
    public List<PackageItem> getItems(){ return items; }

    // ── Setters ─────────────────────────────────────────────────
    public void setPackageId(int v)              { this.packageId = v; }
    public void setTitle(String v)               { this.title = v; }
    public void setDestination(String v)         { this.destination = v; }
    public void setDescription(String v)         { this.description = v; }
    public void setBasePrice(BigDecimal v)       { this.basePrice = v; }
    public void setDurationDays(int v)           { this.durationDays = v; }
    public void setMaxPersons(int v)             { this.maxPersons = v; }
    public void setImageUrl(String v)            { this.imageUrl = v; }
    public void setStatus(String v)              { this.status = v; }
    public void setCreatedBy(Integer v)          { this.createdBy = v; }
    public void setCreatedAt(Timestamp v)        { this.createdAt = v; }
    public void setUpdatedAt(Timestamp v)        { this.updatedAt = v; }
    public void setItems(List<PackageItem> v)    { this.items = v; }

    // ════════════════════════════════════════════════════════════
    //  Inner class — maps to package_items table
    //  Kept here as a nested static class since it only exists
    //  in the context of a Package.
    // ════════════════════════════════════════════════════════════
    public static class PackageItem {

        private int    pkgItemId;
        private int    packageId;
        private String itemType;    // hotel | transport | food
        private int    refId;       // FK to hotel_id / transport_id / food_id
        private int    dayNumber;

        public PackageItem() {}

        public int    getPkgItemId() { return pkgItemId; }
        public int    getPackageId() { return packageId; }
        public String getItemType()  { return itemType; }
        public int    getRefId()     { return refId; }
        public int    getDayNumber() { return dayNumber; }

        public void setPkgItemId(int v)  { this.pkgItemId = v; }
        public void setPackageId(int v)  { this.packageId = v; }
        public void setItemType(String v){ this.itemType = v; }
        public void setRefId(int v)      { this.refId = v; }
        public void setDayNumber(int v)  { this.dayNumber = v; }
    }
}