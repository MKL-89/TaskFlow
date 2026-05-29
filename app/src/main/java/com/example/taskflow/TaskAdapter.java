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

    private List<Task> taskList;
    private OnTaskActionListener listener;

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
            holder.tvTitle.setTextColor(Color.BLACK);
        }

        holder.cbComplete.setOnCheckedChangeListener(null);
        holder.cbComplete.setChecked(task.isCompleted());
        holder.cbComplete.setOnCheckedChangeListener((btn, isChecked) ->
                listener.onToggleComplete(holder.getAdapterPosition(), isChecked));

        switch (task.getPriority()) {
            case "High":
                holder.viewPriorityDot.setBackgroundColor(Color.parseColor("#F44336"));
                break;
            case "Medium":
                holder.viewPriorityDot.setBackgroundColor(Color.parseColor("#FF9800"));
                break;
            case "Low":
                holder.viewPriorityDot.setBackgroundColor(Color.parseColor("#4CAF50"));
                break;
        }
        holder.tvPriority.setText(task.getPriority());

        holder.btnDelete.setOnClickListener(v ->
                listener.onDeleteTask(holder.getAdapterPosition()));

        holder.itemView.setOnLongClickListener(v -> {
            listener.onEditTask(holder.getAdapterPosition());
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
        View viewPriorityDot;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle         = itemView.findViewById(R.id.tvTaskTitle);
            tvPriority      = itemView.findViewById(R.id.tvPriority);
            cbComplete      = itemView.findViewById(R.id.cbComplete);
            btnDelete       = itemView.findViewById(R.id.btnDelete);
            viewPriorityDot = itemView.findViewById(R.id.viewPriorityDot);
        }
    }
}