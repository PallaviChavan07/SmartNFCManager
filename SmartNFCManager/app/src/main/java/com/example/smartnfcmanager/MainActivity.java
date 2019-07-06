package com.example.smartnfcmanager;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.RequiresPermission;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartnfcmanager.model.Contact;
import com.example.smartnfcmanager.model.VolumeSettings;
import com.example.smartnfcmanager.model.WifiNetwork;
import com.example.smartnfcmanager.util.Utils;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.example.smartnfcmanager.util.Constants.ADMIN_USER;
import static com.example.smartnfcmanager.util.Constants.COMPANY_TAG;
import static com.example.smartnfcmanager.util.Constants.CONTACT_FEATURE;
import static com.example.smartnfcmanager.util.Constants.CONTACT_STRING_PATTERN;
import static com.example.smartnfcmanager.util.Constants.NFC_FEATURE;
import static com.example.smartnfcmanager.util.Constants.READ_TAB_POS;
import static com.example.smartnfcmanager.util.Constants.VOLUME_SETTING_FEATURE;
import static com.example.smartnfcmanager.util.Constants.VOLUME_SETTING_STRING_PATTERN;
import static com.example.smartnfcmanager.util.Constants.WIFI_FEATURE;


public class MainActivity extends AppCompatActivity {
    public static final String MIME_TEXT_PLAIN = "text/plain";
    NfcAdapter mNFCAdapter;
    NFCCommunicator mNFCCom;
    private WifiNetwork wifiNetwork;
    private VolumeSettings volumeSettings;
    private Contact contact;
    private static final String MAIN_TAG = "MainActivity";
    private ViewPager mviewPager;
    private TextView readTextview;
    private String writeMsgString;
    static boolean isWrite = false;
    static boolean writeStatus = false;
    private boolean readStatus = false;
    static boolean isAdminUser;
    private WriteFragment writeFragment;
    private int currentTabPosition;
    private ExecuteVolumeSettingTask executeVolumeSettingTask;
    private ExecuteAddContactTask executeAddContactTask;
    private ReadFragment readFragment;
    private WifiManager mWifiMgr;
    private AtomicBoolean mHandleWiFiUpdates;
    private BroadcastReceiverWiFi mBroadcastReceiverWiFi;
    private IntentFilter mIntentFilterWiFi;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        writeFragment = new WriteFragment();
        mNFCAdapter = NfcAdapter.getDefaultAdapter(this);
        /*Check if device has NFC and it is enabled or not*/
        checkNFC();
        mNFCCom = new NFCCommunicator(this, mNFCAdapter);

        /*****************START WIFI CODE BLOCK***********************/
        // default value is false.
        mHandleWiFiUpdates = new AtomicBoolean();
        mWifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        checkWiFi();
        /*
         * Register an event handler and required Wifi state change IntentFilter
         * to receive wifi network status messages
         */
        mIntentFilterWiFi = new IntentFilter();
        mIntentFilterWiFi.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mIntentFilterWiFi.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

        /*****************END WIFI CODE BLOCK***********************/


        //Set up the viewPager with the sections adapter
        mviewPager = (ViewPager) findViewById( R.id.container );
        setupViewPager( mviewPager );
        TabLayout tabLayout = (TabLayout) findViewById( R.id.tabs );
        tabLayout.setupWithViewPager( mviewPager );
        //readFragment  = (ReadFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.container + ":" + 0);

        tabLayout.addOnTabSelectedListener( new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTabPosition = mviewPager.getCurrentItem();
                Log.i(MAIN_TAG, "tab position = "+currentTabPosition);
                /**If current tab is read tab then set visibility of read text message and make write message invisible**/
                if(currentTabPosition == READ_TAB_POS){
                    readStatus = true;
                    writeFragment  = (WriteFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.container + ":" + mviewPager.getCurrentItem());
                    writeFragment.setWriteMsgTextviewInVisibility();
                    if(readFragment != null)
                        readFragment.setReadFragmentTextandImage();
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        } );

