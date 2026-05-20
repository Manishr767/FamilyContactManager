package com.family.model;

import java.time.LocalDate;

public class Contact {
    private int id;
    private String firstName;
    private String lastName;
    private String relationship;
    private String phone;
    private String email;
    private String address;
    private LocalDate birthday;
    private String notes;
    private String photoPath;

    public Contact() {}

    public Contact(int id, String firstName, String lastName, String relationship,
                   String phone, String email, String address,
                   LocalDate birthday, String notes, String photoPath) {
        this.id           = id;
        this.firstName    = firstName;
        this.lastName     = lastName;
        this.relationship = relationship;
        this.phone        = phone;
        this.email        = email;
        this.address      = address;
        this.birthday     = birthday;
        this.notes        = notes;
        this.photoPath    = photoPath;
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public int       getId()           { return id; }
    public String    getFirstName()    { return firstName; }
    public String    getLastName()     { return lastName; }
    public String    getFullName()     { return firstName + " " + lastName; }
    public String    getRelationship() { return relationship; }
    public String    getPhone()        { return phone; }
    public String    getEmail()        { return email; }
    public String    getAddress()      { return address; }
    public LocalDate getBirthday()     { return birthday; }
    public String    getNotes()        { return notes; }
    public String    getPhotoPath()    { return photoPath; }

    // ── Setters ──────────────────────────────────────────────────────────────
    public void setId(int id)                     { this.id = id; }
    public void setFirstName(String firstName)    { this.firstName = firstName; }
    public void setLastName(String lastName)      { this.lastName = lastName; }
    public void setRelationship(String r)         { this.relationship = r; }
    public void setPhone(String phone)            { this.phone = phone; }
    public void setEmail(String email)            { this.email = email; }
    public void setAddress(String address)        { this.address = address; }
    public void setBirthday(LocalDate birthday)   { this.birthday = birthday; }
    public void setNotes(String notes)            { this.notes = notes; }
    public void setPhotoPath(String photoPath)    { this.photoPath = photoPath; }

    @Override
    public String toString() { return getFullName(); }
}
