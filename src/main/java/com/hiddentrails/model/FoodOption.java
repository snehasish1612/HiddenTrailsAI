package com.hiddentrails.model;

import java.math.BigDecimal;

public class FoodOption {
    private int        foodId;
    private String     name;
    private String     location;
    private String     cuisineType;
    private BigDecimal pricePerPerson;
    private String     mealType;
    private boolean    isVegetarian;
    private String     imageUrl;
    private boolean    isActive;

    public FoodOption() {}

    public int        getFoodId()         { return foodId; }
    public String     getName()           { return name; }
    public String     getLocation()       { return location; }
    public String     getCuisineType()    { return cuisineType; }
    public BigDecimal getPricePerPerson() { return pricePerPerson; }
    public String     getMealType()       { return mealType; }
    public boolean    isVegetarian()      { return isVegetarian; }
    public String     getImageUrl()       { return imageUrl; }
    public boolean    isActive()          { return isActive; }

    public void setFoodId(int v)               { this.foodId = v; }
    public void setName(String v)              { this.name = v; }
    public void setLocation(String v)          { this.location = v; }
    public void setCuisineType(String v)       { this.cuisineType = v; }
    public void setPricePerPerson(BigDecimal v){ this.pricePerPerson = v; }
    public void setMealType(String v)          { this.mealType = v; }
    public void setVegetarian(boolean v)       { this.isVegetarian = v; }
    public void setImageUrl(String v)          { this.imageUrl = v; }
    public void setActive(boolean v)           { this.isActive = v; }
}