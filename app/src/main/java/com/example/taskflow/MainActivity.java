package com.example.taskflow;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
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
    private TextView tvTaskCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTaskCount  = findViewById(R.id.tvTaskCount);
        recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton fab = findViewById(R.id.fab);

        taskList = loadTasksFromPrefs();

        adapter = new TaskAdapter(taskList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddTaskDialog();
            }
        });

        updateTaskCount();
    }

    // called when user taps the delete icon
    @Override
    public void onDeleteTask(final int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Remove this task?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    taskList.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, taskList.size());
                    saveTasksToPrefs();
                    updateTaskCount();
                    Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }

    // called when checkbox is ticked or unticked
    @Override
    public void onToggleComplete(int position, boolean isChecked) {
        taskList.get(position).setCompleted(isChecked);
        sortTasks();
        adapter.notifyDataSetChanged();
        saveTasksToPrefs();
        updateTaskCount();
    }

    // called on long press to open the edit screen
    @Override
    public void onEditTask(int position) {
        showEditTaskDialog(position);
    }

    // dialog for adding a brand new task
    private void showAddTaskDialog() {
        View dialogView = buildDialogView(null);
        final EditText etTitle = dialogView.findViewById(R.id.etTaskTitle);
        final Spinner  spinner = dialogView.findViewById(R.id.spinnerPriority);

        new AlertDialog.Builder(this)
                .setTitle("New Task")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String title    = etTitle.getText().toString().trim();
                    String priority = spinner.getSelectedItem().toString();

                    if (title.isEmpty()) {
                        Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    taskList.add(new Task(title, priority));
                    sortTasks();
                    adapter.notifyDataSetChanged();
                    saveTasksToPrefs();
                    updateTaskCount();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // dialog for editing an existing task - fields are pre filled
    private void showEditTaskDialog(final int position) {
        Task task = taskList.get(position);
        View dialogView = buildDialogView(task);
        final EditText etTitle = dialogView.findViewById(R.id.etTaskTitle);
        final Spinner  spinner = dialogView.findViewById(R.id.spinnerPriority);

        new AlertDialog.Builder(this)
                .setTitle("Edit Task")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String title    = etTitle.getText().toString().trim();
                    String priority = spinner.getSelectedItem().toString();

                    if (title.isEmpty()) {
                        Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    task.setTitle(title);
                    task.setPriority(priority);
                    sortTasks();
                    adapter.notifyDataSetChanged();
                    saveTasksToPrefs();
                    Toast.makeText(this, "Task updated", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // builds the dialog layout and optionally pre fills it for editing
    private View buildDialogView(Task existing) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_task, null);
        EditText etTitle = view.findViewById(R.id.etTaskTitle);
        Spinner  spinner = view.findViewById(R.id.spinnerPriority);

        String[] options = {"High", "Medium", "Low"};
        ArrayAdapter<String> sa = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, options);
        sa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(sa);

        if (existing != null) {
            etTitle.setText(existing.getTitle());
            for (int i = 0; i < options.length; i++) {
                if (options[i].equals(existing.getPriority())) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }
        return view;
    }

    // incomplete tasks stay at top, completed go to the bottom
    private void sortTasks() {
        Collections.sort(taskList, new Comparator<Task>() {
            @Override
            public int compare(Task a, Task b) {
                return Boolean.compare(a.isCompleted(), b.isCompleted());
            }
        });
    }

    // counts tasks that are not yet done
    private void updateTaskCount() {
        int remaining = 0;
        for (Task t : taskList) {
            if (!t.isCompleted()) remaining++;
        }
        tvTaskCount.setText(remaining + (remaining == 1 ? " task remaining" : " tasks remaining"));
    }

    // saves the full task list as a JSON string
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

    // reads saved tasks back from SharedPreferences on startup
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