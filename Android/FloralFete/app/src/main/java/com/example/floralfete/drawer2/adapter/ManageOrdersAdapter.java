package com.example.floralfete.drawer2.adapter;

import static com.example.floralfete.customcomponents.CustomToast.successCustomToast;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.floralfete.R;
import com.example.floralfete.drawer.adapter.OrderProductAdapter;
import com.example.floralfete.model.OrderModel;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class ManageOrdersAdapter extends RecyclerView.Adapter<ManageOrdersAdapter.ViewHolder> {

    private Context context;
    private List<OrderModel> orderList;
    private FirebaseFirestore firestore;

    public ManageOrdersAdapter(Context context, List<OrderModel> orderList) {
        this.context = context;
        this.orderList = orderList;
        firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_manage_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderModel order = orderList.get(position);

        holder.orderId.setText("Order ID: " + order.getOrderId());
        holder.userId.setText("User ID: "+order.getUserId());
        holder.userMobile.setText("User Mobile: " + order.getUserMobile());
        holder.orderedDatetime.setText("Ordered Date: " + order.getOrderedDatetime());
        holder.deliveryAddress.setText("Delivery Address: " + order.getDeliveryAddress());
        holder.deliveryDate.setText("Delivery Date: " + order.getDeliveryDate());
        holder.totalAmount.setText("Total: Rs." + order.getTotalAmount());

        // nested RecyclerView for products
        ManageOrderProductAdapter productAdapter = new ManageOrderProductAdapter(order.getProducts());
        holder.productsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        holder.productsRecyclerView.setAdapter(productAdapter);


        // Update button text and color based on order status
        holder.statusButton.setText(order.getOrderStatus());
        switch (order.getOrderStatus()) {
            case "Processing":
                holder.statusButton.setBackgroundColor(context.getResources().getColor(R.color.processing));
                break;
            case "Packing":
                holder.statusButton.setBackgroundColor(context.getResources().getColor(R.color.packing));
                break;
            case "Shipped":
                holder.statusButton.setBackgroundColor(context.getResources().getColor(R.color.shipped));
                break;
            case "Delivered":
                holder.statusButton.setBackgroundColor(context.getResources().getColor(R.color.delivered));
               // holder.statusButton.setEnabled(false);
                break;
        }

        // Update order status on button click
        holder.statusButton.setOnClickListener(v -> {
            String nextStatus = getNextStatus(order.getOrderStatus());
            order.setOrderStatus(nextStatus);
            holder.statusButton.setText(nextStatus);
            firestore.collection("orders").document(order.getOrderId())
                    .update("orderStatus", nextStatus)
                    .addOnSuccessListener(aVoid -> {
                       // Toast.makeText(context, "Order updated", Toast.LENGTH_SHORT).show();
                        successCustomToast("Order Updated",context);
                        notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Error updating order", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView orderId,userId, userMobile, orderedDatetime, deliveryAddress, deliveryDate, totalAmount;
        Button statusButton;
        RecyclerView productsRecyclerView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            orderId = itemView.findViewById(R.id.manage_order_id);
            userId = itemView.findViewById(R.id.manage_order_user_id);
            userMobile = itemView.findViewById(R.id.manage_order_user_mobile);
            orderedDatetime = itemView.findViewById(R.id.manage_order_datetime);
            deliveryAddress = itemView.findViewById(R.id.manage_order_delivery_address);
            deliveryDate = itemView.findViewById(R.id.manage_order_delivery_date);
            totalAmount = itemView.findViewById(R.id.manage_order_total_amount);
            statusButton = itemView.findViewById(R.id.manage_order_status_button);
            productsRecyclerView = itemView.findViewById(R.id.order_products_recycler_view);
        }
    }

    private String getNextStatus(String currentStatus) {
        switch (currentStatus) {
            case "Processing": return "Packing";
            case "Packing": return "Shipped";
            case "Shipped": return "Delivered";
            case "Delivered": return "Processing";
            default: return "Processing";
        }
    }
}
