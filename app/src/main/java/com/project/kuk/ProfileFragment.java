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

        binding.logoutButton.setOnClickListener(v -> logoutUser());
        binding.languageButton.setOnClickListener(v -> startActivity(new Intent(getActivity(), LanguageActivity.class)));
        binding.updateProfileButton.setOnClickListener(v -> startActivity(new Intent(getActivity(), UpdateProfileActivity.class)));
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
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String email = mAuth.getCurrentUser().getEmail();
                            String username = snapshot.child("username").getValue(String.class);
                            String imageUrl = snapshot.child("profileImageUrl").getValue(String.class);

                            binding.emailTextView.setText(email != null ? email : getString(R.string.email_not_available));
                            binding.usernameTextView1.setText(username != null ? username : getString(R.string.username_not_available));

                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Glide.with(requireContext()).load(imageUrl).into(binding.uploadImageButton);
                                binding.uploadImageButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            }
                        } else {
                            showToast(R.string.failed_to_load_data);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        showToast(R.string.failed_to_load_data);
                    }
                });
    }

    private void logoutUser() {
        mAuth.signOut();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("rememberMe", false);
        editor.apply();

        showToast(R.string.logged_out);
        startActivity(new Intent(getActivity(), SignInActivity.class));
        getActivity().finish();
    }

    private void confirmDeleteAccount() {
        final EditText input = new EditText(requireContext());
        input.setHint(getString(R.string.enter_password));

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirm_deletion)
                .setMessage(R.string.enter_password_to_delete)
                .setView(input)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    String password = input.getText().toString().trim();
                    if (!password.isEmpty()) {
                        reauthenticateAndDelete(password);
                    } else {
                        showToast(R.string.password_required);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void reauthenticateAndDelete(String password) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            showToast(R.string.user_not_logged_in);
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

                                showToast(R.string.account_deleted);
                                startActivity(new Intent(getActivity(), SignInActivity.class));
                                getActivity().finish();
                            } else {
                                showToast(R.string.failed_to_delete_account);
                            }
                        });
                    } else {
                        showToast(R.string.reauthentication_failed);
                    }
                });
    }

    private void checkAdminAndOpenPanel() {
        if (mAuth.getCurrentUser() == null) {
            showToast(R.string.user_not_logged_in);
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
                        startActivity(new Intent(getActivity(), AdminActivity.class));
                    } else {
                        showToast(R.string.access_denied);
                    }
                })
                .addOnFailureListener(e -> showToast(R.string.failed_to_verify_role));
    }

    private void showToast(int messageId) {
        Toast.makeText(getContext(), getString(messageId), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}




