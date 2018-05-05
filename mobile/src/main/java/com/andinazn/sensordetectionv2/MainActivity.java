/*
*** Created by Andina Zahra Nabilla on 10 April 2018
*** Created by Astrid Gloria on 30 April 2018
*** Created by Kalista Umari on 2 May 2018
*
* Active Sensor: Heartrate & Accelerometer Monitor
* Build Gradle Version: 2.3.0
* Mobile SDK Version: 25.0.3
* Wear SDK Version: 25.0.0
*
* Done right:
* + Hasil pengiriman sensor muncul di logcat
* + Sensor data heartrate muncul di TextView
* + Sensor accelerometer diterima bukan 0 kalau fall detected (accelerometer)
* + Fall detected processing (accelerometer)
* + SMS gateway (accelerometer)
* + Show notification fall detected (accelerometer)
* + Data heartrate masuk ke firebase (heartrate)
* + Heartrate processing (heartrate)
* + SMS gateway (heartrate abnormality)
* + Show notification heartrate abnormality & overtired (heartrate)
* + Accelerometer dan heartrate monitor bisa aktif bersamaan (intent broadcast)
* + Login registrasi sukses
* + Input umur ke firebase dan dipanggil kembali untuk processing
* + Upload state fall & heartrate abnormality detected ke firebase
* + Upload hr, timestamp, state, gps ke firebase secara array child dari child (Astrid)
*
* Need fixing:
* - Banyak array event.values accelerometer yg skip (fall detected susah)
* - Layout mobile
* - Timestamp (astrid)
* - Input no HP ke firebase (astrid)
* - GPS (Amira)
* - Fragment / NavigationDrawer
* - Max 590 records (20 menit)
* - Timestamp masih salah (data)
* - Timestamp masih salah (textview)
 */

