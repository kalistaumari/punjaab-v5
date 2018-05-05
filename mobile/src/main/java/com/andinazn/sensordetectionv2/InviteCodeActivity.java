package com.andinazn.sensordetectionv2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class InviteCodeActivity extends AppCompatActivity {

    String name, date, isSharing, code;
    Integer age;
    String email,password;
    TextView t1;
    FirebaseAuth auth;
    ProgressDialog progressDialog;
    FirebaseUser user;
    DatabaseReference reference;
    String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_code);

        t1 =(TextView)findViewById(R.id.textView);
        auth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        Intent myIntent = getIntent();

        reference = FirebaseDatabase.getInstance().getReference().child("Users");

        if(myIntent!=null)
        {
            name = myIntent.getStringExtra("name");
            age = myIntent.getIntExtra("age", 0);
            email = myIntent.getStringExtra("email");
            password = myIntent.getStringExtra("password");
            code = myIntent.getStringExtra("code");
            isSharing = myIntent.getStringExtra("isSharing");
        }
        t1.setText(code);

    }

    public void registerUser (View v)
    {
        progressDialog.setMessage("Please wait while we are creating an account for you.");
        progressDialog.show();

        auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                           //insert values in database
                         user = auth.getCurrentUser();
                         CreateUser createUser = new CreateUser(name,age,email,password,code,"false","na","na",user.getUid(), 0, "normal", "normal");

                         user = auth.getCurrentUser();
                         userId = user.getUid();

                         reference.child(userId).setValue(createUser)
                                 .addOnCompleteListener(new OnCompleteListener<Void>() {
                                     @Override
                                     public void onComplete(@NonNull Task<Void> task) {
                                         if(task.isSuccessful())
                                         {
                                             progressDialog.dismiss();
                                             Toast.makeText(getApplicationContext(), "User registered successfully", Toast.LENGTH_SHORT).show();
                                             finish();
                                             Intent myIntent = new Intent(InviteCodeActivity.this, MainActivity.class);
                                             startActivity(myIntent);
                                         }
                                         else
                                         {
                                             progressDialog.dismiss();
                                             Toast.makeText(getApplicationContext(), "Could not insert values in database (couldnt register)", Toast.LENGTH_SHORT).show();
                                         }
                                     }
                                 });




                        }
                    }
                });



    }
}
