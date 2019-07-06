package com.example.smartnfcmanager;



import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.smartnfcmanager.model.Contact;
import com.example.smartnfcmanager.model.VolumeSettings;
import com.example.smartnfcmanager.model.WifiNetwork;
import com.example.smartnfcmanager.util.Utils;


import java.io.Serializable;

import static com.example.smartnfcmanager.util.Constants.ADMIN_USER;
import static com.example.smartnfcmanager.util.Constants.NFC_FEATURE;

public class WriteFragment extends Fragment {
    private WifiNetwork wifiNetwork;
    private VolumeSettings volumeSettings;
    private Contact contact;
    private ImageView wifiImgView;
    private ImageView setttingsImgView;
    private ImageView contactImgView;
    private TextView writeMsgTextview;
    private static final String WRITE_FRAGMENT_TAG = "WriteFragment";
    private WriteDialogFragment writeDialogFragment;
    private String writeNFCMessage;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate( R.layout.fragment_write, container, false );
        wifiImgView =(ImageView) view.findViewById( R.id.wifiImgButton );
        writeMsgTextview = (TextView) view.findViewById( R.id.writeMsgTxtView );
        setttingsImgView = (ImageView) view.findViewById( R.id.settingsImgButton );
        contactImgView = (ImageView) view.findViewById( R.id.contactImgButton );


        setWriteMsgTextviewInVisibility();

        /**
         * Check if intent data is not null then get it's corresponding object value
         * and set text "write to tag" with it's visibility
         * **/
        Bundle extras = getActivity().getIntent().getExtras();
        if(extras != null){
            Serializable intentData = extras.getSerializable( NFC_FEATURE );
            if(intentData != null){

                writeMsgTextview.setVisibility( View.VISIBLE );

                if(intentData instanceof WifiNetwork){
                    wifiNetwork = (WifiNetwork) intentData;
                    writeMsgTextview.setText(R.string.write_tag_message);

                    writeNFCMessage = Utils.getNFCWiFiMessage(wifiNetwork.getSsid(), wifiNetwork.getPassword());
                }else if(intentData instanceof VolumeSettings){
                    volumeSettings = (VolumeSettings) intentData;
                    writeMsgTextview.setText(R.string.write_tag_message);
                    writeNFCMessage = Utils.getNFCSilenceMessage( volumeSettings.getVolumeSettingMod() );

                }else if(intentData instanceof Contact){
                    contact = (Contact) intentData;
                    writeMsgTextview.setText(R.string.write_tag_message);
                    writeNFCMessage = Utils.getNFCContactMessage(contact.getFirstName(), contact.getLastName(), contact.getEmailId(), contact.getPhoneNumber() );

                }
            }


        }
        /**
         * Show data entry screen of WiFi activity
         * **/
        wifiImgView.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //start WriteWifi activity
                Intent intent = new Intent(getActivity(), WriteWifiActivity.class);
                //to persist previously entered wifi data
                Bundle extras = new Bundle();
                //extras.putBoolean(ADMIN_USER,adminUserCheckbx.isChecked());
                extras.putSerializable(NFC_FEATURE,wifiNetwork);
                intent.putExtras(extras);
                startActivity(intent);
            }
        } );

        /**
         * On click of writeMsgTextView, call showWriteDialog method
         * **/
        writeMsgTextview.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showWriteDialog(true, false);
            }
        } );

        /**
         * Show data entry screen of Volume Setting Activity
         * **/
        setttingsImgView.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //start WriteSilence activity
                Intent intent = new Intent(getActivity(), WriteSilenceActivity.class);
                //to persist previously entered volumesetting data
                Bundle extras = new Bundle();
                extras.putSerializable(NFC_FEATURE,volumeSettings);
                intent.putExtras(extras);
                startActivity(intent);
            }
        } );

        /**
         * Show data entry screen of Add Contact Activity
         * **/
        contactImgView.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //start WriteContactActivity activity
                Intent intent = new Intent(getActivity(), WriteContactActivity.class);
                //to persist previously entered contact data
                Bundle extras = new Bundle();
               // extras.putBoolean(ADMIN_USER,adminUserCheckbx.isChecked());
                extras.putSerializable(NFC_FEATURE,contact);
                intent.putExtras(extras);
                startActivity(intent);
            }
        } );

        return view;
    }


    /**
     * Show Dialog box with instruction - "Please tap on the NFC Tag" that will wait for user to tap on the tag
     * **/
    public void showWriteDialog(boolean isWrite, boolean writeStatus) {

        MainActivity.isWrite = true;
        Log.i( WRITE_FRAGMENT_TAG, "writeStatus ======> " + writeStatus );

        WriteDialogFragment writeDialogFragment = new WriteDialogFragment();
        writeDialogFragment.show( getActivity().getFragmentManager(), String.valueOf( R.string.write_progress_dialog ) );

    }



    /** Show write success message on completion of writing to tag**/
    public void showWriteSuccess(){

        Log.i(WRITE_FRAGMENT_TAG, "In showWriteSuccess()========> Not in MainActivity");
        String feature = Utils.getFeatureFromNFCWriteMessage( writeNFCMessage );
        writeMsgTextview.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.check_48, 0, 0, 0);
        writeMsgTextview.setCompoundDrawablePadding( 25 );
        writeMsgTextview.setText( "Tag updated with " + feature + " Successfully!" );
        writeMsgTextview.setVisibility( View.VISIBLE );
        Log.i(WRITE_FRAGMENT_TAG, "In showWriteSuccess()========> should display success message");
        MainActivity.isWrite = false;
    }

    /**Dismiss write progress dialog**/
    public void dismissWriteDialog(){
        Log.i(WRITE_FRAGMENT_TAG, "In dismissDialog()()==========> Not In MainActivity");
        writeDialogFragment = (WriteDialogFragment) getActivity().getFragmentManager().findFragmentByTag( String.valueOf( R.string.write_progress_dialog ) );
        if(writeDialogFragment != null){
            writeDialogFragment.dismiss();
        }
        MainActivity.isWrite = false;
        MainActivity.writeStatus = false;

    }

    @Override
    public void onPause() {
        super.onPause();
        setWriteMsgTextviewInVisibility();
    }

    /**
     * make writeMsgTextview invisible
     * **/
    public void setWriteMsgTextviewInVisibility(){
        writeMsgTextview.setVisibility( View.INVISIBLE );
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(WRITE_FRAGMENT_TAG, "OnResume() started");
    }

}
