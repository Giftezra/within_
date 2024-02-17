package com.example.within.helpers;

public class TwilioKeys {
    private static final String ACCOUNT_SID = "AC7d0ab45a836794efe53e63daa5ed897c";
    private static final String ACCOUNT_AUTH_TOKEN = "c209fd8d1719d248133dafb73ad36c09";

    public TwilioKeys (){

    }

    public static String getAccountAuthToken() {
        return ACCOUNT_AUTH_TOKEN;
    }

    public static String getAccountSid() {
        return ACCOUNT_SID;
    }
}
