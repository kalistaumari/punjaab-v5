package com.andinazn.sensordetectionv2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class NameActivity extends AppCompatActivity {

    String email,password;
    EditText e5_name;
    EditText ageEntry;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);
        e5_name = (EditText)findViewById(R.id.editText5);
        ageEntry = (EditText)findViewById(R.id.editText6);




        Intent myIntent = getIntent();
        if(myIntent!=null)
        {
          email = myIntent.getStringExtra("email");
          password = myIntent.getStringExtra("password");
        }
    }

    public void generateCode (View v)
    {
        Date myDate = new Date();
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.getDefault());
        String date = format1.format(myDate);
        Random r = new Random();

        int n = 100000 + r.nextInt(900000);
        String code = String.valueOf(n);

        String userData = ageEntry.getText().toString();
        int userNumber = Integer.parseInt(userData);
        Log.i("user age nameact", "user age " + userNumber);

        if((e5_name!=null) && (ageEntry!=null)) //part 6, image deleted this if changed
        {

            Intent myIntent = new Intent(NameActivity.this,InviteCodeActivity.class);
            myIntent.putExtra("name",e5_name.getText().toString());
            myIntent.putExtra("age", userNumber);
            myIntent.putExtra("email",email);
            myIntent.putExtra("password",password);
            myIntent.putExtra("date",date);
            myIntent.putExtra("isSharing", "false");
            myIntent.putExtra("code",code);
            myIntent.putExtra("hrvalue", 0);
            myIntent.putExtra("fallstate", "normal");
            myIntent.putExtra("hrstate", "normal");

            startActivity(myIntent);
            finish();
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Please complete the form", Toast.LENGTH_SHORT).show();
        }
    }


}
