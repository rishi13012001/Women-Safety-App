package com.example.womensafetyapp;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;

class User implements Serializable {
    private String Id;
    private String name;
    private String email;
    private String mobile;
    private boolean isLoggedIn=false;
    private ArrayList<Contact>  Contacts=new ArrayList();

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public ArrayList<Contact> getContacts() {
        return Contacts;
    }

    public void setContacts(ArrayList<Contact> contacts) {
        Contacts = contacts;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }
    public void removeContact(int position){
        Contacts.remove(position);
    }
}
class Contact{

    private String Id;
    private String name;
    private String mobile;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

}