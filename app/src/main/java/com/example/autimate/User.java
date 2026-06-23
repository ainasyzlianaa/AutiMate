package com.example.autimate;

public class User {

    public String id;
    public String email;
    public String role;

    // EMPTY CONSTRUCTOR (REQUIRED for Firebase)
    public User() {
    }

    // CONSTRUCTOR
    public User(String id, String email, String role) {
        this.id = id;
        this.email = email;
        this.role = role;
    }
}