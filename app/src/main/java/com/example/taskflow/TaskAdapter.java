package com.example.taskflow;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnTaskActionListener {
        void onDeleteTask(int position);
        void onToggleComplete(int position, boolean isChecked);
        void onEditTask(int position);
    }

    private final List<Task> taskList;
    private final OnTaskActionListener listener;

    public TaskAdapter(List<Task> taskList, OnTaskActionListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.tvTitle.setText(task.getTitle());

        if (task.isCompleted()) {
            holder.tvTitle.setPaintFlags(
                    holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTitle.setTextColor(Color.GRAY);
        } else {
            holder.tvTitle.setPaintFlags(
                    holder.tvTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTitle.setTextColor(Color.WHITE); // ✅ FIXED: was Color.BLACK
        }

        holder.cbComplete.setOnCheckedChangeListener(null);
        holder.cbComplete.setChecked(task.isCompleted());
        holder.cbComplete.setOnCheckedChangeListener((btn, isChecked) -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onToggleComplete(pos, isChecked);
            }
        });

        int priorityColor;
        switch (task.getPriority()) {
            case "High":
                priorityColor = Color.parseColor("#FF9800"); // Orange
                break;
            case "Medium":
                priorityColor = Color.parseColor("#FFEB3B"); // Yellow
                break;
            case "Low":
                priorityColor = Color.parseColor("#4CAF50"); // Green
                break;
            default:
                priorityColor = Color.GRAY;
                break;
        }

        holder.viewPriorityBar.setBackgroundColor(priorityColor);
        holder.tvPriority.setText(task.getPriority());
        holder.tvPriority.setTextColor(priorityColor);

        holder.btnDelete.setOnClickListener(v ->
                listener.onDeleteTask(holder.getBindingAdapterPosition()));

        holder.itemView.setOnLongClickListener(v -> {
            listener.onEditTask(holder.getBindingAdapterPosition());
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPriority;
        CheckBox cbComplete;
        ImageButton btnDelete;
        View viewPriorityBar;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle         = itemView.findViewById(R.id.tvTaskTitle);
            tvPriority      = itemView.findViewById(R.id.tvPriority);
            cbComplete      = itemView.findViewById(R.id.cbComplete);
            btnDelete       = itemView.findViewById(R.id.btnDelete);
            viewPriorityBar = itemView.findViewById(R.id.viewPriorityBar);
        }
    }
}