//package com.example.demo.dto;
//
//import com.example.demo.Security.Role;
//import jakarta.validation.constraints.NotBlank;
//
//import java.util.Set;
//
//// RegisterRequest.java
//public class RegisterRequest {
//    @NotBlank
//    private String username;
//    @NotBlank
//    private String password;
//    private String firstName;
//    private String lastName;
//    private String email;
//    private String passportNumber;
//    private String afm;
//    // προαιρετικά roles αν φτιάχνεις admin μέσω API
//    private Set<Role> roles;
//    // getters / setters
//
//    public String getUsername() {
//        return username;
//    }
//
//    public void setUsername(String username) {
//        this.username = username;
//    }
//
//    public String getPassword() {
//        return password;
//    }
//
//    public void setPassword(String password) {
//        this.password = password;
//    }
//
//    public String getFirstName() {
//        return firstName;
//    }
//
//    public void setFirstName(String firstName) {
//        this.firstName = firstName;
//    }
//
//    public String getLastName() {
//        return lastName;
//    }
//
//    public void setLastName(String lastName) {
//        this.lastName = lastName;
//    }
//
//    public String getEmail() {
//        return email;
//    }
//
//    public void setEmail(String email) {
//        this.email = email;
//    }
//
//    public String getPassportNumber() {
//        return passportNumber;
//    }
//
//    public void setPassportNumber(String passportNumber) {
//        this.passportNumber = passportNumber;
//    }
//
//    public String getAfm() {
//        return afm;
//    }
//
//    public void setAfm(String afm) {
//        this.afm = afm;
//    }
//
//    public Set<Role> getRoles() {
//        return roles;
//    }
//
//    public void setRoles(Set<Role> roles) {
//        this.roles = roles;
//    }
//}