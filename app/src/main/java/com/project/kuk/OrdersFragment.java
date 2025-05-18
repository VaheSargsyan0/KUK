package com.project.kuk;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrdersFragment extends Fragment {

    private RecyclerView recyclerView;
    private OrdersAdapter ordersAdapter;
    private List<OrderItem> orderList;
    private DatabaseReference ordersRef, usersRef;
    private String userId, userRole = "customer";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        orderList = new ArrayList<>();
        ordersAdapter = new OrdersAdapter(orderList, userRole);
        recyclerView.setAdapter(ordersAdapter);

        userId = FirebaseAuth.getInstance().getUid();
        ordersRef = FirebaseDatabase.getInstance().getReference("Orders");
        usersRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        if (userId != null) {
            checkUserRole();
        } else {
            Toast.makeText(getContext(), "Ошибка: пользователь не найден!", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void checkUserRole() {
        usersRef.child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userRole = snapshot.getValue(String.class);

                if ("courier".equalsIgnoreCase(userRole)) {
                    loadAllOrders(false);
                } else if ("admin".equalsIgnoreCase(userRole)) {
                    loadAllOrders(true);
                } else if ("shop_owner".equalsIgnoreCase(userRole)) {
                    loadShopOrders();
                } else {
                    loadUserOrders();
                }

                ordersAdapter.setUserRole(userRole);
                ordersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Ошибка проверки роли", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserOrders() {
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();
                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    Object obj = orderSnapshot.getValue();
                    if (obj instanceof Map) {
                        Map<String, Object> orderData = (Map<String, Object>) obj;
                        OrderItem order = parseOrder(orderData);

                        orderList.add(order);
                    }
                }
                ordersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Ошибка загрузки заказов", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAllOrders(final boolean includeDelivered) {
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();
                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    Object obj = orderSnapshot.getValue();
                    if (obj instanceof Map) {
                        Map<String, Object> orderData = (Map<String, Object>) obj;
                        OrderItem order = parseOrder(orderData);
                        if (includeDelivered || !"Доставлено".equals(order.getStatus())) {
                            orderList.add(order);
                        }
                    }
                }
                ordersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Ошибка загрузки заказов", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadShopOrders() {
        usersRef.child("shopId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String shopId = snapshot.getValue(String.class);
                if (shopId != null) {
                    ordersRef.orderByChild("shopId").equalTo(shopId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            orderList.clear();
                            for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                                Object obj = orderSnapshot.getValue();
                                if (obj instanceof Map) {
                                    Map<String, Object> orderData = (Map<String, Object>) obj;
                                    orderList.add(parseOrder(orderData));
                                }
                            }
                            ordersAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), "Ошибка загрузки заказов", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "Ошибка: магазин не найден!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Ошибка проверки магазина", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private OrderItem parseOrder(Map<String, Object> orderData) {
        String orderId = (String) orderData.get("orderId");
        String orderDate = (String) orderData.get("orderDate");
        String status = (String) orderData.get("status");
        double totalOrderPrice = Double.parseDouble(orderData.get("totalOrderPrice").toString());

        List<CartItem> items = new ArrayList<>();
        Object itemsObject = orderData.get("items");
        if (itemsObject instanceof List) {
            List list = (List) itemsObject;
            for (Object obj : list) {
                if (obj instanceof Map) {
                    Map<String, Object> itemMap = (Map<String, Object>) obj;
                    CartItem cartItem = parseCartItem(itemMap);
                    items.add(cartItem);
                }
            }
        }
        return new OrderItem(orderId, items, totalOrderPrice, orderDate, status);
    }

    private CartItem parseCartItem(Map<String, Object> itemMap) {
        String productId = itemMap.get("productId") != null ? itemMap.get("productId").toString() : "";
        String productName = itemMap.get("productName") != null ? itemMap.get("productName").toString() : "";
        double price = itemMap.get("price") != null ? Double.parseDouble(itemMap.get("price").toString()) : 0;
        int quantity = itemMap.get("quantity") != null ? Integer.parseInt(itemMap.get("quantity").toString()) : 0;
        String imageUrl = itemMap.get("imageUrl") != null ? itemMap.get("imageUrl").toString() : "";
        String userId = itemMap.get("userId") != null ? itemMap.get("userId").toString() : "";
        return new CartItem(productId, productName, price, quantity, imageUrl, userId);
    }
}








