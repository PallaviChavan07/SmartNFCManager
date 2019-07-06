package com.example.smartnfcmanager;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import static com.example.smartnfcmanager.util.Constants.RINGER_MOD;
import static com.example.smartnfcmanager.util.Constants.SILENCE_MOD;

public class ExecuteVolumeSettingTask extends AppCompatActivity {
    private int volumeSettingMod;
    private Context context;
    private Context baseContext;
    String TAG = getClass().getSimpleName();
    private int ON_DO_NOT_DISTURB_CALLBACK_CODE = 1;
    Activity activity;

    public ExecuteVolumeSettingTask(int volumeSettingMod, Context context, Context baseContext, Activity activity) {
        this.volumeSettingMod = volumeSettingMod;
        this.context = context;
        this.baseContext = baseContext;
        this.activity = activity;

    }

    /**
     * Check for user permission to modify volume settings in the device,
     * if it is granted, then modify volume settings in the device as we received from NFC tag
     * **/
    public boolean executeVolumeSettingTask() {

        boolean volumeSettingTask = false;
        NotificationManager notificationManager =
                (NotificationManager) this.context.getSystemService( Context.NOTIFICATION_SERVICE);
        if (!notificationManager.isNotificationPolicyAccessGranted()) {

            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            activity.startActivityForResult(intent,ON_DO_NOT_DISTURB_CALLBACK_CODE);
        }
        else{
            AudioManager audioMngr;
            audioMngr= (AudioManager) this.baseContext.getSystemService( Context.AUDIO_SERVICE);
            if (this.volumeSettingMod == SILENCE_MOD) {
                try {
                    audioMngr.setRingerMode( AudioManager.RINGER_MODE_SILENT);
                    audioMngr.setRingerMode( AudioManager.ADJUST_MUTE);
                }catch (Exception e){
                    e.printStackTrace();
                    Log.e(TAG, "EXCEPTION in executeVolumeSettingTask ===> ");
                }

                volumeSettingTask = true;
                //RingerMode = normal;
            } else if (this.volumeSettingMod == RINGER_MOD) {
                audioMngr.setRingerMode( AudioManager.RINGER_MODE_NORMAL);
                audioMngr.setRingerMode( AudioManager.ADJUST_UNMUTE);
                volumeSettingTask = true;

            }
        }

        return volumeSettingTask;


    }

    /**
     * After receiving permission from user, call executeVolumeSettingTask() function
     * **/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == ON_DO_NOT_DISTURB_CALLBACK_CODE) {
            //this.requestDoNotDisturbPermission();
            executeVolumeSettingTask();
        }
    }


}


