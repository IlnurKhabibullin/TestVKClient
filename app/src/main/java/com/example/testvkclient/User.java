package com.example.testvkclient;

import org.joda.time.DateTime;

/**
 * Created by Ильнур on 16.06.2015.
 */

public class User {

    private final String name;
    private final DateTime birthDate;
    private final String dateFormat;

    public User(String name, DateTime birthDate, String dateFormat) {
        this.name = name;
        this.birthDate = birthDate;
        this.dateFormat = dateFormat;
    }

    public DateTime getBirthDate() {
        return birthDate;
    }

    public String getName() {
        return name;
    }

    public String getDateFormat() {
        return dateFormat;
    }
}
