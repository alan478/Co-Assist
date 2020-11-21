package com.example.covidresourceapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.FirebaseError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SelfReportActivity extends AppCompatActivity {
    private static final String TAG = SelfReportActivity.class.getSimpleName();
    GoogleSignInAccount account = MainActivity.getAccount();
    GoogleSignInClient client = MainActivity.getClient();
    String userEmail = account.getEmail();
    String userIdentity = account.getDisplayName();
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self_report);

        Button notifyContacts = findViewById(R.id.notifyContactsButton);
        notifyContacts.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                String userToken = userEmail.substring(0, userEmail.indexOf('@')).replaceAll("[\\-\\+\\.\\^:,]","");
                notifyContacts(userToken);
            }
        });

    }

    // Function to convert Set<String> to String[]
    public static String[] convert(Set<String> setOfString) {

        // Create String[] of size of setOfString
        String[] arrayOfString = new String[setOfString.size()];

        // Copy elements from set to string array
        // using advanced for loop
        int index = 0;
        for (String str : setOfString)
            arrayOfString[index++] = str;

        // return the formed String[]
        return arrayOfString;
    }


    private void notifyContacts(String currentUserID) {

        Set<String> closeContactEmails = new HashSet<String>();
        // Retrieve data from database
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){
                    HashMap<String, Object> dataMap = (HashMap<String, Object>) dataSnapshot.getValue();

                    for (String key : dataMap.keySet()){
                        Object data = dataMap.get(key);
                        HashMap<String, Object> userData = (HashMap<String, Object>) data;

                        for (String key2 : userData.keySet()){
                            Object data2 = userData.get(key2);
                            HashMap<String, Object> userData2 = (HashMap<String, Object>) data2;

                            String userID = (String) userData2.get("userID");
                            String closeContactEmail = (String) userData2.get("closeContactEmail");
                            String timeStamp = (String) userData2.get("timestamp");
                            String currTimeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
                            long diff;

                            try {
                                Date currDate = sdf.parse(currTimeStamp);
                                Date contactDate = sdf.parse(timeStamp);
                                diff = currDate.getTime() - contactDate.getTime();
                                int days = (int) diff / (1000 * 60 * 60 * 24);
                                // Only add to the closeContactEmails list if less than 2 weeks since contact
//                                Log.w(TAG, "userID: " + userID);
                                if (userID.equals(currentUserID) && days < 14){
//                                    Log.w(TAG, "close contact email: " + closeContactEmail);
                                    closeContactEmails.add(closeContactEmail);
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }


                        }

                        }

                    }

                for (String s : closeContactEmails ) {
                    Log.w(TAG, "closeContactEmails: " + s);

                    final Intent emailLauncher = new Intent(Intent.ACTION_SEND);
                    emailLauncher.setType("message/rfc822");
                    emailLauncher.putExtra(Intent.EXTRA_BCC, new String[] {s});
                    emailLauncher.putExtra(Intent.EXTRA_SUBJECT, "Exposure Notification COVID 19");
                    emailLauncher.putExtra(Intent.EXTRA_TEXT, "Dear Sir/Madam, You have recently come into close contact with someone who self-reported a positive COVID-19 test. Here are some Isolation instructions and testing recommendations that you can follow to keep yourself and those around you safe:\n" +
                            "\n" +
                            "If you were diagnosed with COVID-19 in the past 90 days:\n" +
                            "\n" +
                            "\t- If you have no current symptoms of COVID-19, you do not have to quarantine and retesting is not recommended.\n" +
                            "\t- If you do have current symptoms, begin self isolation immediately for 10 days after symptom onset and consult with a healthcare provider to determine if you have been re-infected with COVID-19. Note that quarantine period remains at 14 days irrespective of subsequent test result.\n" +
                            "\n" +
                            "If you were not diagnosed with COVID-19 in the past 90 days:\n" +
                            "\n" +
                            "\t- If you have no symptoms, you are asked to self-quarantine for 14 days from last potential exposure and should be referred for testing.\n" +
                            "\t- If you have symptoms, immediately self-isolate for 10 days after symptom onset and get referred for testing and medical care." +
                            "In general:\n" +
                            "\t\n" +
                            "\t- Cancel or postpone plans that involve social gatherings, vacations or other planned travel until cleared for these activities by the health authorities.\n" +
                            "\t- Monitor your symptoms continuously. COVID-19 symptoms include:\n" +
                            "\t\t- Feel feverish or have a temperature of 100.4⁰F or higher.\n" +
                            "\t\t- Develop a cough or shortness of breath.\n" +
                            "\t\t- Have persistent pain or pressure in your chest.\n" +
                            "\t\t- Develop new confusion.\n" +
                            "\t\t- Are unable to wake up or stay awake.\n" +
                            "\t\t- Have bluish lips or face.\n" +
                            "\t\t- Develop mild symptoms like sore throat, muscle aches, tiredness, or diarrhoea.\n" +
                            "\t- Refer to https://www.cdc.gov/coronavirus/2019-ncov/php/contact-tracing/contact-tracing-plan/contact-tracing.html for more detail");
                    try{
                        startActivity(emailLauncher);
                    }catch(ActivityNotFoundException e){

                    }

                }

                }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }


        });


        // Email the close contact
//        String temp[] = (String[]) closeContactEmails.toArray();

    }

}