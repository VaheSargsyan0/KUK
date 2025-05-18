package com.project.kuk;

import android.os.Bundle;
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
import java.util.List;
import java.util.Map;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView recyclerViewUsers;
    private UserAdapter userAdapter;
    private List<Map<String, String>> userList;
    private List<Map<String, String>> filteredList;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));

        searchView = findViewById(R.id.searchView);

        userList = new ArrayList<>();
        filteredList = new ArrayList<>();
        userAdapter = new UserAdapter(this, filteredList);
        recyclerViewUsers.setAdapter(userAdapter);

        loadUsersFromDatabase();
        setupSearch();

    }

    private void loadUsersFromDatabase() {
        FirebaseDatabase.getInstance().getReference().child("Users")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userList.clear();
                        for (DataSnapshot userSnap : snapshot.getChildren()) {
                            Map<String, String> userMap = Map.of(
                                    "uid", userSnap.getKey(),
                                    "username", userSnap.child("username").getValue(String.class) != null ? userSnap.child("username").getValue(String.class) : "N/A",
                                    "email", userSnap.child("email").getValue(String.class) != null ? userSnap.child("email").getValue(String.class) : "N/A",
                                    "role", userSnap.child("role").getValue(String.class) != null ? userSnap.child("role").getValue(String.class) : "customer"
                            );

                            userList.add(userMap);
                        }

                        filteredList.clear();
                        filteredList.addAll(userList);
                        userAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        showToast(R.string.failed_to_load_users);
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

    private void showToast(int messageId) {
        Toast.makeText(this, getString(messageId), Toast.LENGTH_SHORT).show();
    }
}




