package com.project.kuk;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
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
    }

    private void loadCurrentUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        database.getReference().child("Users").child(user.getUid())
                .get().addOnSuccessListener(snapshot -> {
                    String username = snapshot.child("username").getValue(String.class);
                    String phone = snapshot.child("phoneNumber").getValue(String.class);
                    String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);

                    binding.usernameEt.setText(username);
                    binding.phoneEt.setText(phone);
                    binding.emailEt.setText(user.getEmail());

                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(this)
                                .load(profileImageUrl)
                                .into(binding.profileImageView);
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
        String username = binding.usernameEt.getText().toString().trim();
        String phone = binding.phoneEt.getText().toString().trim();
        String email = binding.emailEt.getText().toString().trim();
        String password = binding.passwordEt.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email)) {
            Toast.makeText(this, getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        HashMap<String, Object> updates = new HashMap<>();
        updates.put("username", username);
        updates.put("phoneNumber", phone);

        if (!email.equals(user.getEmail())) {
            user.updateEmail(email).addOnFailureListener(e ->
                    Toast.makeText(this, getString(R.string.email_update_failed) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        }

        if (!TextUtils.isEmpty(password)) {
            user.updatePassword(password).addOnFailureListener(e ->
                    Toast.makeText(this, getString(R.string.password_update_failed) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        }

        if (imageUri != null) {
            storage.getReference().child("profile_images").child(user.getUid() + ".jpg")
                    .putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                updates.put("profileImageUrl", uri.toString());
                                updateDatabase(user.getUid(), updates);
                            }))
                    .addOnFailureListener(e ->
                            Toast.makeText(this, getString(R.string.image_upload_failed) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        } else {
            updateDatabase(user.getUid(), updates);
        }
    }

    private void updateDatabase(String uid, HashMap<String, Object> updates) {
        database.getReference().child("Users").child(uid).updateChildren(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, getString(R.string.update_successful), Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, getString(R.string.profile_update_failed) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
