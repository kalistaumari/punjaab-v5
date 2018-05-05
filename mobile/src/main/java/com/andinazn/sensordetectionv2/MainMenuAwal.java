package com.andinazn.sensordetectionv2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainMenuAwal extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (user == null)
        {
            setContentView(R.layout.activity_mainmenuawal);
        }
        else
        {
            Intent myIntent = new Intent(MainMenuAwal.this, MainActivity.class);
            startActivity(myIntent);
            finish();
        }

    }

    public void goToLogin (View v)
    {
        Intent myIntent = new Intent(MainMenuAwal.this, LoginActivity.class);
        startActivity(myIntent);
    }

    public void goToRegister (View v)
    {
        Intent myIntent = new Intent(MainMenuAwal.this, RegisterActivity.class);
        startActivity(myIntent);
    }

}


