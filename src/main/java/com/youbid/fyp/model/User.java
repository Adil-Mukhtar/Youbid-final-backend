// src/main/java/com/youbid/fyp/model/User.java
// Add profile picture field to the User model

package com.youbid.fyp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String firstname;
    private String lastname;
    private String password;
    private String email;
    private String gender;
    private BigDecimal balance;
    private String cellphone;
    private Boolean isBanned;
    private Boolean isSuspended;
    private LocalDateTime suspensionDate;
    private String profilePicture;  // New field for profile picture

    @JsonIgnore
    @ManyToMany
    private List<Product> product = new ArrayList<>();

    @JsonIgnore
    @OneToMany
    private List<Review> reviews = new ArrayList<>();

    @JsonIgnore
    @ManyToMany
    private List<Product> wonItems = new ArrayList<>();

    private String role; //admin or normal user



    public User() {
    }

    public User(Integer id, String firstname, String lastname, String password, String email, String gender,
                BigDecimal balance, String cellphone, Boolean isBanned, Boolean isSuspended,
                LocalDateTime suspensionDate, List<Product> product, List<Review> reviews,
                List<Product> wonItems, String role, String profilePicture) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.password = password;
        this.email = email;
        this.gender = gender;
        this.balance = balance;
        this.cellphone = cellphone;
        this.isBanned = isBanned;
        this.isSuspended = isSuspended;
        this.suspensionDate = suspensionDate;
        this.product = product;
        this.reviews = reviews;
        this.wonItems = wonItems;
        this.role = role;
        this.profilePicture = profilePicture;
    }

    // Existing getters and setters...

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    // Rest of the existing getters and setters remain the same
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public BigDecimal getBalance() { return balance;}

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<Product> getProduct() {
        return product;
    }

    public void setProduct(List<Product> product) {
        this.product = product;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public String getCellphone() {return cellphone;}

    public void setCellphone(String cellphone) {this.cellphone = cellphone;}

    public Boolean getBanned() {return isBanned;}

    public void setBanned(Boolean banned) {isBanned = banned;}

    public Boolean getSuspended() {return isSuspended;}

    public void setSuspended(Boolean suspended) {isSuspended = suspended;}

    public LocalDateTime getSuspensionDate() {return suspensionDate;}

    public void setSuspensionDate(LocalDateTime suspensionDate) {this.suspensionDate = suspensionDate;}

    public List<Product> getWonItems() {return wonItems;}

    public void setWonItems(List<Product> wonItems) {this.wonItems = wonItems;}
}