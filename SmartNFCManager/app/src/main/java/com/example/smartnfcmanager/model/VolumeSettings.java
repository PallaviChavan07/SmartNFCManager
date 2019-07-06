package com.example.smartnfcmanager.model;

import java.io.Serializable;

public class VolumeSettings implements Serializable {
    private int volumeSettingMod;
    private String volumeSettingType;

    public VolumeSettings(int volumeSettingMod, String volumeSettingType) {
        this.volumeSettingMod = volumeSettingMod;
        this.volumeSettingType = volumeSettingType;
    }

    public VolumeSettings() {
    }

    public int getVolumeSettingMod() {
        return volumeSettingMod;
    }

    public void setVolumeSettingMod(int volumeSettingMod) {
        this.volumeSettingMod = volumeSettingMod;

    }

    public String getVolumeSettingType() {
        return volumeSettingType;
    }

    public void setVolumeSettingType(String volumeSettingType) {
        this.volumeSettingType = volumeSettingType;
    }
}
