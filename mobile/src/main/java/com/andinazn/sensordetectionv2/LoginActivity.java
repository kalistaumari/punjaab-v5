package com.andinazn.sensordetectionv2;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;

public class LoginActivity extends AppCompatActivity {

    FirebaseAuth auth;
    EditText e1,e2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        e1 = (EditText)findViewById(R.id.editText);
        e2 = (EditText)findViewById(R.id.editText2);
        auth = FirebaseAuth.getInstance();


    }


    public void login(View v)
    {
     auth.signInWithEmailAndPassword(e1.getText().toString(),e2.getText().toString())
             .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                 @Override
                 public void onComplete(@NonNull Task<AuthResult> task) {
                     if (task.isSuccessful()) {


                         Toast.makeText(getApplicationContext(), "User logged in Sucessfully", Toast.LENGTH_LONG).show();
                         Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
                         startActivity(myIntent);
                         finish();

                     } else {
                         Toast.makeText(getApplicationContext(), "Wrong email or password", Toast.LENGTH_LONG).show();
                     }
                 }
             });

    }

}
