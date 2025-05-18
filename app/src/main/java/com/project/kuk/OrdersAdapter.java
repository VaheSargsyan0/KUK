package com.project.kuk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {

    private List<OrderItem> orderList;
    private String userRole;
    private DatabaseReference ordersRef;

    public OrdersAdapter(List<OrderItem> orderList, String userRole) {
        this.orderList = orderList;
        this.userRole = userRole;
        this.ordersRef = FirebaseDatabase.getInstance().getReference("Orders");
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderItem order = orderList.get(position);

        holder.orderDate.setText("Дата: " + order.getOrderDate());
        holder.orderStatus.setText("Статус: " + order.getStatus());
        holder.orderTotalPrice.setText("Сумма: " + String.format("%,.2f AMD", order.getTotalOrderPrice()));

        if ("courier".equalsIgnoreCase(userRole) || "admin".equalsIgnoreCase(userRole)) {
            holder.orderActionsContainer.setVisibility(View.VISIBLE);

            if (order.getStatus().equals("В ожидании")) {
                holder.takeOrderButton.setVisibility(View.VISIBLE);
                holder.takeOrderButton.setEnabled(true);
                holder.completeOrderButton.setVisibility(View.GONE);

                holder.takeOrderButton.setOnClickListener(v -> {
                    ordersRef.child(order.getOrderId()).child("status")
                            .setValue("Доставляется")
                            .addOnSuccessListener(aVoid -> {
                                order.setStatus("Доставляется");
                                notifyDataSetChanged();
                                Toast.makeText(v.getContext(),
                                        "Заказ принят в работу!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(v.getContext(),
                                        "Ошибка при принятии заказа!", Toast.LENGTH_SHORT).show();
                            });
                });
            } else if (order.getStatus().equals("Доставляется")) {
                holder.takeOrderButton.setVisibility(View.GONE);
                holder.completeOrderButton.setVisibility(View.VISIBLE);

                holder.completeOrderButton.setOnClickListener(v -> {
                    String orderId = order.getOrderId();
                    ordersRef.child(orderId)
                            .removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(v.getContext(),
                                        "Заказ доставлен и удалён!", Toast.LENGTH_SHORT).show();
                                removeOrderById(orderId);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(v.getContext(),
                                        "Ошибка при удалении заказа: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                });
            } else {
                holder.orderActionsContainer.setVisibility(View.GONE);
            }
        } else {
            holder.orderActionsContainer.setVisibility(View.GONE);
        }
    }

    private void removeOrderById(String orderId) {
        int indexToRemove = -1;
        for (int i = 0; i < orderList.size(); i++) {
            if (orderList.get(i).getOrderId().equals(orderId)) {
                indexToRemove = i;
                break;
            }
        }
        if (indexToRemove != -1) {
            orderList.remove(indexToRemove);
            notifyItemRemoved(indexToRemove);
        } else {
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderDate, orderStatus, orderTotalPrice;
        Button takeOrderButton, completeOrderButton;
        View orderActionsContainer;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderDate = itemView.findViewById(R.id.orderDate);
            orderStatus = itemView.findViewById(R.id.orderStatus);
            orderTotalPrice = itemView.findViewById(R.id.orderTotalPrice);
            orderActionsContainer = itemView.findViewById(R.id.orderActionsContainer);
            takeOrderButton = itemView.findViewById(R.id.takeOrderButton);
            completeOrderButton = itemView.findViewById(R.id.completeOrderButton);
        }
    }
}





