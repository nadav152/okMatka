package com.example.okmatka;

public class User {

    private String password, name, roll, experience, favouriteBeach, email,imageURL;
    private int age, numberOfReviews;
    private double rate;
    private String id = "";


    public User() {
    }

    public User(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.roll = "NaN";
        this.favouriteBeach = "NaN";
        this.age = 0;
        this.experience = "NaN";
        this.numberOfReviews = 0;
        this.rate = 0.0;
        this.id = "";
        this.imageURL = "default";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoll() {
        return roll;
    }

    public String getExperience() {
        return experience;
    }

    public String getFavouriteBeach() {
        return favouriteBeach;
    }

    public String getEmail() {
        return email;
    }

    public String getImageURL() {
        return imageURL;
    }

    public int getAge() {
        return age;
    }

    public int getNumberOfReviews() {
        return numberOfReviews;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "User{" + "email = " + email +", password='" + password + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
