package com.project.kuk;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CartFragment extends Fragment {

    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems;
    private DatabaseReference cartRef, ordersRef;
    private String userId;
    private Button clearCartButton, orderCartButton;
    private TextView totalCartPrice;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewCart);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        totalCartPrice = view.findViewById(R.id.totalCartPrice);
        clearCartButton = view.findViewById(R.id.clearCartButton);
        orderCartButton = view.findViewById(R.id.orderCartButton);

        cartItems = new ArrayList<>();
        cartAdapter = new CartAdapter(cartItems, item -> removeCartItem(item));
        recyclerView.setAdapter(cartAdapter);

        userId = FirebaseAuth.getInstance().getUid();
        if (userId != null) {
            cartRef = FirebaseDatabase.getInstance().getReference("Cart").child(userId);
            ordersRef = FirebaseDatabase.getInstance().getReference("Orders");
            loadCartItems();
            setupButtons();
        } else {
            Toast.makeText(getContext(), "Ошибка: пользователь не найден!", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void loadCartItems() {
        cartRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartItems.clear();
                double totalSum = 0;

                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    CartItem item = itemSnapshot.getValue(CartItem.class);
                    if (item != null) {
                        totalSum += item.getTotalPrice();
                        cartItems.add(item);
                    }
                }

                totalCartPrice.setText(String.format("Общая сумма: %,.2f AMD", totalSum));
                cartAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Ошибка загрузки корзины", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeCartItem(CartItem item) {
        cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    CartItem cartItem = itemSnapshot.getValue(CartItem.class);
                    if (cartItem != null && cartItem.getProductId().equals(item.getProductId())) {
                        itemSnapshot.getRef().removeValue().addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Товар удалён!", Toast.LENGTH_SHORT).show();
                            loadCartItems();
                        });
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Ошибка удаления товара", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupButtons() {
        clearCartButton.setOnClickListener(v -> {
            cartRef.removeValue().addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "Корзина очищена!", Toast.LENGTH_SHORT).show();
                totalCartPrice.setText("Общая сумма: 0 AMD");
                cartItems.clear();
                cartAdapter.notifyDataSetChanged();
            });
        });

        orderCartButton.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(getContext(), "Корзина пуста!", Toast.LENGTH_SHORT).show();
                return;
            }

            double totalSum = 0;
            for (CartItem item : cartItems) {
                totalSum += item.getTotalPrice();
            }

            String orderId = ordersRef.push().getKey();
            if (orderId == null) {
                Toast.makeText(getContext(), "Ошибка: orderId не может быть null!", Toast.LENGTH_SHORT).show();
                return;
            }

            String orderDate = DateFormat.getDateInstance().format(new Date());

            OrderItem order = new OrderItem(orderId, cartItems, totalSum, orderDate, "В ожидании");

            ordersRef.child(orderId).setValue(order).addOnSuccessListener(aVoid -> {
                cartRef.removeValue();
                Toast.makeText(getContext(), "Заказ оформлен!", Toast.LENGTH_SHORT).show();
                totalCartPrice.setText("Общая сумма: 0 AMD");
                cartItems.clear();
                cartAdapter.notifyDataSetChanged();
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Ошибка при оформлении заказа", Toast.LENGTH_SHORT).show();
            });
        });
    }
}





