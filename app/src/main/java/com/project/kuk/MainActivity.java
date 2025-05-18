package com.project.kuk;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.FirebaseDatabase;
import com.project.kuk.databinding.ActivityMainBinding;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SharedPreferences sharedPreferences;

    private final boolean[] isPasswordVisible = {false};
    private final boolean[] isConfirmPasswordVisible = {false};

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

        passwordEt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        confirmPasswordEt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        showPasswordBtn.setOnClickListener(v -> togglePasswordVisibility(passwordEt, showPasswordBtn, isPasswordVisible));
        showConfirmPasswordBtn.setOnClickListener(v -> togglePasswordVisibility(confirmPasswordEt, showConfirmPasswordBtn, isConfirmPasswordVisible));

        binding.signUpBtn.setOnClickListener(v -> {
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
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                saveUserToDatabase(email, username);
                            } else {
                                handleRegistrationError(task.getException());
                            }
                        });
            }
        });

        binding.goToSigninActivityTv.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SignInActivity.class)));
    }

    private void togglePasswordVisibility(EditText passwordField, ImageView toggleButton, boolean[] isVisible) {
        isVisible[0] = !isVisible[0];
        if (isVisible[0]) {
            passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            toggleButton.setImageResource(R.drawable.ic_eye_open);
        } else {
            passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            toggleButton.setImageResource(R.drawable.ic_eye_closed);
        }
        passwordField.setSelection(passwordField.length());
    }

    private void saveUserToDatabase(String email, String username) {
        HashMap<String, String> userInfo = new HashMap<>();
        userInfo.put("email", email);
        userInfo.put("username", username);
        userInfo.put("profileImageUrl", "");
        userInfo.put("phoneNumber", "");
        userInfo.put("role", "customer");

        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(userInfo)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("rememberMe", true);
                        editor.apply();

                        startActivity(new Intent(MainActivity.this, MainMenuActivity.class));
                        finish();
                    }
                });
    }

    private void handleRegistrationError(Exception e) {
        if (e instanceof FirebaseAuthWeakPasswordException) {
            Toast.makeText(MainActivity.this, getString(R.string.weak_password), Toast.LENGTH_LONG).show();
        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
            Toast.makeText(MainActivity.this, getString(R.string.invalid_email), Toast.LENGTH_LONG).show();
        } else if (e instanceof FirebaseAuthUserCollisionException) {
            Toast.makeText(MainActivity.this, getString(R.string.email_already_registered), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(MainActivity.this, getString(R.string.registration_failed) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}





