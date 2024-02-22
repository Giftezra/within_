package com.example.within.calllog_activities.phone_recent;

public class PhoneRecentCallModel {
    private String reciepientName;
    private String reciepientNumber;
    private String callDuration;
    private final String callType;
    private int intialLetter;
    private boolean isExpanded = true;
    private final String recentDate;


    public PhoneRecentCallModel(String reciepientName, String reciepientNumber,
                                int intialLetter, String callDuration, boolean isExpanded, String callType, String date) {
        this.reciepientName = reciepientName;
        this.reciepientNumber = reciepientNumber;
        this.intialLetter = intialLetter;
        this.callDuration = callDuration;
        this.callType = callType;
        this.isExpanded = isExpanded;
        this.recentDate =  date;
    }

    public String getReciepientName() {
        return reciepientName;
    }

    public void setReciepientName(String reciepientName) {
        this.reciepientName = reciepientName;
    }

    public String getReciepientNumber() {
        return reciepientNumber;
    }

    public void setReciepientNumber(String reciepientNumber) {
        this.reciepientNumber = reciepientNumber;
    }

    public int getIntialLetter() {
        return intialLetter;
    }

    public void setIntialLetter(int intialLetter) {
        this.intialLetter = intialLetter;
    }

    public String getCallDuration() {
        return callDuration;
    }

    public void setCallDuration(String callDuration) {
        this.callDuration = callDuration;
    }

    public String getRecentDate() {
        return recentDate;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public String getCallType() {
        return callType;
    }

    public boolean isExpanded() {
        return isExpanded;
    }


    @Override
    public String toString() {
        return "PhoneRecentCallModel{" +
                "reciepientName='" + reciepientName + '\'' +
                ", reciepientNumber='" + reciepientNumber + '\'' +
                ", callDuration='" + callDuration + '\'' +
                ", callType='" + callType + '\'' +
                ", intialLetter=" + intialLetter +
                ", isExpanded=" + isExpanded +
                ", recentDate='" + recentDate + '\'' +
                '}';
    }
}
