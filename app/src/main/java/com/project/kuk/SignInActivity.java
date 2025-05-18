package com.project.kuk;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.project.kuk.databinding.ActivitySignInBinding;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private SharedPreferences sharedPreferences;
    private final boolean[] isPasswordVisible = {false};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        boolean rememberMe = sharedPreferences.getBoolean("rememberMe", false);
        if (rememberMe) {
            startActivity(new Intent(SignInActivity.this, MainMenuActivity.class));
            finish();
        }

        binding.showPasswordBtn.setOnClickListener(v -> togglePasswordVisibility(binding.passwordEt, binding.showPasswordBtn));

        binding.loginBtn.setOnClickListener(v -> {
            String email = binding.emailEt.getText().toString();
            String password = binding.passwordEt.getText().toString();
            boolean rememberMeChecked = binding.rememberMeCheckBox.isChecked();

            if (!validateInput(email, password)) return;

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> handleSignInResult(task, rememberMeChecked));
        });

        binding.goToRegisterActivityTv.setOnClickListener(v -> startActivity(new Intent(SignInActivity.this, MainActivity.class)));
    }

    private void togglePasswordVisibility(EditText passwordField, ImageView toggleButton) {
        isPasswordVisible[0] = !isPasswordVisible[0];
        passwordField.setInputType(isPasswordVisible[0] ?
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        toggleButton.setImageResource(isPasswordVisible[0] ? R.drawable.ic_eye_open : R.drawable.ic_eye_closed);
        passwordField.setSelection(passwordField.length());
    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.fields_cannot_be_empty), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, getString(R.string.invalid_email_format), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void handleSignInResult(Task<AuthResult> task, boolean rememberMeChecked) {
        if (task.isSuccessful()) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("rememberMe", rememberMeChecked);
            editor.apply();

            startActivity(new Intent(SignInActivity.this, MainMenuActivity.class));
            finish();
        } else {
            handleSignInError(task.getException());
        }
    }

    private void handleSignInError(Exception e) {
        if (e instanceof FirebaseAuthInvalidCredentialsException) {
            Toast.makeText(SignInActivity.this, getString(R.string.invalid_credentials), Toast.LENGTH_LONG).show();
        } else if (e instanceof FirebaseAuthInvalidUserException) {
            Toast.makeText(SignInActivity.this, getString(R.string.account_not_found), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(SignInActivity.this, getString(R.string.login_failed) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}



