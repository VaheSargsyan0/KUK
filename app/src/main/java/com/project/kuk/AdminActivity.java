package com.project.kuk;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView recyclerViewUsers;
    private UserAdapter userAdapter;
    private List<Map<String, String>> userList;
    private List<Map<String, String>> filteredList;

    private SearchView searchView;
    private Button refreshButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));

        searchView = findViewById(R.id.searchView);
        refreshButton = findViewById(R.id.refreshButton);

        userList = new ArrayList<>();
        filteredList = new ArrayList<>();

        userAdapter = new UserAdapter(this, filteredList);
        recyclerViewUsers.setAdapter(userAdapter);

        loadUsersFromDatabase();

        setupSearch();

        refreshButton.setOnClickListener(v -> {
            loadUsersFromDatabase();
            Toast.makeText(AdminActivity.this, "Users refreshed", Toast.LENGTH_SHORT).show();

            searchView.setQuery("", false);
            searchView.clearFocus();
        });
    }

    private void loadUsersFromDatabase() {
        FirebaseDatabase.getInstance().getReference().child("Users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userList.clear();
                        for (DataSnapshot userSnap : snapshot.getChildren()) {
                            String uid = userSnap.getKey();
                            String username = userSnap.child("username").getValue(String.class);
                            String email = userSnap.child("email").getValue(String.class);
                            String role = userSnap.child("role").getValue(String.class);

                            Map<String, String> userMap = new HashMap<>();
                            userMap.put("uid", uid);
                            userMap.put("username", username != null ? username : "N/A");
                            userMap.put("email", email != null ? email : "N/A");
                            userMap.put("role", role != null ? role : "customer");

                            userList.add(userMap);
                        }

                        filteredList.clear();
                        filteredList.addAll(userList);
                        userAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AdminActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterUsers(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterUsers(newText);
                return true;
            }
        });
    }

    private void filterUsers(String text) {
        filteredList.clear();
        if (text.isEmpty()) {
            filteredList.addAll(userList);
        } else {
            String query = text.toLowerCase();
            for (Map<String, String> user : userList) {
                String username = user.get("username").toLowerCase();
                String email = user.get("email").toLowerCase();
                if (username.contains(query) || email.contains(query)) {
                    filteredList.add(user);
                }
            }
        }
        userAdapter.notifyDataSetChanged();
    }
}



