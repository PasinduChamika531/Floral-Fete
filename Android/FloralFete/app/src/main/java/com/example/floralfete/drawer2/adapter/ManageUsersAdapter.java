package com.example.floralfete.drawer2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.floralfete.R;
import com.example.floralfete.model.UserModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ManageUsersAdapter extends RecyclerView.Adapter<ManageUsersAdapter.ViewHolder> {
    private Context context;
    private List<UserModel> userList;
    private FirebaseFirestore firestore;

    public ManageUsersAdapter(Context context, List<UserModel> userList) {
        this.context = context;
        this.userList = userList;
        this.firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_manage_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel user = userList.get(position);

        holder.userName.setText(user.getFname() + " " + user.getLname());
        holder.userEmail.setText(user.getEmail());
        holder.userMobile.setText(user.getMobile());

        if (!user.getProfileImageUrl().isEmpty()) {
            // Load profile image
            Glide.with(context)
                    .load(user.getProfileImageUrl())
                    .placeholder(R.drawable.ic_user_profile)
                    .into(holder.userProfileImage);
        }


        // Set status button properties
        if (user.getStatus().equals("active")) {
            holder.statusButton.setText("Active");
            holder.statusButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.green));
        } else {
            holder.statusButton.setText("Inactive");
            holder.statusButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.red));
        }

        // Handle status button click
        holder.statusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newStatus = user.getStatus().equals("active") ? "inactive" : "active";

                firestore.collection("user").document(user.getUserId())
                        .update("status", newStatus)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                user.setStatus(newStatus);
                                notifyItemChanged(position);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "Failed to update status", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView userProfileImage;
        TextView userName, userEmail, userMobile;
        Button statusButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userProfileImage = itemView.findViewById(R.id.manage_user_profile_image);
            userName = itemView.findViewById(R.id.manage_user_name);
            userEmail = itemView.findViewById(R.id.manage_user_email);
            userMobile = itemView.findViewById(R.id.manage_user_mobile);
            statusButton = itemView.findViewById(R.id.manage_user_status_button);
        }
    }
}

