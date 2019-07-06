package com.example.smartnfcmanager;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.smartnfcmanager.model.Contact;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

import static com.example.smartnfcmanager.util.Constants.ADMIN_USER;
import static com.example.smartnfcmanager.util.Constants.NFC_FEATURE;

public class WriteContactActivity extends AppCompatActivity {
    private Contact contact;
    String APP_TAG = "WriteContactActivity";
    EditText mFirstNameEdtTxt;
    EditText mLastNameEdtTxt;
    EditText mEmailEdtTxt;
    EditText mPhoneNumberEdtTxt;
    Button mWriteConfirmButton;
    Button mWriteCancelButton;
    Button mWriteClearButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_write_contact );
        setTitle(R.string.title_write_contact);
        Log.i(APP_TAG, "onCreate method Started");

        mFirstNameEdtTxt = (EditText) findViewById(R.id.txtFirstName);
        mLastNameEdtTxt = (EditText) findViewById(R.id.txtLastName);
        mEmailEdtTxt = (EditText) findViewById(R.id.txtEmail);
        mPhoneNumberEdtTxt = (EditText) findViewById(R.id.txtPhone);
        mWriteConfirmButton = (Button) findViewById(R.id.contactConfirmBtn);
        mWriteCancelButton = (Button)findViewById(R.id.contactCancelBtn);
        mWriteClearButton = (Button)findViewById(R.id.contactClearBtn);
        /*Keep clear button invisible. Make it visible only if we have data*/
        if(mWriteClearButton != null)
            mWriteClearButton.setVisibility( View.INVISIBLE );

        /** If block to maintain initially entered data **/
        //Serializable intentData = getIntent().getSerializableExtra(NFC_FEATURE);
        Bundle extras = getIntent().getExtras();
        Serializable intentData = extras.getSerializable( NFC_FEATURE );
        final boolean isAdminUser = extras.getBoolean( ADMIN_USER );
        if(intentData != null && intentData instanceof Contact) {
            mWriteClearButton.setVisibility( View.VISIBLE );
            contact = (Contact) intentData;
            mFirstNameEdtTxt.setText( contact.getFirstName() );
            mLastNameEdtTxt.setText( contact.getLastName() );
            mEmailEdtTxt.setText( contact.getEmailId() );
            mPhoneNumberEdtTxt.setText( contact.getPhoneNumber().toString() );
        }

        /**
         * On Write Confirm, get details on Contact data entry screen and send those details to MainActivity
         * With these details, "Write to tag" text will get enabled.
         * **/
        mWriteConfirmButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(StringUtils.isEmpty( mFirstNameEdtTxt.getText().toString() )){
                    Toast.makeText( getApplicationContext(), getApplicationContext().getString( R.string.firstNameEmptyMsg ), Toast.LENGTH_SHORT ).show();
                }else if(StringUtils.isEmpty( mLastNameEdtTxt.getText().toString() )){
                    Toast.makeText( getApplicationContext(), getApplicationContext().getString( R.string.lastNameEmptyMsg ), Toast.LENGTH_SHORT ).show();
                }else if(StringUtils.isEmpty( mEmailEdtTxt.getText().toString() )){
                    Toast.makeText( getApplicationContext(), getApplicationContext().getString( R.string.emailEmptyMsg ), Toast.LENGTH_SHORT ).show();
                }else if(StringUtils.isEmpty( mPhoneNumberEdtTxt.getText().toString() )){
                    Toast.makeText( getApplicationContext(), getApplicationContext().getString( R.string.phoneEmptyMsg ), Toast.LENGTH_SHORT ).show();
                }else{
                    contact = new Contact();
                    contact.setFirstName( mFirstNameEdtTxt.getText().toString() );
                    contact.setLastName( mLastNameEdtTxt.getText().toString() );
                    contact.setEmailId( mEmailEdtTxt.getText().toString() );
                    contact.setPhoneNumber( mPhoneNumberEdtTxt.getText().toString() );

                    //Toast.makeText(getApplicationContext(), "contact object set with details n intent is starting", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    Bundle extras = new Bundle(  );
                    extras.putSerializable( NFC_FEATURE, contact );
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
                mFirstNameEdtTxt.getText().clear();
                mLastNameEdtTxt.getText().clear();
                mEmailEdtTxt.getText().clear();
                mPhoneNumberEdtTxt.getText().clear();
            }
        } );

        /**
         * On Cancel Button go to Main Activity
         * **/
        mWriteCancelButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i=new Intent(WriteContactActivity.this, MainActivity.class);
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
