package com.javaxpert.demos.trainings.cassandra.bloomfilters.bo;

import java.time.Instant;

/**
 * This business object contains data related to a person...
 * Persons may be stored in large datasets ....
 */
public class Person {
    private final String personalMail;
    private final String firstName;
    private final String lastName;
    private final Instant dateOfBirth;

    public Person(String personalMail, String firstName, String lastName, Instant dateOfBirth) {
        this.personalMail = personalMail;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
    }

    public String getFirstName() {

        return firstName;
    }



    public String getLastName() {
        return lastName;
    }


    public Instant getDateOfBirth() {
        return dateOfBirth;
    }


    public String getPersonalMail() {
        return personalMail;
    }

    @Override
    public String toString() {
        return "Person{" +
                "personalMail='" + personalMail + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                '}';
    }
}
