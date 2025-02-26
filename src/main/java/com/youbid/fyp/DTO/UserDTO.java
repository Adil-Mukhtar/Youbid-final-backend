package com.youbid.fyp.DTO;

public class UserDTO {
    private Integer id;
    private String firstname;
    private String lastname;

    public UserDTO(Integer id, String firstname, String lastname) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
    }

    // Getters
    public Integer getId() {
        return id;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }
}
