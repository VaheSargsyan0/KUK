package com.project.kuk;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.project.kuk.databinding.ActivityUpdateProfileBinding;

import java.util.HashMap;

public class UpdateProfileActivity extends AppCompatActivity {

    private ActivityUpdateProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private Uri imageUri;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.setAppLanguage(this);
        super.onCreate(savedInstanceState);
        binding = ActivityUpdateProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        loadCurrentUserData();

        binding.profileImageView.setOnClickListener(v -> openImagePicker());
        binding.saveChangesButton.setOnClickListener(v -> updateProfile());

        binding.emailEt.setFocusable(false);
        binding.emailEt.setClickable(true);
        binding.emailEt.setOnClickListener(v -> showToast(getString(R.string.email_change_not_allowed)));
    }

    private void loadCurrentUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        database.getReference().child("Users").child(user.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!isFinishing() && !isDestroyed()) {
                            if (snapshot.exists()) {
                                String username = snapshot.child("username").getValue(String.class);
                                String phone = snapshot.child("phoneNumber").getValue(String.class);
                                String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);

                                binding.usernameEt.setText(username);
                                binding.phoneEt.setText(phone);
                                binding.emailEt.setText(user.getEmail());

                                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                    Glide.with(UpdateProfileActivity.this).load(profileImageUrl).into(binding.profileImageView);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        showToast(getString(R.string.failed_to_load_profile));
                    }
                });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            binding.profileImageView.setImageURI(imageUri);
        }
    }

    private void updateProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String username = binding.usernameEt.getText().toString().trim();
        String phone = binding.phoneEt.getText().toString().trim();
        String password = binding.passwordEt.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            showToast(getString(R.string.error_empty_fields));
            return;
        }

        HashMap<String, Object> updates = new HashMap<>();
        updates.put("username", username);
        updates.put("phoneNumber", phone);

        if (!TextUtils.isEmpty(password)) {
            requestPasswordForUpdate(user, password, updates);
        } else {
            updateDatabase(user.getUid(), updates);
        }

        if (imageUri != null) {
            updateProfileImage(user.getUid());
        }
    }

    private void requestPasswordForUpdate(FirebaseUser user, String newPassword, HashMap<String, Object> updates) {
        final EditText passwordInput = new EditText(this);
        passwordInput.setHint(getString(R.string.enter_current_password));

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.confirm_password)
                .setMessage(R.string.enter_password_to_continue)
                .setView(passwordInput)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    String password = passwordInput.getText().toString().trim();
                    if (!TextUtils.isEmpty(password)) {
                        reauthenticateAndUpdatePassword(user, password, newPassword, updates);
                    } else {
                        showToast(getString(R.string.password_required));
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void reauthenticateAndUpdatePassword(FirebaseUser user, String currentPassword, String newPassword, HashMap<String, Object> updates) {
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

        binding.passwordEt.setText("");

        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                user.updatePassword(newPassword)
                        .addOnSuccessListener(unused -> updateDatabase(user.getUid(), updates))
                        .addOnFailureListener(e -> showToast(getString(R.string.password_update_failed) + ": " + e.getMessage()));
            } else {
                showToast(getString(R.string.reauthentication_failed));
            }
        });
    }

    private void updateProfileImage(String uid) {
        storage.getReference().child("profile_images").child(uid + ".jpg")
                .putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            HashMap<String, Object> updates = new HashMap<>();
                            updates.put("profileImageUrl", uri.toString());
                            updateDatabase(uid, updates);
                        }))
                .addOnFailureListener(e -> showToast(getString(R.string.image_upload_failed) + ": " + e.getMessage()));
    }

    private void updateDatabase(String uid, HashMap<String, Object> updates) {
        database.getReference().child("Users").child(uid).updateChildren(updates)
                .addOnSuccessListener(unused -> showToast(getString(R.string.update_successful)))
                .addOnFailureListener(e -> showToast(getString(R.string.profile_update_failed) + ": " + e.getMessage()));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}








