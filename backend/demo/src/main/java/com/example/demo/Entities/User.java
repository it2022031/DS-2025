package com.example.demo.Entities;

import com.example.demo.Security.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "email"),
                @UniqueConstraint(columnNames = "passport_number"),
                @UniqueConstraint(columnNames = "afm")
        })
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(nullable = false)
    private String username;

    @NotBlank
    @Size(min = 6)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // δέχομαι αλλά δεν επιστρέφω
    @Column(nullable = false)
    private String password;

    @Email
    @NotBlank
    @Column(nullable = false)
    private String email;

    private String firstName;
    private String lastName;

    @Column(name = "passport_number")
    private String passportNumber;

    private String afm;  // Greek Tax Identification Number

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="user_roles", joinColumns=@JoinColumn(name="user_id"))
    @Column(name="role")
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();

    // Κατάσταση λογαριασμού (μπορείς να τις επεκτείνεις/χρήσεις στο μέλλον)
    private boolean enabled = true;
    private boolean accountNonLocked = true;

    // Σχέσεις
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "user-properties")
    private List<Property> properties = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Rental> rentals = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private ApprovalStatus renterRequestStatus;

    public ApprovalStatus getRenterRequestStatus() {
        return renterRequestStatus;
    }

    public void setRenterRequestStatus(ApprovalStatus renterRequestStatus) {
        this.renterRequestStatus = renterRequestStatus;
    }

    public User() {}

    // constructor για registration / δημιουργία
    public User(String username,
                String password,
                String email,
                String firstName,
                String lastName,
                String passportNumber,
                String afm,
                Role initialRole) {
        this.username       = username;
        this.password       = password;
        this.email          = email;
        this.firstName      = firstName;
        this.lastName       = lastName;
        this.passportNumber = passportNumber;
        this.afm            = afm;
        // ensure at least USER if null
        this.roles.clear();
        this.roles.add(initialRole != null ? initialRole : Role.USER);
    }

    // Getters & Setters

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // δεν εκθέτει το password σε JSON output λόγω @JsonProperty WRITE_ONLY
    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() { // μόνο για internal χρήση (π.χ. password comparison)
        return password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassportNumber() {
        return passportNumber;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public String getAfm() {
        return afm;
    }

    public void setAfm(String afm) {
        this.afm = afm;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles != null ? new HashSet<>(roles) : new HashSet<>();
    }
    public void addRole(Role role) {
        if (role != null) {
            this.roles.add(role);
        }
    }

    /**
     * Revoke a role.
     */
    public void removeRole(Role role) {
        if (role != null) {
            this.roles.remove(role);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public List<Rental> getRentals() {
        return rentals;
    }

    public void setRentals(List<Rental> rentals) {
        this.rentals = rentals;
    }

    // βοηθητικά για σχέσεις
    public void addProperty(Property property) {
        properties.add(property);
        property.setOwner(this);
    }

    public void removeProperty(Property property) {
        properties.remove(property);
        property.setOwner(null);
    }

    public void addRental(Rental rental) {
        rentals.add(rental);
        rental.setUser(this);
    }

    public void removeRental(Rental rental) {
        rentals.remove(rental);
        rental.setUser(null);
    }

    // υπολογιζόμενο πλήρες όνομα
    @Transient
    public String getFullName() {
        String fn = firstName != null ? firstName : "";
        String ln = lastName != null ? lastName : "";
        return (fn + " " + ln).trim();
    }
}
