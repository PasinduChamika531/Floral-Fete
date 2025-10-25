package com.example.floralfete.drawer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.floralfete.R;
import com.example.floralfete.model.Order;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orderList;
    private FeedbackListener feedbackListener;

    public interface FeedbackListener {
        void onGiveFeedback(String productId,String orderId);
    }

    public OrderHistoryAdapter(Context context, List<Order> orderList, FeedbackListener feedbackListener) {
        this.context = context;
        this.orderList = orderList;
        this.feedbackListener = feedbackListener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_history, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.textOrderId.setText("Order ID: " + order.getOrderId());
        //format Date
        Date date = new Date((long) order.getOrderedDatetime());
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault());
        String formattedDate = sdf.format(date);
        holder.textOrderedDatetime.setText("Ordered On: " + formattedDate);
        holder.textTotalAmount.setText("Total: Rs. " + String.format("%.2f", order.getTotalAmount()));

        // Set up the product list inside the order
        holder.recyclerViewProducts.setLayoutManager(new LinearLayoutManager(context));
        holder.recyclerViewProducts.setAdapter(new OrderProductAdapter(context, order.getProducts(), feedbackListener,order.getOrderId()));
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView textOrderId, textOrderedDatetime, textTotalAmount;
        RecyclerView recyclerViewProducts;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            textOrderId = itemView.findViewById(R.id.order_history_text_order_id);
            textOrderedDatetime = itemView.findViewById(R.id.order_history_text_order_date);
            textTotalAmount = itemView.findViewById(R.id.order_history_text_total_amount);
            recyclerViewProducts = itemView.findViewById(R.id.order_history_recycler_products);
        }
    }
}