        // Get Intent from write fragment
        /**Check intent data type and accordingly put that object into bundle**/
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            Serializable intentData = extras.getSerializable( NFC_FEATURE );
            Log.i(getClass().getSimpleName(), "isAdminUser =======> "+isAdminUser);
            if(intentData != null && intentData instanceof WifiNetwork){
                wifiNetwork = (WifiNetwork) intentData;
                writeMsgString = Utils.getNFCWiFiMessage( wifiNetwork.getSsid(), wifiNetwork.getPassword() );//wifiNetwork.getSsid()+";"+wifiNetwork.getPassword();
                Bundle args = new Bundle();
                args.putSerializable( NFC_FEATURE, wifiNetwork );
                writeFragment.setArguments(args);
            }
            else if(intentData != null && intentData instanceof VolumeSettings){
                volumeSettings = (VolumeSettings)intentData;
                writeMsgString = Utils.getNFCSilenceMessage( volumeSettings.getVolumeSettingMod() );
                Bundle args = new Bundle();
                args.putSerializable( NFC_FEATURE, volumeSettings );

                writeFragment.setArguments(args);
            }else if(intentData != null && intentData instanceof Contact){
                contact = (Contact)intentData;
                writeMsgString = Utils.getNFCContactMessage(contact.getFirstName(), contact.getLastName(), contact.getEmailId(), contact.getPhoneNumber());
                Toast.makeText( this, "writeMsgString = "+writeMsgString, Toast.LENGTH_SHORT );
                Log.i(MAIN_TAG, "writeMsgString in contact if ====>"+writeMsgString);
                Bundle args = new Bundle();
                args.putSerializable( NFC_FEATURE, contact );
                writeFragment.setArguments(args);
            }
        }

        Log.e(MAIN_TAG, "onCreate End");
    }

    /**Set viewpage with adapter**/
    private void setupViewPager(ViewPager viewPager){
        SectionPageAdapter adapter = new SectionPageAdapter( getSupportFragmentManager() );
        adapter.addFragment( new WriteFragment(), "WRITE" );
        adapter.addFragment( new ReadFragment(), "READ" );
        viewPager.setAdapter( adapter );
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.i(MAIN_TAG, "onResume() Started");
        /****REMOVE COMMENT LATER****/
        checkNFC();
        Log.e(MAIN_TAG, "onResume NFC Check Pass");

        checkWiFi();
        Log.e(MAIN_TAG, "onResume WiFI Check Pass");


        Intent nfcIntent = new Intent(this, getClass());
        // need to setup a Intent with Flag SINGLE_TOP
        nfcIntent.addFlags( Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, nfcIntent, 0);
        // Create intent filters we want this App to handle. For now we are only
        // handling ACTION_NDEF_DISCOVERED
        IntentFilter[] intentFiltersArray = new IntentFilter[1];
        /*
         * Notice that this is the same filter as in our manifest. create an
         * IntentFilter and configure the Action, Category and data fields of
         * the intent filter as in our AndroidManifest.xml
         */
        intentFiltersArray[0] = new IntentFilter();
        intentFiltersArray[0].addAction( NfcAdapter.ACTION_NDEF_DISCOVERED);
        intentFiltersArray[0].addCategory( Intent.CATEGORY_DEFAULT);

        /* ONly handling plain/text for now */
        try {
            intentFiltersArray[0].addDataType(MIME_TEXT_PLAIN);
        }catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
            Log.e(MAIN_TAG, "setupForegroundDispatch - MalformedMimeTypeException");
            e.printStackTrace();
        }

        String[][] techList = new String[][] { { android.nfc.tech.Ndef.class.getName() }, { android.nfc.tech.NdefFormatable.class.getName() } };

        mNFCAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techList);

        Log.i(MAIN_TAG, "onResume getIntent()==>" + getIntent().toString());


        /*
         * The Intent received can either come from OnNewIntent() or Android
         * waking up the activity due to user selecting app from list of
         * current applications. In the case of user selecting the app from
         * list of applications, Android starts the activity straight into
         * OnResume with the old Intent. If we process this Intent, we would
         * process an old intent that has old ready been processed More over
         * the intent if it is NFC Intent, we will try to process an NFC
         * intent when no actual NFC tag is near by causing failure while
         * reading or writing to tag. To prevent it we check the flags to
         * see if the FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY is true That means
         * Android is waking the app with old Intent. So do not process that
         * NFC intent.
         */
        int flags = getIntent().getFlags();
        if ((flags & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
            // this is an already processed intent
        }
        else{
            if ( mNFCCom.handleIntent(getIntent()) == true) {

                // if read tab selects then only perform the action
                if(readStatus){

                    // We must have a message. get the message
                    ArrayList<String> nfcMsgList = mNFCCom.getNfcTagMessage();
                    Log.i(MAIN_TAG, "onResume nfcMsgList " + nfcMsgList.toString());
                    /**Get the message from NFC tag and check if it is same as we saved by checking company tag,
                     * by checking NFC feature mode combination**/
                    String[] messageValues = nfcMsgList.get(0).split( ";" );
                    Log.i(MAIN_TAG, "messageValues[0].equals( COMPANY_TAG ) " + messageValues[0].equals( COMPANY_TAG ));
                    /**if 1st string matches with company tag (SPT) then only proceed further to read and execute task**/
                    if (messageValues[0].equals( COMPANY_TAG )){
                        readFragment = (ReadFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.container + ":" + mviewPager.getCurrentItem());
                        if(readFragment == null){
                            readFragment = (ReadFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.container + ":" + 0);
                        }
                        if(readFragment != null)
                            isAdminUser = readFragment.isAdminUser();
                        Log.i(MAIN_TAG, "isAdminUser=========> " + isAdminUser);
                        Log.i(MAIN_TAG, "messageValues[0] " + messageValues[0]);
                        String feature = messageValues[1];

                        /**Check which read from tag**/
                        switch (Integer.parseInt(  feature )){

                            case VOLUME_SETTING_FEATURE:
                                /**If admin user checkbox selected, then show data entry screen of volumeSetting feature
                                 * else execute volumeSetting task**/
                                volumeSettings = Utils.getVolumeSettingsObj( Integer.parseInt( messageValues[2] ));
                                if(isAdminUser){
                                    Intent intent = new Intent(this, WriteSilenceActivity.class);
                                    //to persist previously entered volume setting information
                                    Bundle extras = new Bundle();
                                    extras.putBoolean(ADMIN_USER,isAdminUser);
                                    extras.putSerializable(NFC_FEATURE,volumeSettings);
                                    intent.putExtras(extras);
                                    startActivity(intent);
                                }
                                else{
                                    executeVolumeSettingTask = new ExecuteVolumeSettingTask( Integer.parseInt( messageValues[2]), getApplicationContext(), getBaseContext(), this );
                                    Log.i( MAIN_TAG,"In switch case of SILENCE_FEATURE "+VOLUME_SETTING_FEATURE  );
                                    boolean volumeSettingStatus  = executeVolumeSettingTask.executeVolumeSettingTask();
                                    if(volumeSettingStatus){
                                        Log.i(MAIN_TAG, "In switch case After execution of task =======> volumeSettingStatus = " + volumeSettingStatus);
                                        if(readFragment!= null)
                                            readFragment.showExecutionTaskSuccess(VOLUME_SETTING_FEATURE,  volumeSettings);
                                    }
                                }
                                break;
                            case CONTACT_FEATURE:
                                /**If admin user checkbox selected, then show data entry screen of Add Contact feature
                                 * else execute Add Contact task**/
                                contact = new Contact( messageValues[2],messageValues[3], messageValues[4], messageValues[5]);
                                if(isAdminUser){

                                    Intent intent = new Intent(this, WriteContactActivity.class);
                                    //to persist previously entered contact data
                                    Bundle extras = new Bundle();
                                    extras.putBoolean(ADMIN_USER,isAdminUser);
                                    extras.putSerializable(NFC_FEATURE,contact);
                                    intent.putExtras(extras);
                                    startActivity(intent);
                                }
                                else{
                                    Log.i( MAIN_TAG,"In switch case of CONTACT_FEATURE "+CONTACT_FEATURE  );
                                    executeAddContactTask = new ExecuteAddContactTask( messageValues[2],messageValues[3], messageValues[4], messageValues[5], getApplicationContext(), this, getPackageManager() );
                                    boolean addContactStatus = executeAddContactTask.executeAddContactTask();
                                    if(addContactStatus){
                                        Log.i(MAIN_TAG, "In switch case After execution of task =======> addContactStatus = " + addContactStatus+ "readFragment = "+readFragment);
                                        if(readFragment!= null)

                                            readFragment.showExecutionTaskSuccess(CONTACT_FEATURE, contact);
                                    }
                                }

                                break;
                            case WIFI_FEATURE:
                                /**If admin user checkbox selected, then show data entry screen of Connect WiFi feature
                                 * else execute Connect WiFi task**/
                                wifiNetwork = new WifiNetwork( messageValues[2],messageValues[3],this);
                                if(isAdminUser){

                                    Intent intent = new Intent(this, WriteWifiActivity.class);
                                    //to persist previously entered Wifi information
                                    Bundle extras = new Bundle();
                                    extras.putBoolean(ADMIN_USER,isAdminUser);
                                    extras.putSerializable(NFC_FEATURE,wifiNetwork);
                                    intent.putExtras(extras);
                                    startActivity(intent);
                                }else{

                                    mBroadcastReceiverWiFi = new BroadcastReceiverWiFi();
                                    Log.i( MAIN_TAG,"In switch case of WIFI_FEATURE "+WIFI_FEATURE  );
                                    registerReceiver(mBroadcastReceiverWiFi, mIntentFilterWiFi);
                                    WiFiConnectAsyncTask wifiTask = new WiFiConnectAsyncTask(wifiNetwork);
                                    wifiTask.execute(wifiNetwork);
                                }

                                break;
                        }
                    }

                }

                Log.i(MAIN_TAG, "Is write ===> "+ isWrite);
                /**If isWrite is true then only perform write operation.**/
                if (isWrite){
                    writeFragment  = (WriteFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.container + ":" + mviewPager.getCurrentItem());
                    if (writeMsgString != null && writeMsgString.length() != 0) {
                        Log.i(MAIN_TAG, "onResume Write message is not empty. Write to NFC  ");
                        writeStatus = mNFCCom.writeMsgToTag(writeMsgString);
                        Log.i(MAIN_TAG, "onResume Write message status. " + String.valueOf(writeStatus));
                        Log.i(MAIN_TAG, "onResume Write message status. writeStatus " + writeStatus);
                        /**If write operation is successful then dismiss the dialogbox which was showing instruction to tap on NFC tag
                         * and display write success message**/
                        if (writeStatus){
                            writeFragment.dismissWriteDialog();
                            writeFragment.showWriteSuccess();



                        }
                    }
                }


            }
        }
    }




    @Override
    protected void onPause() {
        super.onPause();
        /**REMOVE COMMENT LATER**/
        //mNFCAdapter.disableForegroundDispatch(this);
        try {
            /**REMOVE COMMENT LATER**/
            //if (mBroadcastReceiverWiFi != null)
            // unregisterReceiver(mBroadcastReceiverWiFi);
        } catch (IllegalArgumentException e) {
            Log.e(MAIN_TAG,
                    "onPause(): unregisterReceiver has already been called");
            e.printStackTrace();
        }
    }

    /*Check if device has NFC and it is enabled or not*/
    private void checkNFC() {
        if (mNFCAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, R.string.no_nfc_support, Toast.LENGTH_LONG).show();
            finish();
        }

        if (mNFCAdapter.isEnabled() == false) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, R.string.nfc_not_enabled, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void checkWiFi() {

        /*
         * make sure the Wifi service is enabled
         */
        if (mWifiMgr.isWifiEnabled() == false) {
            Log.e(MAIN_TAG, "checkWiFi: WiFi service is not enabled");
            Toast.makeText(this, R.string.wifi_not_enabled, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        /*
         * This method gets called, when a new Intent gets associated with the
         * current activity instance. Instead of creating a new activity,
         * onNewIntent will be called. In our case this method gets called, when
         * the  NFC tag is detected by Android. We set the intent here with the
         * NFC information. The order of Activity State function call is
         * onNewIntent() -> onPause() -> onResume().
         */
        Log.i(MAIN_TAG, "onNewIntent" + intent.toString());

        setIntent(intent);
        super.onNewIntent(intent);

    }

//    public String createNfcWiFiMessage(final String ssid, final String key) {
//
//        return String.format("%s;%s;%s;%s", "SPT", "2", ssid, key);
//    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent startMain = new Intent( Intent.ACTION_MAIN);
        startMain.addCategory( Intent.CATEGORY_HOME);
        startMain.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }




    /**************************************WIFI CODE BLOCK START ***********************************************/

    private class WiFiConnectAsyncTask extends AsyncTask<Object, String, Boolean> {

        public static final String WIFI_MODULE_TAG = "WiFiConnectAsyncTask";
        WifiNetwork mWifiNWInfo;
        boolean mWifiConfigStatus = false;

        WiFiConnectAsyncTask(WifiNetwork wifiNWInfo){
            this.mWifiNWInfo = wifiNWInfo;
        }


        @Override
        protected Boolean doInBackground(Object... objects) {
            Log.i(WIFI_MODULE_TAG, "in doInBackground() objects =====> "+objects.length);
            if(objects[0] instanceof WifiNetwork){
                Log.i(WIFI_MODULE_TAG, "in doInBackground() objects has WifiNetwork type obj "+objects[0].toString());
            }else{
                Log.i(WIFI_MODULE_TAG, "in doInBackground() object is not of WifiNetwork type "+objects[0].getClass());
            }
            /**REMOVE COMMENT LATER**/
             mWifiNWInfo = (WifiNetwork) objects[0];
            /**CHANGE**/
            if (mWifiNWInfo == null) {
                Log.i(WIFI_MODULE_TAG, "doInBackground():" + R.string.no_wifi_connect_details);
                publishProgress(mWifiNWInfo.getContext().getString(R.string.no_wifi_connect_details));

                return false;
            }

            publishProgress(mWifiNWInfo.getContext().getString(R.string.wifi_connect_start));

            // confirm device can find the Wi-Fi SSID NFC gave
            /*
            if (scanWiFiNetworks() == false) {
                return false;
            }
            */

            //create and add or update the wifiConfig to the android WiFi network list
            WifiConfiguration wifiConfig = generateWifiNWConfig();
            if (mWifiConfigStatus == false) {

                Log.i(WIFI_MODULE_TAG, "doInBackground():" + R.string.wifi_network_list_update_failed);
                publishProgress(mWifiNWInfo.getContext().getString(R.string.wifi_network_list_update_failed));

                return false;
            }

            //Check if we are already connect to the NFC provided SSID
            if (mWifiMgr.getConnectionInfo().getSSID().equalsIgnoreCase(wifiConfig.SSID)) {
                Log.i(WIFI_MODULE_TAG, "doInBackground():" + R.string.wifi_connection_success);
                publishProgress(mWifiNWInfo.getContext().getString(R.string.wifi_connection_success));

                return true;
            }

            //Indicate can process Wifi connect information
            mHandleWiFiUpdates.set(true);

            // We are not connetced to NFC stated Wi-FI network so initiate the connection request
            mWifiMgr.enableNetwork(wifiConfig.networkId, true);

            Log.i(WIFI_MODULE_TAG, "doInBackground():" + R.string.wifi_network_enable_success + " " + wifiConfig.SSID.toString());
            publishProgress(mWifiNWInfo.getContext().getString(R.string.wifi_network_enable_success) +" "+ wifiConfig.SSID.toString());
            return true;

        }

        @Override
        protected void onProgressUpdate(String... text) {
            //mNFCReadMsgTxtView.append(text[0] + "\r\n");
            readFragment.setReadText( text[0] + "\r\n" );


        }

        private boolean scanWiFiNetworks() {
            Log.i(WIFI_MODULE_TAG, "scanWiFiNetworks() Beginnings");

            // confirm device can find the Wi-Fi SSID NFC gave
            List<ScanResult> scanResultList = mWifiMgr.getScanResults();
            Log.i(WIFI_MODULE_TAG, "scanWiFiNetworks()" + scanResultList.toString());

            boolean wifiNwFound = false;
            for (ScanResult result : scanResultList) {
                Log.i(WIFI_MODULE_TAG, "scanWiFiNetworks()" + result.SSID.toString());
                if (result.SSID.equals(mWifiNWInfo.getSsid())) {
                    wifiNwFound = true;
                    break;
                }
            }

            if (wifiNwFound == false) {
                Log.i(WIFI_MODULE_TAG, "scanWiFiNetworks()" + mWifiNWInfo.getContext().getString(R.string.SSID_discovery_failed));
                publishProgress(mWifiNWInfo.getContext().getString(R.string.SSID_discovery_failed));
                return false;
            }

            return true;
        }

        private WifiConfiguration generateWifiNWConfig() {

            mWifiConfigStatus =  false;

            String ssid = mWifiNWInfo.getSsid();
            String key = mWifiNWInfo.getPassword();

            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.networkId = -1;

            wifiConfig.SSID = String.format("\"%s\"", ssid);
            wifiConfig.preSharedKey = String.format("\"%s\"", key);

            wifiConfig.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.CCMP);
            wifiConfig.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.TKIP);
            wifiConfig.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
            wifiConfig.allowedKeyManagement
                    .set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfig.status = WifiConfiguration.Status.ENABLED;

            /*
             * When enabling a network, the priority value matters as to which
             * network Android decides connect. if 2 networks have the same
             * value, it may select one the we do not want to connect. Therefore
             * find the highest priority value in the configured networks and
             * make our WifiConfigursation have a higher number
             */
            int highestPriority = 0;
            List<WifiConfiguration> wifiConfigList = mWifiMgr.getConfiguredNetworks();
            //Log.i(WIFI_MODULE_TAG, "WifiConfiguration()" + wifiConfigList.toString());

            for (WifiConfiguration config : wifiConfigList) {

                //Log.i(WIFI_MODULE_TAG, "WifiConfiguration()" + config.toString() + "\r\n");

                if (config.SSID.equals(String.format("\"%s\"", ssid)) == false) {

                    //Log.i(WIFI_MODULE_TAG, "WifiConfiguration() FALSE SSID " + config.SSID.toString() + " NFC SSID " +  String.format("\"%s\"", ssid));
                    if (config.priority >= highestPriority) {
                        highestPriority = config.priority + 1;
                    }
                }
                else {
                    //Log.i(WIFI_MODULE_TAG, "WifiConfiguration() TRUE SSID " + config.SSID.toString() + " NFC SSID " +  String.format("\"%s\"", ssid));
                    //Log.i(WIFI_MODULE_TAG, "WifiConfiguration() found " + String.valueOf(config.networkId) + "\r\n");
                    //There is already a network config exists with the same ssid
                    wifiConfig.networkId = config.networkId;
                }
            }

            wifiConfig.priority = highestPriority;

            // if we found a wifi config with the same ssid then update that wifi config
            // If we found a config with the NFC supplied ssid then wifiConfig.networkId will not be -1
            // if wifiConfig.networkId is -1 means there are no configs with the ssid in the android network list

            if (wifiConfig.networkId != -1) {
                // Andoid WiFi notwork list does have the NFC supplied ssid therefore update Wifi config
                // in the list
                if (mWifiMgr.updateNetwork(wifiConfig) == -1) {
                    Log.e(WIFI_MODULE_TAG,
                            "WifiConfiguration(): failed to update existing WifiConfig for SSID: "
                                    + wifiConfig.SSID + "network ID" + wifiConfig.networkId);

                    mWifiConfigStatus =  false;
                }
                else {
                    mWifiConfigStatus = true;
                }
            }
            else {
                // add the new config to the configuration list

                if (mWifiMgr.addNetwork(wifiConfig) == -1) {
                    Log.i(WIFI_MODULE_TAG,
                            "WifiConfiguration(): failed to add config " + wifiConfig.toString());
                    mWifiConfigStatus =  false;
                }
                else {
                    mWifiConfigStatus = true;
                }
            }

            Log.d(WIFI_MODULE_TAG,
                    "generateWifiNWConfig(): New Config\r\n-------------" + wifiConfig.toString() + "----------\r\n");



            return wifiConfig;
        }



    }




    private class BroadcastReceiverWiFi extends BroadcastReceiver {
        private static final String BR_MODULE_TAG = "BroadcastReceiverWiFi";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(BR_MODULE_TAG, "mHandleWiFiUpdates.get() = " + String.valueOf(mHandleWiFiUpdates.get()) + " Got Intent " + intent.toString());

            if (mHandleWiFiUpdates.get() == false) {

                return;
            }

            //WifiInfo wifiNwInfo = (WifiInfo) intent.getParcelableExtra(WifiManager.EXTRA_WIFI_STATE);
            WifiInfo wifiInfo = mWifiMgr.getConnectionInfo();


            if (wifiInfo == null) {
                return;

            }

            //updateStatus("Successfully connected to " + wifiInfo.getSSID());
            Log.d(BR_MODULE_TAG, wifiInfo.toString());
            SupplicantState state = wifiInfo.getSupplicantState();

            switch (state) {

                case ASSOCIATED:
                    updateStatus(getString(R.string.wifi_state_ASSOCIATED), false);
                    break;
                case ASSOCIATING:
                    updateStatus(getString(R.string.wifi_state_ASSOCIATING), false);
                    break;
                case AUTHENTICATING:
                    updateStatus(getString(R.string.wifi_state_Authenticating), false);
                    break;
                case COMPLETED:
                    updateStatus(getString(R.string.wifi_state_COMPLETED), false);
                    break;
                case DISCONNECTED:
                    updateStatus(getString(R.string.wifi_state_Disconnected), false);
                    break;
                case DORMANT:
                    updateStatus(getString(R.string.wifi_state_DORMANT), false);
                    break;
                case FOUR_WAY_HANDSHAKE:
                    updateStatus(getString(R.string.wifi_state_FOUR_WAY_HANDSHAKE), false);
                    break;
                case GROUP_HANDSHAKE:
                    updateStatus(getString(R.string.wifi_state_GROUP_HANDSHAKE), false);
                    break;
                case INACTIVE:
                    updateStatus(getString(R.string.wifi_state_INACTIVE), false);
                    break;
                case INTERFACE_DISABLED:
                    updateStatus(getString(R.string.wifi_state_INTERFACE_DISABLED), false);
                    break;
                case INVALID:
                    updateStatus(getString(R.string.wifi_state_INVALID), false);
                    break;
                case SCANNING:
                    updateStatus(getString(R.string.wifi_state_SCANNING), false);
                    break;
                case UNINITIALIZED:
                    updateStatus(getString(R.string.wifi_state_UNINITIALIZED), false);
                    break;
                default:
                    updateStatus(getString(R.string.wifi_state_Unknown), false);
                    break;

            }

            if (intent.getAction().equals( WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (networkInfo == null) {
                    return;
                }
                /**CHANGE REMOVE COMMENT LATER**/
                //networkInfo.getExtraInfo();

                Log.i(BR_MODULE_TAG, "---networkInfo--- " + networkInfo.toString());
                /**REMOVE COMMENT LATER**/


                String extraStr = "";
                String detailStateStr = "";
                if(networkInfo.getExtraInfo() != null){
                    Log.i(BR_MODULE_TAG, "---getExtraInfo--- " + networkInfo.getExtraInfo().toString());
                    extraStr = networkInfo.getExtraInfo().toString();
                }else{

                    Log.i(BR_MODULE_TAG, "---getExtraInfo--- is null " + networkInfo.getExtraInfo());
                }
                if(networkInfo.getDetailedState() != null){
                    Log.i(BR_MODULE_TAG, "---getDetailedState--- " + networkInfo.getDetailedState().toString());
                    detailStateStr = networkInfo.getDetailedState().toString();
                }else{
                    Log.i(BR_MODULE_TAG, "---getDetailedState---is null " + networkInfo.getDetailedState());
                }

                String statusStr = extraStr + " " + detailStateStr + "\r\n";
                Log.i(BR_MODULE_TAG, "---getDetailedInfo--- " + statusStr);
                updateStatus(statusStr, false);

                //if (extraStr.equalsIgnoreCase(String.format("\"%s\"", wifiNetwork.getSsid())) && detailStateStr.equalsIgnoreCase("CONNECTED")) {
                if(detailStateStr.equalsIgnoreCase("CONNECTED")){
                    statusStr = "---Final--- Success";
                    Log.i(BR_MODULE_TAG,  statusStr);
                    updateStatus(statusStr, true);
                    mHandleWiFiUpdates.set(false);
                }
            }
        }
    }

    private void updateStatus(String text, boolean isDone) {
        readFragment.setReadText( text + "\r\n" );

        if (isDone == true) {
            readFragment.showExecutionTaskSuccess( WIFI_FEATURE, wifiNetwork );
            //readFragment.setReadText(getString(R.string.wifi_connection_success));
        }
// else{
//            readFragment.showExecutionTaskFailure( WIFI_FEATURE, wifiNetwork );
//        }
    }




/**************************************WIFI CODE BLOCK END ***********************************************/





}
