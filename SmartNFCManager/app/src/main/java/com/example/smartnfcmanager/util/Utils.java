package com.example.smartnfcmanager.util;

import com.example.smartnfcmanager.model.Contact;
import com.example.smartnfcmanager.model.VolumeSettings;
import com.example.smartnfcmanager.model.WifiNetwork;

import static com.example.smartnfcmanager.util.Constants.COMPANY_TAG;
import static com.example.smartnfcmanager.util.Constants.CONTACT_FEATURE;
import static com.example.smartnfcmanager.util.Constants.CONTACT_FEATURE_STR;
import static com.example.smartnfcmanager.util.Constants.CONTACT_STRING_PATTERN;
import static com.example.smartnfcmanager.util.Constants.RINGER_MOD;
import static com.example.smartnfcmanager.util.Constants.RINGER_MOD_STR;
import static com.example.smartnfcmanager.util.Constants.SILENCE_MOD;
import static com.example.smartnfcmanager.util.Constants.SILENCE_MOD_STR;
import static com.example.smartnfcmanager.util.Constants.VOLUME_SETTING_FEATURE;
import static com.example.smartnfcmanager.util.Constants.VOLUME_SETTING_FEATURE_STR;
import static com.example.smartnfcmanager.util.Constants.VOLUME_SETTING_STRING_PATTERN;
import static com.example.smartnfcmanager.util.Constants.WIFI_FEATURE;
import static com.example.smartnfcmanager.util.Constants.WIFI_FEATURE_STR;
import static com.example.smartnfcmanager.util.Constants.WIFI_STRING_PATTERN;

public class Utils {

    /*can be used in writeSilenceActivity*/
    public static VolumeSettings getVolumeSettingsObj(String settingType){
        VolumeSettings volumeSettings = new VolumeSettings();
        if(settingType.equals( SILENCE_MOD_STR  )){
            volumeSettings.setVolumeSettingMod( SILENCE_MOD );
        }else if(settingType.equals( RINGER_MOD_STR )){
            volumeSettings.setVolumeSettingMod( RINGER_MOD );
        }
        volumeSettings.setVolumeSettingType( settingType );
        return  volumeSettings;
    }
    public static VolumeSettings getVolumeSettingsObj(int mod){
        VolumeSettings volumeSettings = new VolumeSettings();
        if(mod == SILENCE_MOD){
            volumeSettings.setVolumeSettingType( SILENCE_MOD_STR );
        }else if(mod == RINGER_MOD){
            volumeSettings.setVolumeSettingType( RINGER_MOD_STR );
        }
        volumeSettings.setVolumeSettingMod( mod );
        return  volumeSettings;
    }
    public static String getNFCSilenceMessage(final int mode){
        return String.format( VOLUME_SETTING_STRING_PATTERN, COMPANY_TAG, VOLUME_SETTING_FEATURE , mode);
    }
    public static String getNFCContactMessage(final String firstNm, final String lastNm, final String email, final String phone) {
        return String.format( CONTACT_STRING_PATTERN, COMPANY_TAG, CONTACT_FEATURE , firstNm, lastNm, email,phone);
    }
    public static String getNFCWiFiMessage(String ssid, String password) {
        return String.format( WIFI_STRING_PATTERN, COMPANY_TAG, WIFI_FEATURE , ssid, password);
    }
    public static String getFeatureFromNFCWriteMessage(String writeNFCMessage){
        if(writeNFCMessage != null && writeNFCMessage.length() > 0){
            String[] messageValues = writeNFCMessage.split( ";" );

            if(messageValues != null && messageValues.length>0){
                if(Integer.parseInt( messageValues[1] ) == VOLUME_SETTING_FEATURE)
                    return VOLUME_SETTING_FEATURE_STR;
                else if (Integer.parseInt( messageValues[1] ) == CONTACT_FEATURE)
                    return CONTACT_FEATURE_STR;
                else if (Integer.parseInt( messageValues[1] ) == WIFI_FEATURE)
                    return WIFI_FEATURE_STR;
            }
        }else
            return null;

        return  null;
    }


}
