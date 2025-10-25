package com.example.floralfete.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.floralfete.R;
import com.example.floralfete.model.FeedbackModel;

import java.util.List;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.ViewHolder> {
    private List<FeedbackModel> feedbackList;

    public FeedbackAdapter(List<FeedbackModel> feedbackList) {
        this.feedbackList = feedbackList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.feedback_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FeedbackModel feedbackModel = feedbackList.get(position);
        holder.feedbackText1.setText(feedbackModel.getUsername());
        holder.feedbackText2.setText(feedbackModel.getFeedback());
    }

    @Override
    public int getItemCount() {
        return feedbackList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView feedbackText1,feedbackText2;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            feedbackText1 = itemView.findViewById(R.id.feedback_text1);
            feedbackText2 = itemView.findViewById(R.id.feedback_text2);
        }
    }
}
