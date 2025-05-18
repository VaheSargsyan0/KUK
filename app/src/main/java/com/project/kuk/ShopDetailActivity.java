package com.project.kuk;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.project.kuk.databinding.ActivityShopDetailBinding;
import java.util.ArrayList;
import java.util.List;

public class ShopDetailActivity extends AppCompatActivity {

    private ActivityShopDetailBinding binding;
    private FirebaseDatabase database;
    private String shopId;
    private String shopName;
    private List<Product> productList;
    private List<Product> filteredList;
    private ProductAdapter productAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShopDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database = FirebaseDatabase.getInstance();
        shopId = getIntent().getStringExtra("shopId");
        shopName = getIntent().getStringExtra("shopName");
        Log.d("ShopDetailActivity", "shopId: " + shopId + ", shopName: " + shopName);
        binding.shopNameTextView.setText(shopName);
        setupRecyclerView();
        loadProducts();
        setupSearchView();
        checkPermissions();
        binding.addProductFab.setOnClickListener(v -> {
            Intent intent = new Intent(ShopDetailActivity.this, AddProductActivity.class);
            intent.putExtra("shopId", shopId);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        productList = new ArrayList<>();
        filteredList = new ArrayList<>();
        productAdapter = new ProductAdapter(filteredList, this::openProductDetail);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(productAdapter);
    }

    private void loadProducts() {
        DatabaseReference productsRef = database.getReference("Shops").child(shopId).child("Products");
        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                filteredList.clear();
                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    Product product = productSnapshot.getValue(Product.class);
                    if (product != null) {
                        Log.d("ShopDetailActivity", "Загружен продукт: " + product.getProductName() + " (" + product.getProductId() + ")");
                        productList.add(product);
                        filteredList.add(product);
                    }
                }
                productAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.shopNameTextView.setText("Ошибка загрузки товаров");
            }
        });
    }

    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterProducts(query);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                filterProducts(newText);
                return false;
            }
        });
    }

    private void filterProducts(String query) {
        filteredList.clear();
        for (Product product : productList) {
            if (product.getProductName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(product);
            }
        }
        productAdapter.notifyDataSetChanged();
    }

    private void checkPermissions() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            binding.addProductFab.setVisibility(View.GONE);
            return;
        }
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
        DatabaseReference shopRef = FirebaseDatabase.getInstance().getReference("Shops").child(shopId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                String userRole = snapshot.child("role").getValue(String.class);
                shopRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot shopSnapshot) {
                        if (!shopSnapshot.exists()) return;
                        String ownerId = shopSnapshot.child("ownerId").getValue(String.class);
                        if (user.getUid().equals(ownerId) || "admin".equals(userRole)) {
                            binding.addProductFab.setVisibility(View.VISIBLE);
                        } else {
                            binding.addProductFab.setVisibility(View.GONE);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.addProductFab.setVisibility(View.GONE);
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.addProductFab.setVisibility(View.GONE);
            }
        });
    }

    private void openProductDetail(Product product) {
        Log.d("ProductIdDebug", "Передаем productId: " + product.getProductId());
        Intent intent = new Intent(ShopDetailActivity.this, ProductDetailActivity.class);
        intent.putExtra("productId", product.getProductId());
        intent.putExtra("shopId", shopId);
        startActivity(intent);
    }
}






