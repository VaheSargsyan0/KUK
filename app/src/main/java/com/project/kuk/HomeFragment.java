package com.project.kuk;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.project.kuk.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private FirebaseDatabase database;
    private List<Shop> shopList;
    private List<Shop> filteredList;
    private ShopAdapter shopAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        database = FirebaseDatabase.getInstance();

        setupRecyclerView();
        loadShops();
        setupSearchView();
        checkUserRole();

        binding.createShopFab.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CreateShopActivity.class);
            startActivity(intent);
        });

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        shopList = new ArrayList<>();
        filteredList = new ArrayList<>();
        shopAdapter = new ShopAdapter(filteredList);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(shopAdapter);
    }

    private void loadShops() {
        DatabaseReference shopsRef = database.getReference("Shops");
        shopsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                shopList.clear();
                filteredList.clear();
                for (DataSnapshot shopSnapshot : snapshot.getChildren()) {
                    Shop shop = shopSnapshot.getValue(Shop.class);
                    if (shop != null) {
                        shopList.add(shop);
                        filteredList.add(shop);
                    }
                }
                shopAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Ошибка загрузки магазинов");
            }
        });
    }

    private void checkUserRole() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            binding.createShopFab.setVisibility(View.GONE);
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String role = snapshot.child("role").getValue(String.class);

                    if ("shop_owner".equals(role) || "admin".equals(role)) {
                        binding.createShopFab.setVisibility(View.VISIBLE);
                    } else {
                        binding.createShopFab.setVisibility(View.GONE);
                    }
                } else {
                    binding.createShopFab.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.createShopFab.setVisibility(View.GONE);
            }
        });
    }

    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterShops(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterShops(newText);
                return false;
            }
        });
    }

    private void filterShops(String query) {
        filteredList.clear();
        for (Shop shop : shopList) {
            if (shop.getShopName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(shop);
            }
        }
        shopAdapter.notifyDataSetChanged();
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}




