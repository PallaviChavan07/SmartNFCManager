package com.example.smartnfcmanager;

import android.app.Activity;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.Ndef;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import static com.example.smartnfcmanager.util.Constants.MIME_TEXT_PLAIN;

public class NFCCommunicator {

   // public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String NFCCOM_TAG = "NFCCommunicator";

    NfcAdapter mNfcAdapter;
    Activity mActivity;
    Tag mNFCTag;
    ArrayList<String> mNFCDataList;
    String mNFCWriteMsg;


    public NFCCommunicator(Activity activity, NfcAdapter nfcAdaptor){
        mNfcAdapter = nfcAdaptor;
        mActivity = activity;
        mNFCTag = null;
        mNFCWriteMsg = "";
        mNFCDataList = new ArrayList<String>();

        Log.d(NFCCOM_TAG, "NFCCommunicator created");
    }

    public boolean setNFCWriteMsg(String msg) {
        mNFCWriteMsg = msg;
        return true;
    }

    public boolean handleIntent(Intent intent) {
        boolean status = isNfcIntent(intent);

        if (mNFCDataList.isEmpty() == false) {
            mNFCDataList.clear();
        }


        if (status == false) {
            Log.e(NFCCOM_TAG, "handleIntent: Not a NFC Intent");
            return status;
        }

        mNFCTag = intent.getParcelableExtra( NfcAdapter.EXTRA_TAG);
        if (mNFCTag == null) {
            Log.e(NFCCOM_TAG, "handleIntent: NFC Tag is NULL");
            return false;
        }

        // Retrieve the Ndef from Tag. Ndef contains the Ndef Message
        Ndef ndef = Ndef.get(mNFCTag);
        if (ndef == null) {
            Log.i(NFCCOM_TAG, "handleIntent:Ndef is NULL");
            return false;
        }

        //retrieve the Ndef message
        NdefMessage ndefMsg = ndef.getCachedNdefMessage();
        Log.i(NFCCOM_TAG, "handleIntent:NdefMessage is " + ndefMsg.toString());

        mNFCDataList = getPlainTextList(ndefMsg);
        Log.i(NFCCOM_TAG, "handleIntent:nfcDataList is " + mNFCDataList.toString());

        return status;
    }

