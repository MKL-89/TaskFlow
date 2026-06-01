package com.example.taskflow;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskActionListener {

    private static final String PREFS_NAME = "TaskFlowPrefs";
    private static final String TASKS_KEY  = "tasks_json";

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<Task> taskList;
    private List<Task> filteredList;
    private TextView tvTaskCount;
    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTaskCount  = findViewById(R.id.tvTaskCount);
        recyclerView = findViewById(R.id.recyclerView);
        etSearch     = findViewById(R.id.etSearch);
        FloatingActionButton fab = findViewById(R.id.fab);

        taskList     = loadTasksFromPrefs();
        filteredList = new ArrayList<>(taskList);

        adapter = new TaskAdapter(filteredList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(v -> showAddTaskDialog());
        updateTaskCount();

        // real-time search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTasks(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void filterTasks(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(taskList);
        } else {
            for (Task t : taskList) {
                if (t.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(t);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDeleteTask(final int position) {
        new AlertDialog.Builder(this, R.style.DarkDialog)
                .setTitle("Delete Task")
                .setMessage("Remove this task?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Task task = filteredList.get(position);
                    taskList.remove(task);
                    filteredList.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, filteredList.size());
                    saveTasksToPrefs();
                    updateTaskCount();
                    Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onToggleComplete(int position, boolean isChecked) {
        filteredList.get(position).setCompleted(isChecked);
        sortTasks();
        filterTasks(etSearch.getText().toString());
        saveTasksToPrefs();
        updateTaskCount();
    }

    @Override
    public void onEditTask(int position) {
        showEditTaskDialog(position);
    }

    private void showAddTaskDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_task);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView tvTitle  = dialog.findViewById(R.id.tvDialogTitle);
        EditText etTitle  = dialog.findViewById(R.id.etTaskTitle);
        Spinner spinner   = dialog.findViewById(R.id.spinnerPriority);
        Button btnCancel  = dialog.findViewById(R.id.btnCancel);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);

        tvTitle.setText("New Task");
        btnConfirm.setText("Add");
        setupSpinner(spinner, null);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            String title    = etTitle.getText().toString().trim();
            String priority = spinner.getSelectedItem().toString();
            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
                return;
            }
            taskList.add(new Task(title, priority));
            sortTasks();
            filterTasks(etSearch.getText().toString());
            saveTasksToPrefs();
            updateTaskCount();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showEditTaskDialog(final int position) {
        Task task = filteredList.get(position);

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_task);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView tvTitle  = dialog.findViewById(R.id.tvDialogTitle);
        EditText etTitle  = dialog.findViewById(R.id.etTaskTitle);
        Spinner spinner   = dialog.findViewById(R.id.spinnerPriority);
        Button btnCancel  = dialog.findViewById(R.id.btnCancel);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);

        tvTitle.setText("Edit Task");
        btnConfirm.setText("Save");
        setupSpinner(spinner, task);
        etTitle.setText(task.getTitle());

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            String title    = etTitle.getText().toString().trim();
            String priority = spinner.getSelectedItem().toString();
            if (title.isEmpty()) {
                Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            task.setTitle(title);
            task.setPriority(priority);
            sortTasks();
            filterTasks(etSearch.getText().toString());
            saveTasksToPrefs();
            Toast.makeText(this, "Task updated", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void setupSpinner(Spinner spinner, Task existing) {
        String[] options = {"High", "Medium", "Low"};
        ArrayAdapter<String> sa = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, options);
        sa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(sa);

        if (existing != null) {
            for (int i = 0; i < options.length; i++) {
                if (options[i].equals(existing.getPriority())) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }
    }

    private void sortTasks() {
        Collections.sort(taskList, new Comparator<Task>() {
            @Override
            public int compare(Task a, Task b) {
                return Boolean.compare(a.isCompleted(), b.isCompleted());
            }
        });
    }

    private void updateTaskCount() {
        int remaining = 0;
        for (Task t : taskList) {
            if (!t.isCompleted()) remaining++;
        }
        tvTaskCount.setText(remaining + (remaining == 1 ? " task remaining" : " tasks remaining"));
    }

    private void saveTasksToPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        JSONArray arr = new JSONArray();
        for (Task t : taskList) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("id",          t.getId());
                obj.put("title",       t.getTitle());
                obj.put("priority",    t.getPriority());
                obj.put("isCompleted", t.isCompleted());
                arr.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        prefs.edit().putString(TASKS_KEY, arr.toString()).apply();
    }

    private List<Task> loadTasksFromPrefs() {
        List<Task> list = new ArrayList<>();
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(TASKS_KEY, null);
        if (json == null) return list;

        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                list.add(new Task(
                        obj.getLong("id"),
                        obj.getString("title"),
                        obj.getString("priority"),
                        obj.getBoolean("isCompleted")
                ));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }
}