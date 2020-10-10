package com.example.okmatka;

import java.util.ArrayList;
import java.util.HashMap;

public class User {

    private String password, name, lastName, roll, experience, favouriteBeach, email,imageURL;
    private int age, numberOfReviews;
    private double rate;
    private String id = "",status;
    private HashMap<String,User> likedPeopleList;
    private ArrayList<User> chatList;


    public User() {
    }

    // this is main constructor
    public User(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.lastName = "NaN";
        this.roll = "NaN";
        this.favouriteBeach = "NaN";
        this.age = 0;
        this.experience = "NaN";
        this.numberOfReviews = 0;
        this.rate = 0.0;
        this.id = "";
        this.imageURL = "default";
        this.status = "";
        this.likedPeopleList = new HashMap<>();
        this.chatList = new ArrayList<>();
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getRoll() {
        return roll;
    }

    public void setRoll(String roll) {
        this.roll = roll;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getFavouriteBeach() {
        return favouriteBeach;
    }

    public void setFavouriteBeach(String favouriteBeach) {
        this.favouriteBeach = favouriteBeach;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getNumberOfReviews() {
        return numberOfReviews;
    }

    public void setNumberOfReviews(int numberOfReviews) {
        this.numberOfReviews = numberOfReviews;
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

    public HashMap<String, User> getLikedPeopleList() {
        return likedPeopleList;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setLikedPeopleList(HashMap<String, User> likedPeopleList) {
        this.likedPeopleList = likedPeopleList;
    }

    public ArrayList<User> getChatList() {
        return chatList;
    }

    public void setChatList(ArrayList<User> chatList) {
        this.chatList = chatList;
    }

    public Boolean isTheSame(User o) {
        return name.equals(o.name) &&
                lastName.equals(o.lastName) &&
                roll.equalsIgnoreCase(o.roll) &&
                experience.equalsIgnoreCase(o.experience) &&
                favouriteBeach.equalsIgnoreCase(o.favouriteBeach) &&
                rate == o.rate &&
                age == o.age;
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
