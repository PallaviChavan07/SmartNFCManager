package com.example.smartnfcmanager.model;

import android.content.Context;

import java.io.Serializable;

public class WifiNetwork implements Serializable {

    private String ssid;
    private String password;
    private Context context;

    public WifiNetwork(String ssid, String password) {
        this.ssid = ssid;
        this.password = password;
    }
    public WifiNetwork(String ssid, String password, Context context) {
        this.ssid = ssid;
        this.password = password;
        this.context = context;
    }

    public WifiNetwork() {
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Context getContext() {
        return context;
    }
}
