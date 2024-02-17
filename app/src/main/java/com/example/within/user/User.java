package com.example.within.user;

/** This is the user object which represents every an individual on the app. this object will have different attributes synonymous with a specifiec user*/
public class User {
    /* The user fields include (first name, last name, email, phone)*/
    private String first_name, last_name, email, phone_number, currency;
    private double balance;
    private Object date_created;
    // Constructor with arguments to initialize the user data fields
    public User(String first_name, String last_name,
                String email, String phone_number,
                double credit_balance){
        this.last_name = last_name;
        this.first_name = first_name;
        this.email = email;
        this.phone_number = phone_number;
        this.balance = credit_balance;
    }

    public User (String currency){
        this.currency = currency;
    }

    public User (){

    }

    // Getter (Accessors) for the user fields
    public String getFirst_name() {
        return first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public double getBalance() {
        return balance;
    }

    public Object getDate_created() {
        return date_created;
    }

    public void setDate_created(Object date_created) {
        this.date_created = date_created;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
