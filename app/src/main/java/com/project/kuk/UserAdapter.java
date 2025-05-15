package com.project.kuk;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Map;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private Context context;
    private List<Map<String, String>> userList;

    public UserAdapter(Context context, List<Map<String, String>> userList) {
        this.context = context;
        this.userList = userList;
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTv, emailTv;
        Spinner roleSpinner;
        Button saveRoleBtn;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTv = itemView.findViewById(R.id.usernameTv);
            emailTv = itemView.findViewById(R.id.emailTv);
            roleSpinner = itemView.findViewById(R.id.roleSpinner);
            saveRoleBtn = itemView.findViewById(R.id.saveRoleBtn);
        }
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_admin, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        Map<String, String> user = userList.get(position);

        String uid = user.get("uid");
        String username = user.get("username");
        String email = user.get("email");
        String role = user.get("role");

        holder.usernameTv.setText(username);
        holder.emailTv.setText(email);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                context,
                R.array.user_roles,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.roleSpinner.setAdapter(adapter);

        if (role != null) {
            int spinnerPosition = adapter.getPosition(role);
            holder.roleSpinner.setSelection(spinnerPosition);
        }

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        boolean isCurrentUser = uid != null && uid.equals(currentUserId);
        boolean isUserAdmin = "admin".equals(role);

        if (isCurrentUser || isUserAdmin) {
            holder.roleSpinner.setEnabled(false);
            holder.saveRoleBtn.setVisibility(View.GONE);
        } else {
            holder.roleSpinner.setEnabled(true);
            holder.saveRoleBtn.setVisibility(View.VISIBLE);

            holder.saveRoleBtn.setOnClickListener(v -> {
                String selectedRole = holder.roleSpinner.getSelectedItem().toString();

                if ("admin".equals(selectedRole)) {
                    Toast.makeText(context, "You cannot assign admin role", Toast.LENGTH_SHORT).show();
                    int pos = adapter.getPosition(role);
                    holder.roleSpinner.setSelection(pos);
                    return;
                }

                if (uid != null) {
                    FirebaseDatabase.getInstance().getReference()
                            .child("Users")
                            .child(uid)
                            .child("role")
                            .setValue(selectedRole)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(context, "Role updated for " + username, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "Failed to update role", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}

