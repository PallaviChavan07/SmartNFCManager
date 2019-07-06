package com.example.smartnfcmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.smartnfcmanager.model.VolumeSettings;

import java.io.Serializable;

import static com.example.smartnfcmanager.util.Constants.ADMIN_USER;
import static com.example.smartnfcmanager.util.Constants.NFC_FEATURE;
import static com.example.smartnfcmanager.util.Constants.RINGER_MOD;
import static com.example.smartnfcmanager.util.Constants.RINGER_MOD_STR;
import static com.example.smartnfcmanager.util.Constants.SILENCE_MOD;
import static com.example.smartnfcmanager.util.Constants.SILENCE_MOD_STR;

public class WriteSilenceActivity extends AppCompatActivity {

    private VolumeSettings volumeSettings;

    String APP_TAG = getClass().getSimpleName();
    //NfcAdapter mNFCAdapter;
    RadioGroup mRadioGroup;
    Button mWriteConfirmButton;
    Button mWriteCancelButton;
    Button mWriteClearButton;
    String settingType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_write_silence );
        setTitle(R.string.title_write_volumesetting);
        Log.i(APP_TAG, "onCreate method Started");

        mRadioGroup = (RadioGroup) findViewById(R.id.settingsRadioGroup);
        mWriteConfirmButton = (Button) findViewById(R.id.settingsConfirmBtn);
        mWriteCancelButton = (Button)findViewById(R.id.settingsCancelBtn);
        mWriteClearButton = (Button)findViewById(R.id.settingsClearBtn);
        mWriteClearButton.setVisibility( View.INVISIBLE );
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int radioButtonId = radioGroup.getCheckedRadioButtonId();

                RadioButton radioButton = findViewById(radioButtonId);
                if(radioButton != null){
                    settingType = radioButton.getText().toString();
                    int settingTYpeId = radioButton.getId();
                }

            }
        });

        /** If block to maintain initially entered data **/
        Bundle extras = getIntent().getExtras();
        Serializable intentData = extras.getSerializable( NFC_FEATURE );
        final boolean isAdminUser = extras.getBoolean( ADMIN_USER );

        if(intentData != null && intentData instanceof VolumeSettings) {
            mWriteClearButton.setVisibility( View.VISIBLE );
            volumeSettings = (VolumeSettings) intentData;
            if(volumeSettings.getVolumeSettingMod() == SILENCE_MOD)
                mRadioGroup.check( R.id.silenceRadioButton );
            else if(volumeSettings.getVolumeSettingMod() == RINGER_MOD){
                mRadioGroup.check( R.id.ringerRadioButton );
            }
        }

        /**
         * On Write Confirm, get details on Silence phone data entry screen, set it to object and send object to MainActivity
         * With these details, "Write to tag" text will get enabled.
         * **/
        mWriteConfirmButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //check if one of the radiobutton is clicked
                if (mRadioGroup.getCheckedRadioButtonId() == -1)
                {
                    // no radio buttons are checked
                    Toast.makeText( WriteSilenceActivity.this, R.string.volumeRadiogrp_empty_message, Toast.LENGTH_SHORT ).show();
                }else{
                    volumeSettings = new VolumeSettings();
                    volumeSettings.setVolumeSettingType( settingType );
                    //Log.i( "WriteSilenceActivity", "settingType.equals( RINGER_MOD_STR ) "+settingType.equals( RINGER_MOD_STR )+" ==>  " );
                    if(settingType.equals( SILENCE_MOD_STR  )){
                        volumeSettings.setVolumeSettingMod( SILENCE_MOD );

                    }else if(settingType.equals( RINGER_MOD_STR )){
                        volumeSettings.setVolumeSettingMod( RINGER_MOD );
                    }
                    //Toast.makeText( WriteSilenceActivity.this, "setVolumeSettingMod = "+volumeSettings.getVolumeSettingMod(), Toast.LENGTH_SHORT ).show();

                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    Bundle extras = new Bundle(  );
                    extras.putSerializable( NFC_FEATURE, volumeSettings );
                    extras.putBoolean( ADMIN_USER, isAdminUser );
                    intent.putExtras( extras );
                    startActivity( intent );
                }



            }
        } );

        /**
         * On Clear Button Clear all fields on data entry screen
         * **/
        mWriteClearButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRadioGroup.clearCheck();
            }
        } );

        /**
         * On Cancel Button go to Main Activity
         * **/
        mWriteCancelButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i=new Intent(WriteSilenceActivity.this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity( i );

            }
        } );

    }
/**
 * On backPressed go to Main Activity
 * **/
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i=new Intent(this,MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity( i );
    }

}

