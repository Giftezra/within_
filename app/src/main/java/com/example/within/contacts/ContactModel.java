package com.example.within.contacts;

/**
 * The contact model class models the look and feel of the users contact list when displayed.
 * it implements methods and object to signify the contact name and number
 * it implements method to initialize the first letter of the contact name using it for the contact icon
 * */
public class ContactModel{
    private final String name; // Create a name string with a final for the contact name
    private final String phoneNumber;
    private String pricing; // Create phone number string for the contact phoneNumber
    private boolean isExpanded = false;

    /*
     * Constructor for the model contains two param for the name and phone number
     * of the contact being modeled
     * */
    public ContactModel(String name, String phoneNumber, boolean expanded) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.isExpanded = expanded;
    }


    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    // Method to get the initial letter of the contact's name
    public String getInitialLetter() {
        if (name != null && !name.isEmpty()) {
            return String.valueOf(name.charAt(0)).toUpperCase();
        } else {
            return "#";
        }
    }

}