package com.andinazn.sensordetectionv2;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opencsv.CSVWriter;
import com.andinazn.sensordetectionv2.database.DatabaseHandler;
import com.andinazn.sensordetectionv2.model.MonitorDataModel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private static RemoteSensorManager remoteSensorManager;
    FirebaseAuth auth;
    FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        remoteSensorManager = RemoteSensorManager.getInstance(this);
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));



        DatabaseReference userdatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        DatabaseReference ref = userdatabase.child(user.getUid()).child("age");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                    int ageValue = dataSnapshot.getValue(Integer.class);

                    //INPUT AGE
                    Log.i("user age mainactivity", "user age " + ageValue);
                    Bundle bundle = new Bundle();
                    bundle.putInt("USER_AGE2", ageValue);
                    PlaceholderFragment fragInfo = new PlaceholderFragment();
                    fragInfo.setArguments(bundle);
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, fragInfo);
                    transaction.commit();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        TextView hrTxt;
        TextView falldetectionTxt;
        TextView lastSyncTxt;
        Switch mSwitch;

        long lastMeasurementTime = 0L;
        boolean isRunning = false;
        boolean isStop = false;

        //Input age for heartrate processing
        public int ageValue2;



        FirebaseAuth auth;
        FirebaseUser user;

        //astrid v-1
        DatabaseReference databaseUser;
        DatabaseReference ref;
        DatabaseReference ref1;
        DatabaseReference ref2;
        List<CreateHR> hrlist;
        List<CreateHRState> hrstatelist;
        List<CreateFallState> fallstatelist;


        //astrid
        private final int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;
        private final String SENT = "SMS_SENT";
        private final String DELIVERED = "SMS_DELIVERED";
        PendingIntent sentPI, deliveredPI;
        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            mSwitch = (Switch) rootView.findViewById(R.id.hrSwitch);
            lastSyncTxt = (TextView) rootView.findViewById(R.id.lastSyncTxt);

            //Show to textview
            hrTxt = (TextView) rootView.findViewById(R.id.hrTxt);
            falldetectionTxt = (TextView) rootView.findViewById(R.id.falldetectionTxt);

            //Input age for heartrate processing
            Bundle args = getArguments();
            ageValue2 = args.getInt("USER_AGE2", 0);
            Log.i("user age fragment", "user age " + ageValue2);


            //DATABASE KALISTA
            databaseUser = FirebaseDatabase.getInstance().getReference().child("Users");
            auth = FirebaseAuth.getInstance();
            user = auth.getCurrentUser();
            ref = databaseUser.child(user.getUid()).child("hrvalue");
            ref1 = databaseUser.child(user.getUid()).child("fallstate");
            ref2= databaseUser.child(user.getUid()).child("hrstate");
            hrlist = new ArrayList<>();
            hrstatelist = new ArrayList<>();
            fallstatelist = new ArrayList<>();




            mSwitch.setOnCheckedChangeListener(checkBtnChange);
            getActivity().registerReceiver(mMessageReceiver, new IntentFilter("com.example.Broadcast"));
            Intent intent = new Intent();
            intent.setAction("com.example.Broadcast");
            intent.putExtra("START_TIME", 0L); // clear millisec time
            getActivity().sendBroadcast(intent);
            return rootView;
        }

        CompoundButton.OnCheckedChangeListener checkBtnChange = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    lastMeasurementTime = System.currentTimeMillis();
                    remoteSensorManager.startMeasurement();
                    Intent intent = new Intent();
                    intent.setAction("com.example.Broadcast1");
                    intent.putExtra("START_TIME", lastMeasurementTime); // get current millisec time
                    getActivity().sendBroadcast(intent);
                    lastSyncTxt.setText(String.valueOf(getDate(lastMeasurementTime)));
                    SharedPreferences pref = getActivity().getSharedPreferences("START_TIME", Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putLong("START_TIME", lastMeasurementTime);
                    editor.apply();

                } else {
                    Intent intent = new Intent();
                    intent.setAction("com.example.Broadcast1");
                    intent.putExtra("START_TIME", 0L); // clear millisec time
                    getActivity().sendBroadcast(intent);
                    lastSyncTxt.setText("");
                    remoteSensorManager.stopMeasurement();
                    measurementDataSaveConfirm(lastMeasurementTime);
                    SharedPreferences pref = getActivity().getSharedPreferences("START_TIME", Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putLong("START_TIME", 0L);
                    editor.apply();
                }
            }
        };

        private void measurementDataSaveConfirm(final long lastMeasurementTime) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setMessage("Do you want to save this measurement?");
            alertDialogBuilder.setPositiveButton("Save",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            //DatabaseHandler db = new DatabaseHandler(getActivity());
                            //db.getAllUserMonitorDataByLastMeasurementTime(lastMeasurementTime);
                            saveMonitorDataToCsv(lastMeasurementTime);

                        }
                    });
            alertDialogBuilder.setNegativeButton("Not Save",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DatabaseHandler db = new DatabaseHandler(getActivity());
                            db.deleteMeasurementDataByMeasurementTime(lastMeasurementTime);
                        }
                    });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }

        private void saveMonitorDataToCsv(long lastMeasurementTime) {
            DatabaseHandler db = new DatabaseHandler(getActivity());
            List<MonitorDataModel> mm = db.getAllUserMonitorDataByLastMeasurementTime(lastMeasurementTime);
            CSVWriter writer = null;
            try
            {
                File direct = new File(Environment.getExternalStorageDirectory() + "/" + "non");
                if(!direct.exists())
                {
                    if(direct.mkdir())
                    {
                        //directory is created;
                    }
                }
                String fileName = getDate2(lastMeasurementTime).concat(".csv").toString();
                writer = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory().getPath().concat("/").concat("non").concat("/").concat(fileName)), ',');
                String[] header = {
                        "id",
                        "sensorUpdateTime",
                        "sensorId",
                        "userId",
                        "accuracy",
                        "measurementTime",
                        "sensorVal"};
                writer.writeNext(header);
                for (int i = 0; i < mm.size(); i++){
                    String[] entries = {
                            String.valueOf(mm.get(i).getId()),
                            mm.get(i).getSensorUpdateTime(),
                            mm.get(i).getSensorId(),
                            mm.get(i).getUserId(),
                            mm.get(i).getAccuracy(),
                            mm.get(i).getMeasurementTime(),
                            mm.get(i).getSensorVal()}; // array of packet values
                    writer.writeNext(entries);
                }
                writer.close();
                Toast.makeText(getActivity(), "Write Text File Complete.", Toast.LENGTH_SHORT).show();
            }
            catch (IOException e)
            {
                Toast.makeText(getActivity(), "Write test text file fail.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }

        // handler for received Intents for the "my-event" event
        private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Extract data included in the Intent
                try {

                    isRunning = intent.getBooleanExtra("IS_RUNNING",false);
                    if (!isRunning) {
                        //Thread.sleep(3000);
                        isStop = true;
                    } else {
                        isStop = false;
                    }
                    if (mSwitch != null) {
                        if (isStop) {
                            mSwitch.setChecked(isRunning);
                        }
                    }

                    int message2 = intent.getIntExtra("ACCR", 0);
                    int sensorType = intent.getIntExtra("SENSOR_TYPE", 0);

                    //HEARTRATE DATA

                    //Receive heartrate data
                    float[] message1 = intent.getFloatArrayExtra("HR");
                    if ((message1 != null ) && (sensorType == 21)) {
                        Log.d("Receiver", "Got HR: " + message1[0] + ". Got Accuracy: " + message2);
                        int tmpHr = (int)Math.ceil(message1[0] - 0.5f);
                        if (tmpHr > 0) {
                            hrTxt.setText(String.valueOf(tmpHr));

                            //database astrid
                            CreateHR hrval = new CreateHR(tmpHr);
                            ref.child("nilaihr").push().setValue(hrval);


                            //Heartrate Processing
                            if (tmpHr < 60.0) {
                                Log.i("abnormality detected", "HEART EMERGENCY, Got HR: " + tmpHr);

                                //Show emergency notification on user's mobile application
                                showNotificationHRemergency();
                                String hrstate = "HEARTRATE ABNORMAL";

                                //Adding state to database
                                CreateHRState kondisihr = new CreateHRState(hrstate);
                                ref2.child("nilaihrstate").push().setValue(kondisihr);


                                //Sending emergency message through SMS gateway
                                String message = "HEART ABNORMALITY DETECTED";
                                String telNr = "081218610106";

                                //Check permission for SMS gateway
                                if (ActivityCompat.checkSelfPermission(getContext(),
                                        Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(getActivity(),
                                            new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
                                } else {
                                    SmsManager sms = SmsManager.getDefault();
                                    sms.sendTextMessage(telNr,null, message, null, null);
                                }
                            }
                            if (ageValue2 != 0) {
                                if (tmpHr > ((220 - ageValue2) * 0.7)) {
                                    Log.i("user age process", "user age " + ageValue2 + ", hr max " + ((220 - ageValue2) * 0.7));
                                    Log.i("abnormality detected", "YOU ARE TIRED, Got HR: " + tmpHr);

                                    //Show emergency notification on user's mobile application
                                    showNotificationHRwarning();
                                }
                            }

                        }
                        DatabaseHandler db = new DatabaseHandler(getActivity());
                        long timeStamp = intent.getLongExtra("TIME", 0)/1000000L;
                        lastSyncTxt.setText(String.valueOf(getDate(timeStamp)) + " / " + db.getAllUserMonitorDataByLastMeasurementTime(lastMeasurementTime).size() + " records");
                    }

                    //ACCELEROMETER DATA

                    //Receive accelerometer data
                    float[] message3 = intent.getFloatArrayExtra("CURRENT");
                    //Accelerometer processing into fall detection conclusion
                    if (sensorType == 1) {
                        float tmpX = (int)Math.ceil(message3[0]);
                        float tmpY = (int)Math.ceil(message3[1]);
                        float tmpZ = (int)Math.ceil(message3[2]);

                        float currentacc [] = {tmpX, tmpY, tmpZ};
                        Log.d("Receiver", "Got Accelerometer: " + Arrays.toString(currentacc) + ". Got Accuracy: " + message2);


                        if ((tmpX == 0)&&(tmpY == 0)&&(tmpZ == 0)) {

                            falldetectionTxt.setText("No fall Detected.");

                        } else {
                            Log.d("Fall", "Fall Detected.");
                            falldetectionTxt.setText("Fall Detected.");
                            databaseUser.child(user.getUid()).child("fallstate").setValue("FALL DETECTED");

                            //Show emergency notification on user's mobile application
                            showNotification();

                            String fallstate = "FALL DETECTED";

                            //Adding state to database
                            CreateFallState kondisifall = new CreateFallState(fallstate);
                            ref1.child("nilaifallstate").push().setValue(kondisifall);

                            //Sending emergency message through SMS gateway
                            String message = "FALL DETECTED";
                            String telNr = "081218610106";

                            //Check permission for SMS gateway
                            if (ActivityCompat.checkSelfPermission(getContext(),
                                            Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
                            } else {
                                SmsManager sms = SmsManager.getDefault();
                                sms.sendTextMessage(telNr,null, message, null, null);
                            }
                        }

                        DatabaseHandler db = new DatabaseHandler(getActivity());
                        long timeStamp = intent.getLongExtra("TIME", 0)/1000000L;
                        lastSyncTxt.setText(String.valueOf(getDate(timeStamp)) + " / " + db.getAllUserMonitorDataByLastMeasurementTime(lastMeasurementTime).size() + " records");
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        public void showNotification() {
            final NotificationManager mgr = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder note = new NotificationCompat.Builder(getContext());
            note.setContentTitle("Fall alert!");
            note.setContentText("A fall has been detected.");
            note.setTicker("Fall Detection Alert!");
            note.setAutoCancel(true);
            // to set default sound/light/vibrate or all
            note.setVibrate(new long[] {1000, 1000, 1000, 1000, 1000});
            note.setDefaults(Notification.DEFAULT_SOUND);

            //UNTUK CUSTOM SOUND
            /*Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                    + "://" + getActivity().getPackageName() + "/" + R.raw.fall_detected_google);
            note.setSound(alarmSound);*/

            // Icon to be set on Notification
            note.setSmallIcon(R.mipmap.ic_launcher);
            // This pending intent will open after notification click
            PendingIntent pi = PendingIntent.getActivity(getContext(), 0, new Intent(getActivity(), MainActivity.class), 0);
            // set pending intent to notification builder
            note.setContentIntent(pi);
            mgr.notify(693020, note.build());
        }

        public void showNotificationHRemergency() {
            final NotificationManager mgr1 = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder note = new NotificationCompat.Builder(getContext());
            note.setContentTitle("HEART RATE EMERGENCY!");
            note.setContentText("An abnormality in your heart rate has been detected");
            note.setTicker("HEART RATE EMERGENCY!");
            note.setAutoCancel(true);
            // to set default sound/light/vibrate or all
            note.setVibrate(new long[] {1000, 1000, 1000, 1000, 1000});
            note.setDefaults(Notification.DEFAULT_SOUND);
            // Icon to be set on Notification
            note.setSmallIcon(R.mipmap.ic_launcher);
            // This pending intent will open after notification click
            PendingIntent pi = PendingIntent.getActivity(getContext(), 0, new Intent(getActivity(), MainActivity.class), 0);
            // set pending intent to notification builder
            note.setContentIntent(pi);
            mgr1.notify(693020, note.build());
        }

        public void showNotificationHRwarning() {
            final NotificationManager mgr2 = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder note = new NotificationCompat.Builder(getContext());
            note.setContentTitle("PLEASE TAKE A REST!");
            note.setContentText("You've overworked yourself. Please take a rest!");
            note.setTicker("PLEASE TAKE A REST!");
            note.setAutoCancel(true);
            // to set default sound/light/vibrate or all
            note.setVibrate(new long[] {1000, 1000, 1000, 1000, 1000});
            note.setDefaults(Notification.DEFAULT_SOUND);
            // Icon to be set on Notification
            note.setSmallIcon(R.mipmap.ic_launcher);
            // This pending intent will open after notification click
            PendingIntent pi = PendingIntent.getActivity(getContext(), 0, new Intent(getActivity(), MainActivity.class), 0);
            // set pending intent to notification builder
            note.setContentIntent(pi);
            mgr2.notify(960302, note.build());
        }

        private String getDate(long timestamp){
            try{
                DateFormat sdf = new SimpleDateFormat("dd cc HH:mm a");
                sdf.setTimeZone(TimeZone.getTimeZone("Asia/Jakarta"));
                Date netDate = (new Date(timestamp));
                return sdf.format(netDate);
            }
            catch(Exception ex){
                return "7:00";
            }
        }

        private String getDate2(long timestamp){
            try{
                DateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                sdf.setTimeZone(TimeZone.getTimeZone("Asia/Jakarta"));
                Date netDate = (new Date(timestamp));
                return sdf.format(netDate);
            }
            catch(Exception ex){
                return "7:00";
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //BusProvider.getInstance().register(this);
        //List<Sensor> sensors = RemoteSensorManager.getInstance(this).getSensors();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //BusProvider.getInstance().unregister(this);
    }

}
