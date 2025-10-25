package com.example.floralfete.drawer.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.floralfete.R;
import com.example.floralfete.model.Order;

import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orderList;
    private OnItemClickListener onItemClickListener;

    public OrdersAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.textOrderId.setText("Order ID: " + order.getOrderId());
        holder.textTotalAmount.setText("Total: Rs." + String.format("%.2f", order.getTotalAmount()));
        holder.textDeliveryDate.setText("Delivery Date: " + order.getDeliveryDate());
        holder.textStatus.setText(order.getOrderStatus());

        // Set progress bar
        setOrderProgress(holder.progressBar, order.getOrderStatus(),holder);

        holder.itemView.setOnClickListener(v -> {

            if (onItemClickListener != null) {
                Log.i("FloralFeteLog","ItemClicked");
                onItemClickListener.onItemClick(order);
            }
            Log.i("FloralFeteLog","Listner nUll");

        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(Order order);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView textOrderId, textTotalAmount, textDeliveryDate, textStatus;
        ProgressBar progressBar;
        ImageView pin_processing, pin_packing, pin_shipped, pin_delivered;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            textOrderId = itemView.findViewById(R.id.user_text_order_id);
            textTotalAmount = itemView.findViewById(R.id.user_text_order_total);
            textDeliveryDate = itemView.findViewById(R.id.user_text_order_date);
            textStatus = itemView.findViewById(R.id.user_text_order_status);
            progressBar = itemView.findViewById(R.id.user_order_progress_bar);
            pin_processing = itemView.findViewById(R.id.pin_processing);
            pin_packing = itemView.findViewById(R.id.pin_packing);
            pin_shipped = itemView.findViewById(R.id.pin_shipped);
            pin_delivered = itemView.findViewById(R.id.pin_delivered);
        }
    }



    private void setOrderProgress(ProgressBar progressBar, String status, OrderViewHolder holder) {
        int progress;
        int colorResId;

        if (status.equals("Processing")) {
            progress = 0;
            colorResId = R.color.processing;
        } else if (status.equals("Packing")) {
            progress = 1;
            colorResId = R.color.packing;
        } else if (status.equals("Shipped")) {
            progress = 2;
            colorResId = R.color.shipped;
        } else if (status.equals("Delivered")) {
            progress = 3;
            colorResId = R.color.delivered;
        } else {
            progress = 0;
            colorResId = R.color.default_gray;
        }

        // Update progress value
        updatePins(progress, holder);
        progressBar.setProgress(progress);


        // Change progress bar color
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            progressBar.setProgressTintList(context.getResources().getColorStateList(colorResId));
        }
    }


    private void updatePins(int progress, OrderViewHolder holder) {
        // Hide all pins
        holder.pin_processing.setVisibility(View.INVISIBLE);
        holder.pin_packing.setVisibility(View.INVISIBLE);
        holder.pin_shipped.setVisibility(View.INVISIBLE);
        holder.pin_delivered.setVisibility(View.INVISIBLE);

        if (progress == 0) {
            holder.pin_processing.setVisibility(View.VISIBLE);
        }
        if (progress == 1) {
            holder.pin_packing.setVisibility(View.VISIBLE);
        }
        if (progress == 2) {
            holder.pin_shipped.setVisibility(View.VISIBLE);
        }
        if (progress == 3) {
            holder.pin_delivered.setVisibility(View.VISIBLE);
        }
    }


}
