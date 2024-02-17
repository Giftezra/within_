package com.example.within.calllog_activities.app_recent;

public class RecentModel {
    private String reciepientName;
    private String reciepientNumber;
    private String callDuration;
    private final String callType;
    private int intialLetter;
    private boolean isExpanded = true;

    public RecentModel(String reciepientName, String reciepientNumber,
                       int intialLetter, String callDuration, boolean isExpanded, String callType) {
        this.reciepientName = reciepientName;
        this.reciepientNumber = reciepientNumber;
        this.intialLetter = intialLetter;
        this.callDuration = callDuration;
        this.callType = callType;
        this.isExpanded = isExpanded;
    }

    public RecentModel(String reciepientName, String reciepientNumber,String callDuration, boolean isExpanded, String callType) {
        this.reciepientName = reciepientName;
        this.reciepientNumber = reciepientNumber;
        this.callDuration = callDuration;
        this.callType = callType;
        this.isExpanded = isExpanded;
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

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public String getCallType() {
        return callType;
    }

    public boolean isExpanded() {
        return isExpanded;
    }
}
