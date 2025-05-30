package com.project.kuk;

public class User {
    private String username;
    private String email;
    private String role;
    private String profileImageUrl;

    public User() {
    }

    public User(String username, String email, String role, String profileImageUrl) {
        this.username = username;
        this.email = email;
        this.role = role;
        this.profileImageUrl = profileImageUrl;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
}

