package com.aizaz.accessandroid;

import static android.content.Context.NOTIFICATION_SERVICE;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.aizaz.accessandroid.utilities.Constant;
import com.aizaz.accessandroid.utilities.PreferenceManager;

import java.sql.Date;
import java.util.ArrayList;

public class ControllingSMS extends BroadcastReceiver {
    private Context contexts;
    private String sendingNumber = "";
    private Boolean state;
    private AudioManager am;
    LocationTrack locationTrack;
    private PreferenceManager preferenceManager;

    /*The way to launch a BroadcastReceiver manually is by calling*/
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        contexts = context;
        // am=(AudioManager) contexts.getSystemService(Context.AUDIO_SERVICE);
        preferenceManager = new PreferenceManager(contexts.getApplicationContext());
        state = preferenceManager.getBoolen(Constant.KEY_STATE);
        String smsCode = preferenceManager.getString(Constant.KEY_GENERATE_CODE);
        int numberOfLength = smsCode.length();
        if (state.equals(true)) {
            am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            Bundle bundle = intent.getExtras();
            SmsMessage[] msgs = null;
            String smsBody = "";
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                Toast.makeText(context.getApplicationContext(), "sms triggered", Toast.LENGTH_SHORT).show();
                msgs = new SmsMessage[pdus.length];
                for (int i = 0; i < pdus.length; i++) {
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    sendingNumber += msgs[i].getOriginatingAddress();
                    Toast.makeText(context.getApplicationContext(), sendingNumber, Toast.LENGTH_SHORT).show();

                    smsBody = msgs[i].getMessageBody();
                }

                if ((smsBody != null) && (smsBody.length() != 0)) {
                    smsBody = smsBody.replaceAll("", "");
                    smsBody = smsBody.trim();
                    smsBody = smsBody.toLowerCase();
                    Toast.makeText(context, smsBody, Toast.LENGTH_SHORT).show();
                    int c = smsBody.length();
                    if (c <= numberOfLength+1 ) {
                        abortBroadcast();
                        /*Toast.makeText(context, "C less or equal to number of length.", Toast.LENGTH_SHORT).show();*/
                    } else {
                        String preCode = smsBody.substring(0, 4);
                        //System.out.println(preCode);

                        String code = smsBody.substring(5, 7);
                        System.out.println(code);
                        String action;
                        if (code.equals("Cc") || (code.equals("cc"))){
                            action = smsBody.substring(8,c);
                        }
                        else {
                            action = smsBody.substring(7, c);
                            System.out.println(action);
                        }
                        if (preCode.equals(smsCode)) {
                            if ((code.equals("Cc")) || (code.equals("cc")) ||
                                    (code.equals("Mm")) || (code.equals("mm")) ||
                                    (code.equals("Cl")) || (code.equals("cl")) ||
                                    (code.equals("Sm")) || (code.equals("sm")) ||
                                    (code.equals("Lo")) || (code.equals("lo"))) {

                                abortBroadcast();
                                setAction(code, action);
                            /*Toast.makeText(contexts, "action... " + action, Toast.LENGTH_LONG).show();

                            Toast.makeText(contexts, "code... " + code, Toast.LENGTH_LONG).show();*/
                            }
                        } else {
                            /*Toast.makeText(context, "Password does not match", Toast.LENGTH_SHORT).show();*/
                        }

                    }
                }

            }
        }
    }


