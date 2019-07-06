package com.example.smartnfcmanager;

import android.Manifest;
import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import static com.example.smartnfcmanager.util.Constants.PERMISSION_REQUEST_WRITE_CONTACT;

//import android.app.Application.Context.PackageManager;

public class ExecuteAddContactTask extends AppCompatActivity {
private String firstName;
private String lastName;
private String email;
private String phoneNumber;
private Context context;
private Activity activity;
private PackageManager packageManager;
String TAG = getClass().getSimpleName();

    public ExecuteAddContactTask() {
    }

    public ExecuteAddContactTask(String firstName, String lastName, String email, String phoneNumber, Context context, Activity activity, PackageManager packageManager) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.context = context;
        this.activity = activity;
        this.packageManager = packageManager;

    }

    /**
     * Check for user permission to write contact details in the device,
     * if it is granted, then add received contatc details to the device
     * **/
    public boolean executeAddContactTask() {
        Log.i( TAG,"In executeAddContactTask() "  );
        boolean addContactTask = false;
        if(context != null){
            Log.i(TAG,"context is not null "+ context);
        }if(context.getPackageManager() != null){
            Log.i(TAG,"context.getPackageManager() is not null "+ context.getPackageManager());
        }if(activity != null){
            Log.i(TAG,"activity is not null "+ activity);
        }
        if (ContextCompat.checkSelfPermission( activity, Manifest.permission.WRITE_CONTACTS) != activity.getPackageManager().PERMISSION_GRANTED) {
            activity.requestPermissions(new String[]{Manifest.permission.WRITE_CONTACTS}, PERMISSION_REQUEST_WRITE_CONTACT);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        }
        else{
            ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(  );
            operations.add( ContentProviderOperation.newInsert(
                    ContactsContract.RawContacts.CONTENT_URI)
                    .withValue( ContactsContract.RawContacts.ACCOUNT_TYPE, null )
                    .withValue( ContactsContract.RawContacts.ACCOUNT_NAME, null )
                    .build() );

            if(firstName != null ){
                operations.add( ContentProviderOperation.newInsert(
                        ContactsContract.Data.CONTENT_URI )
                        .withValueBackReference( ContactsContract.Data.RAW_CONTACT_ID,0 )
                        .withValue( ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE )
                        .withValue( ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, lastName )
                        .withValue( ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, firstName )
//                        .withValue(StructuredName.GIVEN_NAME, "Second Name")
//                        .withValue(StructuredName.FAMILY_NAME, "First Name")
                        .build() );
                Log.i( TAG,"In executeAddContactTask() =====> added firstName & lastName = "+firstName+" "+lastName );
            }

            if(email != null){
                operations.add( ContentProviderOperation.newInsert(
                        ContactsContract.Data.CONTENT_URI )
                        .withValueBackReference( ContactsContract.Data.RAW_CONTACT_ID,0 )
                        .withValue( ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE )
                        .withValue( ContactsContract.CommonDataKinds.Email.ADDRESS, email )
                        .withValue( ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK )
                        .build() );
                Log.i( TAG,"In executeAddContactTask() =====> added email = "+email  );
            }
            if(phoneNumber != null){
                operations.add( ContentProviderOperation.newInsert(
                        ContactsContract.Data.CONTENT_URI )
                        .withValueBackReference( ContactsContract.Data.RAW_CONTACT_ID,0 )
                        .withValue( ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE )
                        .withValue( ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber )
                        .withValue( ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE )
                        .build() );
                Log.i( TAG,"In executeAddContactTask() =====> added phoneNumber = "+phoneNumber );
            }
            try {
                context.getContentResolver().applyBatch( ContactsContract.AUTHORITY, operations);
                addContactTask = true;
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (OperationApplicationException e) {
                e.printStackTrace();
            }

            Log.i( TAG,"In executeAddContactTask() =====> addContactTask Status flag = "+addContactTask  );

        }

        return addContactTask;
    }


    /**
     * After receiving permission from user, call executeAddContactTask() function
     * **/
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_WRITE_CONTACT) {
            if (grantResults[0] == context.getPackageManager().PERMISSION_GRANTED) {
                // Permission is granted
                executeAddContactTask();
            } else {
                Toast.makeText(this, "Until you grant the permission, we canot add a contact", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
