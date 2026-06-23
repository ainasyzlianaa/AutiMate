package com.example.autimate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StepAdapter extends RecyclerView.Adapter<StepAdapter.StepViewHolder> {

    private List<String> steps;
    private OnStepClickListener listener;

    public interface OnStepClickListener {
        void onEdit(int position);
        void onDelete(int position);
    }

    public StepAdapter(List<String> steps, OnStepClickListener listener) {
        this.steps = steps;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_step, parent, false);
        return new StepViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
        String step = steps.get(position);
        holder.tvStepNumber.setText((position + 1) + ".");
        holder.tvStepText.setText(step);

        holder.btnEditStep.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(position);
            }
        });

        holder.btnDeleteStep.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return steps.size();
    }

    static class StepViewHolder extends RecyclerView.ViewHolder {
        TextView tvStepNumber, tvStepText;
        ImageView btnEditStep, btnDeleteStep;

        StepViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStepNumber = itemView.findViewById(R.id.tvStepNumber);
            tvStepText = itemView.findViewById(R.id.tvStepText);
            btnEditStep = itemView.findViewById(R.id.btnEditStep);
            btnDeleteStep = itemView.findViewById(R.id.btnDeleteStep);
        }
    }
}