//end of onReceive()

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setAction(String aCode, String aAction) {
        // for mode change
        Toast.makeText(contexts, aCode+":"+aAction, Toast.LENGTH_LONG).show();
        if (aCode.equals("mm") || (aCode.equals("Mm"))) {

            if (aAction.equals("s") || (aAction.equals("S"))) {
                try {
                    NotificationManager nt = (NotificationManager)contexts.getSystemService(NOTIFICATION_SERVICE) ;
                    if(!nt.isNotificationPolicyAccessGranted()) {
                        Intent i=new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        contexts.startActivity(i);
                    }

                    am.setRingerMode(AudioManager.RINGER_MODE_SILENT);

                }catch (Exception e)
                {
                    Toast.makeText(contexts, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
            if (aAction.equals("g") || (aAction.equals("G"))) {
                am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            }
            if (aAction.equals("v") || (aAction.equals("V"))) {
                am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                Toast.makeText(contexts, "Vibrate triggered", Toast.LENGTH_SHORT).show();
            }
        }

        // for contact searching
        if (aCode.equals("cc") || (aCode.equals("Cc"))) {
            // search particular contacts in contacts.
            if (aAction.length() >= 3) {
                String sendMessageBody = "";
                final String SELECTION = "((" +
                        ContactsContract.Contacts.DISPLAY_NAME + " NOTNULL) AND (" +
                        ContactsContract.Contacts.DISPLAY_NAME + " LIKE '" + aAction + "%' ))";

                /*Retrieve a list of contacts by matching the search string to any type of detail data,
                 including name, phone number, street address, email address*/

                Cursor cursor = contexts.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null, SELECTION, null, null);

                // int count =0;
                while (cursor.moveToNext()) {
                    @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    @SuppressLint("Range") String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    sendMessageBody += name + "\n" + phoneNumber + "\n";
                   /* count++;
                    if (count==3){

                    }*/

                }// end while loop
                cursor.close();
                /*Toast.makeText(contexts.getApplicationContext(), sendMessageBody, Toast.LENGTH_SHORT).show();*/

                if (sendMessageBody.length() >= 5) {
                    SmsManager ss = SmsManager.getDefault();
                    ArrayList<String> messagesss = ss.divideMessage(sendMessageBody);

                    ss.sendMultipartTextMessage(sendingNumber, null, messagesss, null, null);

                } else {
                    /*SmsManager : Manages SMS operations such as sending data*/
                    SmsManager ss = SmsManager.getDefault();
                    ss.sendTextMessage(sendingNumber, null, "Record Not Found", null, null);

                }
            }
        }

        // for call information
        if (aCode.equals("cl") || (aCode.equals("Cl"))) {
            // search call logs and send back
            if (aAction.equals("g") || (aAction.equals("G"))) {

                String SendCallLogs = "";
                if (ActivityCompat.checkSelfPermission(contexts, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                Cursor cursor = contexts.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                        null, null, null, CallLog.Calls.DATE + " DESC");

                int number = cursor.getColumnIndex(CallLog.Calls.NUMBER);
                int type = cursor.getColumnIndex(CallLog.Calls.TYPE);
                int date = cursor.getColumnIndex(CallLog.Calls.DATE);
                int duration = cursor.getColumnIndex(CallLog.Calls.DURATION);
                int countCallLogs = 0;
                String previousN = "1234567";
                while (cursor.moveToNext()) {
                    String phNumber = cursor.getString(number);
                    String callType = cursor.getString(type);
                    String callDate = cursor.getString(date);
                    Date callDayTime = new Date(Long.valueOf(callDate));
                    String callDuration = cursor.getString(duration);
                    String dir = null;
                    int dircode = Integer.parseInt(callType);
                    switch (dircode) {
                        case CallLog.Calls.OUTGOING_TYPE:
                            dir = "OUTGOING";
                            break;
                        case CallLog.Calls.INCOMING_TYPE:
                            dir = "INCOMING";
                            break;

                        case CallLog.Calls.MISSED_TYPE:
                            dir = "MISSED";
                            break;
                        case CallLog.Calls.REJECTED_TYPE:
                            dir = "Rejected";
                            break;
                    }
                    if (phNumber.equals(previousN)) {
                        previousN = phNumber;
                    } else {
                        SendCallLogs += ("Phone Number:--- " + phNumber + " \nCall Type:--- "
                                + dir + " \nCall Date:--- " + callDayTime
                                + " \nCall duration in sec :--- " + callDuration + "\n\n");
                        previousN = phNumber;

                        countCallLogs++;
                        if (countCallLogs == 5) {
                            SmsManager ss = SmsManager.getDefault();
                            if (SendCallLogs.length() >= 5) {
                                ArrayList<String> messages = ss.divideMessage(SendCallLogs);
                                ss.sendMultipartTextMessage(sendingNumber, null, messages, null, null);
                            } else {
                                ss.sendTextMessage(sendingNumber, null, "No call log found.", null, null);
                                /*Toast.makeText(contexts.getApplicationContext(), "No Call Log Found!!!!", Toast.LENGTH_SHORT).show();*/
                            }
                            /*Toast.makeText(contexts.getApplicationContext(), SendCallLogs, Toast.LENGTH_SHORT).show();*/
                        }
                    }
                }//end while loop
                cursor.close();


            }
        }
        //for message collections
        if (aCode.equals("Sm") || (aCode.equals("sm"))) {
            if (aAction.equals("S") || (aAction.equals("s"))) {

                Uri myMessage = Uri.parse("content://sms/inbox");  /*URI identifies a resource*/
                String SendMessageInfo = "";
                Cursor cursor = contexts.getContentResolver().query(myMessage,
                        new String[]{/*"_id",*/ "address", "date", "body"}, null,
                        null, null);
                int count = 0;
                String previousN = "1234567";
                while ((cursor != null && cursor.moveToNext())) {

                    String Number = cursor.getString(
                            cursor.getColumnIndexOrThrow("address"));
                    String Body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                    String Date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                    Date smsDayTime = new Date(Long.valueOf(Date));
                    if (Number.equals(previousN)) {

                        previousN = Number;
                    } else {
                        SendMessageInfo += "Mobile Number =" + Number + "\n" + "SMS Body =" + Body + "\n" + "SMS Date =" + smsDayTime + "\n\n";
                        previousN = Number;
                        count++;
                        if (count == 5) {
                            if (SendMessageInfo.length() >= 5) {
                                SmsManager ss = SmsManager.getDefault();
                                ArrayList<String> messages = ss.divideMessage(SendMessageInfo);
                              /*  PendingIntent piSend = PendingIntent.getBroadcast(contexts, 0, new Intent(SENT_SMS_ACTION_NAME), 0);
                                PendingIntent piDelivered = PendingIntent.getBroadcast(contexts, 0, new Intent(DELIVERED_SMS_ACTION_NAME), 0);

                                ArrayList<PendingIntent> sendList = new ArrayList<>();
                                sendList.add(piSend);

                                ArrayList<PendingIntent> deliverList = new ArrayList<>();
                                deliverList.add(piDelivered);*/
                                Toast.makeText(contexts, "deragha", Toast.LENGTH_LONG).show();
                                ss.sendMultipartTextMessage(sendingNumber, null, messages, null, null);

                            } else {
                                SmsManager ss = SmsManager.getDefault();
                                ss.sendTextMessage(sendingNumber, null, "Record Not Found", null, null);

                            }
                            /*Toast.makeText(contexts, "Number" + SendMessageInfo, Toast.LENGTH_LONG).show();*/
                        }
                    }
                }//end while loop
                cursor.close();
            }
        }
        if (aCode.equals("Lo") || (aCode.equals("lo"))) {

            if (aAction.equals("C") || aAction.equals("c")) {
                locationTrack = new LocationTrack(contexts);
                String SendLocationInfo = "";
                SmsManager ss = SmsManager.getDefault();
                if (locationTrack.canGetLocation()) {
                    double longitude = locationTrack.getLongitude();
                    double latitude = locationTrack.getLatitude();
                    SendLocationInfo += "https://google.com/maps/search/?api=1&query=" + latitude + "%2C" + longitude;

                    ss.sendTextMessage(sendingNumber, null, SendLocationInfo, null, null);

                    /*Toast.makeText(contexts, "Longitude:" + longitude + "\nLatitude:" + latitude, Toast.LENGTH_SHORT).show();*/
                } else {
                    /*Toast.makeText(contexts, "Currently GPS is not available.", Toast.LENGTH_SHORT).show();*/
                    ss.sendTextMessage(sendingNumber, null, "Currently GPS is not available.", null, null);

                    /*locationTrack.showSettingsAlert();*/
                }
            }
        }
    }
}
