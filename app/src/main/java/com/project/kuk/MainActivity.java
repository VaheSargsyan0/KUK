package com.project.kuk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.project.kuk.databinding.ActivityMainBinding;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SharedPreferences sharedPreferences;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        LocaleHelper.setAppLanguage(this);
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        boolean rememberMe = sharedPreferences.getBoolean("rememberMe", false);
        if (rememberMe) {
            startActivity(new Intent(MainActivity.this, MainMenuActivity.class));
            finish();
        }

        EditText passwordEt = binding.passwordEt;
        EditText confirmPasswordEt = binding.confirmPasswordEt;
        ImageView showPasswordBtn = findViewById(R.id.show_password_btn);
        ImageView showConfirmPasswordBtn = findViewById(R.id.show_confirm_password_btn);

        showPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPasswordVisible = !isPasswordVisible;
                if (isPasswordVisible) {
                    passwordEt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    showPasswordBtn.setImageResource(R.drawable.ic_eye_open);
                } else {
                    passwordEt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    showPasswordBtn.setImageResource(R.drawable.ic_eye_closed);
                }
                passwordEt.setSelection(passwordEt.length());
            }
        });

        showConfirmPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isConfirmPasswordVisible = !isConfirmPasswordVisible;
                if (isConfirmPasswordVisible) {
                    confirmPasswordEt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    showConfirmPasswordBtn.setImageResource(R.drawable.ic_eye_open);
                } else {
                    confirmPasswordEt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    showConfirmPasswordBtn.setImageResource(R.drawable.ic_eye_closed);
                }
                confirmPasswordEt.setSelection(confirmPasswordEt.length());
            }
        });

        binding.signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.emailEt.getText().toString();
                String password = binding.passwordEt.getText().toString();
                String confirmPassword = binding.confirmPasswordEt.getText().toString();
                String username = binding.usernameEt.getText().toString();

                if (email.isEmpty() || password.isEmpty() || username.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.fields_cannot_be_empty), Toast.LENGTH_SHORT).show();
                } else if (!password.equals(confirmPassword)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.passwords_do_not_match), Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        String profileImageUrl = "";
                                        String phoneNum = "";

                                        HashMap<String, String> userInfo = new HashMap<>();
                                        userInfo.put("email", email);
                                        userInfo.put("username", username);
                                        userInfo.put("profileImageUrl", profileImageUrl);
                                        userInfo.put("phoneNumber", phoneNum);
                                        userInfo.put("role", "customer");

                                        FirebaseDatabase.getInstance().getReference()
                                                .child("Users")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .setValue(userInfo)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                                            editor.putBoolean("rememberMe", true);
                                                            editor.apply();

                                                            startActivity(new Intent(MainActivity.this, MainMenuActivity.class));
                                                            finish();
                                                        }
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(MainActivity.this, getString(R.string.registration_failed) + ": " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }
            }
        });

        binding.goToSigninActivityTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SignInActivity.class));
            }
        });
    }
}