    /*
     * This function verifies if the received action is a NDEF Discovered action
     */
    private boolean isNfcIntent(Intent intent) {

        String nfcAction = intent.getAction();
        String nfcType = intent.getType();

        /******** REMOVE LATER ****************/
        nfcType = new String( "text/plain" );

        Log.i(NFCCOM_TAG, "isNfcIntent: Action: " + nfcAction);
        Log.i(NFCCOM_TAG, "isNfcIntent: type: " + nfcType);

        //if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(nfcAction)
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(nfcAction) || NfcAdapter.ACTION_TECH_DISCOVERED.equals(nfcAction)
                && MIME_TEXT_PLAIN.equals(nfcType) ) {
            return true;
        } else {
            return false;
        }
    }

    public ArrayList<String> getNfcTagMessage() {
        return mNFCDataList;
    }

    /*
     * This function returns each payload of the NdefRecords as a string in the stringArray.
     */
    public ArrayList<String> getPlainTextList(NdefMessage nfcNDEFMsg) {
        ArrayList<String> txtList = new ArrayList<String>();
        if (nfcNDEFMsg == null || nfcNDEFMsg.getRecords().length == 0) {
            Log.e(NFCCOM_TAG, "getPlainTextList: Msg is NULL or Record count is 0");
            return txtList;
        }

        NdefRecord[] ndefRecordArray = nfcNDEFMsg.getRecords();
        for (NdefRecord record : ndefRecordArray) {
            String payLoad = getNdefRecordString(record);
            if (payLoad.isEmpty() == false) {
                txtList.add(payLoad);
            }
        }


        return txtList;
    }


    private String getNdefRecordString(NdefRecord ndefRecord) {


        String rtnString = "";

        // the 3 bit Type Name Format field is used to interpret the variable length Type field
        switch (ndefRecord.getTnf()) {
            case NdefRecord.TNF_WELL_KNOWN :
                try {
                    rtnString = getNdfRecordTnfWellKnowPayload(ndefRecord);
                } catch (UnsupportedEncodingException e) {
                    Log.e(NFCCOM_TAG, "getNdefRecordString UnsupportedEncodingException");
                    e.printStackTrace();
                }
                break;
        }

        return rtnString;
    }

    private String getNdfRecordTnfWellKnowPayload(NdefRecord ndefRecord) throws UnsupportedEncodingException {

        String rtnString = "";


        // There are several Well Known Record Type Definitions for NdefRecord's Type.
        //For now only handling RTD_TEXT
        byte[] ndefTypeRtd = ndefRecord.getType();

        if (Arrays.equals(ndefTypeRtd, NdefRecord.RTD_TEXT) == true) {
            /*To create a string out of the payload, need to know text encoding, language coed, actual data start location
             * These are defined in NFC forum specification for "Text Record Type Definition" at 3.2.1
             * bit_7 defines encoding bit_5..0 length of IANA language code
             * 1. need to identify Text Encoding It is at dec 128 or oct 200 or hex 80.
             * 2. need language code for eg en. It is at dec 63 or oct 077
             * 3. create a new string using the text encoding and language code
             */
            byte[] ndefPayload = ndefRecord.getPayload();
            String textEncodingStr = ((ndefPayload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

            int languageCodeLength = (ndefPayload[0] & 63);
            String languageCodeStr = new String(ndefPayload, 1, languageCodeLength, "US-ASCII");

            rtnString = new String(ndefPayload, languageCodeLength + 1, ndefPayload.length - languageCodeLength - 1, textEncodingStr);
            Log.e(NFCCOM_TAG, "getNdfRecordTnfWellKnowPayload encoding: " + textEncodingStr + " languagecodelength: "
                    + languageCodeLength + " languageCode: " + languageCodeStr + " Actual Data: " + rtnString);

        }

        return rtnString;
    }

    public static NdefMessage convertToNDFMsg(final String msg) {


        // Prepare Language Bytes
        byte[] langBytes = Locale.getDefault().getLanguage().getBytes( Charset.forName("US-ASCII"));

        // Character set encoding hard coded to UTF-8
        Charset utfEncoding = Charset.forName("UTF-8");

        //convert payload string
        byte[] textBytes = msg.getBytes(utfEncoding);

        int utfBit = 0;	//for UTF-8
        char status = (char) (utfBit + langBytes.length);
        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        data[0] = (byte) status;
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);

        NdefRecord nedfRecord = new NdefRecord( NdefRecord.TNF_WELL_KNOWN,
                NdefRecord.RTD_TEXT, new byte[0], data);

        // create NdefMesage with the above records
        NdefMessage rtnNedfMsg = new NdefMessage(nedfRecord);

        return rtnNedfMsg;

    }

    public boolean writeMsgToTag(String msg) {
        Log.i(NFCCOM_TAG, "Received msg = "+msg);

        boolean rtnStatus = false;

        NdefMessage ndefMsg = convertToNDFMsg(msg);

        if (mNFCTag == null || ndefMsg == null) {
            Log.e(NFCCOM_TAG, "writeMsgToTag:  Tag or NdefMessage null");
            return rtnStatus;
        }

        Ndef nfcNDEF = Ndef.get(mNFCTag);
        if (nfcNDEF == null) {
            Log.e(NFCCOM_TAG, "writeMsgToTag: Ndef null");
            return rtnStatus;
        }

        if (ndefMsg.getByteArrayLength() > nfcNDEF.getMaxSize()) {
            Log.e(NFCCOM_TAG,
                    String.format(
                            "writeMsgToTag: Message size of %d exceeds MaxTag size %d",
                            ndefMsg.getByteArrayLength(),
                            nfcNDEF.getMaxSize()));
            return rtnStatus;
        }

        if (nfcNDEF.isWritable() == false) {
            Log.e(NFCCOM_TAG, "writeMsgToTag: Tag not Writeable");
            return rtnStatus;
        }
        try {
            rtnStatus = true;
            nfcNDEF.connect();

            nfcNDEF.writeNdefMessage(ndefMsg);

        } catch (TagLostException e) {
            rtnStatus = false;
            mNFCTag = null; // forget the tag
            Log.i(NFCCOM_TAG,
                    "Write failed - TagLostException: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            rtnStatus = false;
            mNFCTag = null; // forget the tag
            Log.i(NFCCOM_TAG,
                    "Write failed - IOException: " + e.getMessage());
            e.printStackTrace();
        } catch (FormatException e) {
            rtnStatus = false;
            Log.i(NFCCOM_TAG,
                    "Write failed - FormatException: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (nfcNDEF != null) {
                    nfcNDEF.close();
                }
            } catch (IOException e) {
                Log.i(NFCCOM_TAG, "Exception while closing");
                e.printStackTrace();
            }

        }

        return rtnStatus;
    }


}
