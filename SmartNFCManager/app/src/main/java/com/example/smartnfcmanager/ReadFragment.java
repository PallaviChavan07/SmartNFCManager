package com.example.smartnfcmanager;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.smartnfcmanager.model.Contact;
import com.example.smartnfcmanager.model.VolumeSettings;
import com.example.smartnfcmanager.model.WifiNetwork;

import static com.example.smartnfcmanager.util.Constants.ADMIN_USER;
import static com.example.smartnfcmanager.util.Constants.CONTACT_FEATURE;
import static com.example.smartnfcmanager.util.Constants.PACKAGE_NAME;
import static com.example.smartnfcmanager.util.Constants.RINGER_MOD;
import static com.example.smartnfcmanager.util.Constants.SILENCE_MOD;
import static com.example.smartnfcmanager.util.Constants.VOLUME_SETTING_FEATURE;
import static com.example.smartnfcmanager.util.Constants.WIFI_FEATURE;

public class ReadFragment extends Fragment {

    public static final String APP_TAG = "ReadFragment";
    private VolumeSettings volumeSettings;
    private Contact contact;
    private WifiNetwork wifiNetwork;
    private TextView readTextView;
    private ImageView readImageView;
    private CheckBox adminUserCheckbx;
    private boolean isAdminUser;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate( R.layout.fragment_read, container, false );
        readTextView = (TextView) view.findViewById( R.id.readTxtView );
        readImageView = (ImageView) view.findViewById( R.id.readImgView );
        adminUserCheckbx = (CheckBox) view.findViewById( R.id.adminCheckBox );
        //Check if admin user checkbox is checked and set that value to the bundle
        adminUserCheckbx.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAdminUser = adminUserCheckbx.isChecked();
            }
        } );

        return view;


    }


    /**
     * Set initial read tab message and imageview
     * **/
    public void setReadFragmentTextandImage(){
        if(readTextView != null){
            readTextView.setText( R.string.approach_text );
        }
        if(readImageView != null){
            int id = getResources().getIdentifier(PACKAGE_NAME+":drawable/nfc_tap_small" , null, PACKAGE_NAME);
            readImageView.setImageResource(id);
        }
    }

    /**
     Show executetask success message. Based on object type set success message and success smile image to the ImageView
     * **/
    public void showExecutionTaskSuccess(int feature,  Object object){
        /*set success smiley to result*/
        int id = getResources().getIdentifier(PACKAGE_NAME+":drawable/happy" , null, PACKAGE_NAME);
        readImageView.setImageResource(id);

        if(object instanceof VolumeSettings ){
            volumeSettings = (VolumeSettings)object;
            Log.i(APP_TAG, "object is instance of Volumezsettings");
            if(volumeSettings.getVolumeSettingMod() == SILENCE_MOD)
                readTextView.setText( "Phone has been muted successfully!" );
            else if(volumeSettings.getVolumeSettingMod() == RINGER_MOD)
                readTextView.setText( "Phone has been unmuted successfully!" );
        }else if(object instanceof  Contact){
            Log.i(APP_TAG, "object is instance of Contacts");
            contact = (Contact)object;
            Log.i(APP_TAG, contact.getFirstName()+" "+contact.getLastName()+" has been added successfully to your contacts!" );
            readTextView.setText( contact.getFirstName()+" "+contact.getLastName()+" has been added successfully to your contacts!" );
        }else if(object instanceof  WifiNetwork){
            Log.i(APP_TAG, "object is instance of WifiNetwork");
            wifiNetwork = (WifiNetwork)object;
            readTextView.setText( "Connected to WiFi "+wifiNetwork.getSsid()+" successfully!"  );

        }
    }

    public void showExecutionTaskFailure(int feature, Object object){
        /*set sad smiley to result*/
        int id = getResources().getIdentifier(PACKAGE_NAME+":drawable/sad" , null, PACKAGE_NAME);
        readImageView.setImageResource(id);
        readTextView.setText( "Could not execute the task" );
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(APP_TAG,"OnPause of readFragment get called.. ");
        //setReadFragmentTextandImage();
    }


    public boolean isAdminUser() {
        return isAdminUser;
    }
public void setReadText(String message){
    readTextView.setText( message );
}

}
