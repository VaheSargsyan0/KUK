package com.project.kuk;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.project.kuk.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FragmentProfileBinding binding;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", getContext().MODE_PRIVATE);

        loadUserDataFromRealtimeDatabase();

        binding.logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("rememberMe", false);
            editor.apply();

            Toast.makeText(getActivity(), "Logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getActivity(), SignInActivity.class));
            getActivity().finish();
        });

        binding.languageButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LanguageActivity.class);
            startActivity(intent);
        });

        binding.updateProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), UpdateProfileActivity.class);
            startActivity(intent);
        });

        binding.deleteAccountButton.setOnClickListener(v -> confirmDeleteAccount());

        binding.adminPanelBut.setOnClickListener(v -> checkAdminAndOpenPanel());

        return view;
    }

    private void loadUserDataFromRealtimeDatabase() {
        if (mAuth.getCurrentUser() == null) return;

        String userId = mAuth.getCurrentUser().getUid();

        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String email = mAuth.getCurrentUser().getEmail();
                            String username = snapshot.child("username").getValue(String.class);
                            String imageUrl = snapshot.child("profileImageUrl").getValue(String.class);

                            binding.emailTextView.setText(email != null ? email : "Email not available");
                            binding.usernameTextView1.setText(username != null ? username : "Username not available");

                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Glide.with(requireContext())
                                        .load(imageUrl)
                                        .into(binding.uploadImageButton);
                                binding.uploadImageButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            }
                        } else {
                            Toast.makeText(getActivity(), "Failed to load user data", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getActivity(), "Failed to load user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void confirmDeleteAccount() {
        final EditText input = new EditText(requireContext());
        input.setHint("Enter your password");

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Confirm Deletion")
                .setMessage("Please enter your password to delete your account:")
                .setView(input)
                .setPositiveButton("Delete", (dialog, which) -> {
                    String password = input.getText().toString().trim();
                    if (!password.isEmpty()) {
                        reauthenticateAndDelete(password);
                    } else {
                        Toast.makeText(getContext(), "Password required", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void reauthenticateAndDelete(String password) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);

        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        user.delete().addOnCompleteListener(deleteTask -> {
                            if (deleteTask.isSuccessful()) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean("rememberMe", false);
                                editor.apply();

                                Toast.makeText(getContext(), "Account deleted", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getActivity(), SignInActivity.class));
                                getActivity().finish();
                            } else {
                                Toast.makeText(getContext(), "Failed to delete account", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(getContext(), "Re-authentication failed. Wrong password?", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkAdminAndOpenPanel() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();

        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(uid)
                .child("role")
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    String role = dataSnapshot.getValue(String.class);
                    if ("admin".equals(role)) {
                        Intent intent = new Intent(getActivity(), AdminActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getContext(), "Access denied: You are not an admin", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to verify role", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}



