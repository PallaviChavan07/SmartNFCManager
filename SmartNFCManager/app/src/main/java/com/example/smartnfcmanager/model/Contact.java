package com.example.smartnfcmanager.model;

import java.io.Serializable;

public class Contact implements Serializable {
    private String mFirstName;
    private String mLastName;
    private String mEmailId;
    private String mPhoneNumber;


    public Contact() {
    }

    public Contact(String firstName, String lastName, String emailId, String phoneNumber ) {
        this.mFirstName = firstName;
        this.mLastName = lastName;
        this.mPhoneNumber = phoneNumber;
        this.mEmailId = emailId;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String firstName) {
        this.mFirstName = firstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public void setLastName(String lastName) {
        this.mLastName = lastName;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.mPhoneNumber = phoneNumber;
    }

    public String getEmailId() {
        return mEmailId;
    }

    public void setEmailId(String emailId) {
        this.mEmailId = emailId;
    }
}

