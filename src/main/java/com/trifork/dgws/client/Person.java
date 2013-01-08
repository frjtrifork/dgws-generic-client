package com.trifork.dgws.client;

/**
 *
 */
public class Person {
    private String firstName;
    private String lastName;
    private String email;
    private String cpr;
    private String cvr;

    public Person(String firstName, String lastName, String email, String cpr) {
        super();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.cpr = cpr;
    }

    public String toString() {
        return firstName + " " + lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getCpr() {
        return cpr;
    }
}
