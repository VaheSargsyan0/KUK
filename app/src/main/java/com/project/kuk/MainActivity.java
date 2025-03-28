package com.project.kuk;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private boolean otpSent = false;
    private String id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        final EditText mobileET = findViewById(R.id.mobileET);
        final EditText otp = findViewById(R.id.otp);
        final Button send_sms = findViewById(R.id.send_sms);

        FirebaseApp.initializeApp(this);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        send_sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (otpSent) {
                    final String getOTP = otp.getText().toString();

                    if (id.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Unable to verify", Toast.LENGTH_SHORT).show();
                    } else {

                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(id, getOTP);

                        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {

                                    FirebaseUser userDetails = task.getResult().getUser();

                                    Toast.makeText(MainActivity.this, "Verified", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }


                }  else {
                    final String getMobile = mobileET.getText().toString();

                    PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                            .setPhoneNumber(getMobile)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(MainActivity.this)
                            .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                @Override
                                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                                    Toast.makeText(MainActivity.this, "Code sent successfully", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onVerificationFailed(@NonNull FirebaseException e) {

                                    Toast.makeText(MainActivity.this, "Something went wrong " +e.getMessage(), Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                    super.onCodeSent(s, forceResendingToken);

                                    otp.setVisibility(View.VISIBLE);
                                    send_sms.setText("Verify OTP");
                                    id = s;
                                    otpSent = true;
                                }
                            }).build();

                    PhoneAuthProvider.verifyPhoneNumber(options);
                }

            }
        });

    }
}
