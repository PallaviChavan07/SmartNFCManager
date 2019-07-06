package com.example.smartnfcmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.smartnfcmanager.model.WifiNetwork;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

import static com.example.smartnfcmanager.util.Constants.ADMIN_USER;
import static com.example.smartnfcmanager.util.Constants.NFC_FEATURE;


public class WriteWifiActivity extends AppCompatActivity {

    private WifiNetwork wifiNetwork;
    public static final String APP_TAG = "WriteWifiActivity";
    EditText mSsidEditTxt;
    EditText mPasswordEditTxt;
    Button mWriteConfirmButton;
    Button mWriteCancelButton;
    Button mWriteClearButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_write_wifi );
        setTitle(R.string.title_write_wifi);
        Log.i(APP_TAG, "onCreate method Started");

        mSsidEditTxt = (EditText) findViewById(R.id.edttxtSsid);
        mPasswordEditTxt = (EditText)findViewById(R.id.edttxtPassword);
        mWriteConfirmButton = (Button) findViewById(R.id.confirmButton);
        mWriteCancelButton = (Button)findViewById(R.id.cancelButton);
        mWriteClearButton = (Button)findViewById(R.id.wifiClearButton);
        mWriteClearButton.setVisibility( View.INVISIBLE );
        //Serializable intentData = getIntent().getSerializableExtra(NFC_FEATURE);
        Bundle extras = getIntent().getExtras();
        Serializable intentData = extras.getSerializable( NFC_FEATURE );
        final boolean isAdminUser = extras.getBoolean( ADMIN_USER );
        /** if block to show previously entered data **/
        if(intentData != null && intentData instanceof WifiNetwork) {
            wifiNetwork = (WifiNetwork) intentData;
            mSsidEditTxt.setText( wifiNetwork.getSsid() );
            mPasswordEditTxt.setText( wifiNetwork.getPassword() );
            mWriteClearButton.setVisibility( View.VISIBLE );
        }

        /**
         * On Write Confirm, get details on Connect WiFi data entry screen, set it to object and send object to MainActivity
         * With these details, "Write to tag" text will get enabled.
         * **/
            mWriteConfirmButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(StringUtils.isEmpty( mSsidEditTxt.getText().toString() )){
                    Toast.makeText( getApplicationContext(), getApplicationContext().getString( R.string.ssidEmptyMsg ), Toast.LENGTH_SHORT ).show();
                }else if(StringUtils.isEmpty( mPasswordEditTxt.getText().toString() )){
                    Toast.makeText( getApplicationContext(), getApplicationContext().getString( R.string.passwordEmptyMsg ), Toast.LENGTH_SHORT ).show();
                }else{
                    wifiNetwork = new WifiNetwork();
                    wifiNetwork.setSsid(mSsidEditTxt.getText().toString());
                    wifiNetwork.setPassword( mPasswordEditTxt.getText().toString() );

                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
//                        intent.putExtra(NFC_FEATURE, wifiNetwork);
//                        startActivity(intent);
                    Bundle extras = new Bundle(  );
                    extras.putSerializable( NFC_FEATURE, wifiNetwork );
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
                    mSsidEditTxt.getText().clear();
                    mPasswordEditTxt.getText().clear();
                }
            } );

        /**
         * On Cancel Button go to Main Activity
         * **/
            mWriteCancelButton.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i=new Intent(WriteWifiActivity.this, MainActivity.class);
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
        //i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity( i );

    }

}
