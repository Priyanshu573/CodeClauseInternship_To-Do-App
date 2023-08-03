package com.example.todowithdesign;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.appcompat.app.AlertDialog;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.List;
import androidx.appcompat.app.AlertDialog;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_TASKS = "tasks";

    private EditText taskInput;
    private LinearLayout taskList;
    private Button addButton;
    private EditText searchBar;

    private List<View> allTaskItems = new ArrayList<>();
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        taskInput = findViewById(R.id.taskInput);
        taskList = findViewById(R.id.taskList);
        addButton = findViewById(R.id.addButton);
        searchBar = findViewById(R.id.searchBar);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTask();
            }
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filter the list based on search input
                filterList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        });

        // Restore tasks from SharedPreferences
        restoreTasksFromSharedPreferences();
    }

    private void addTask() {
        String taskText = taskInput.getText().toString().trim();

        if (!taskText.isEmpty()) {
            addTaskToList(taskText);
            taskInput.getText().clear();
            searchBar.getText().clear();
            saveTasksToSharedPreferences();
            checkAllTasksCompleted();
        }
    }

    private void addTaskToList(String taskText) {
        View listItem = LayoutInflater.from(this).inflate(R.layout.list_item_task, null);
        CheckBox taskCheckbox = listItem.findViewById(R.id.taskCheckbox);
        taskCheckbox.setText(taskText);

        taskCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                taskCheckbox.setPaintFlags(taskCheckbox.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                taskCheckbox.setPaintFlags(taskCheckbox.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            }
            checkAllTasksCompleted();
        });

        Button deleteButton = listItem.findViewById(R.id.deleteButton);
        deleteButton.setVisibility(View.VISIBLE);
        deleteButton.setOnClickListener(v -> {
            taskList.removeView(listItem);
            allTaskItems.remove(listItem);
            saveTasksToSharedPreferences();
            checkAllTasksCompleted();
        });

        taskList.addView(listItem, taskList.getChildCount() - 1); // Add above the separator
        allTaskItems.add(listItem);
    }

    private void filterList(String query) {
        for (View taskItem : allTaskItems) {
            CheckBox taskCheckbox = taskItem.findViewById(R.id.taskCheckbox);
            String taskText = taskCheckbox.getText().toString().toLowerCase();
            if (taskText.contains(query.toLowerCase())) {
                taskItem.setVisibility(View.VISIBLE);
            } else {
                taskItem.setVisibility(View.GONE);
            }
        }
    }

    private void saveTasksToSharedPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();

        JSONArray tasksArray = new JSONArray();
        for (View taskItem : allTaskItems) {
            CheckBox taskCheckbox = taskItem.findViewById(R.id.taskCheckbox);
            tasksArray.put(taskCheckbox.getText().toString());
        }

        editor.putString(KEY_TASKS, tasksArray.toString());
        editor.apply();
    }

    private void restoreTasksFromSharedPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String tasksJson = preferences.getString(KEY_TASKS, null);

        if (tasksJson != null) {
            try {
                JSONArray tasksArray = new JSONArray(tasksJson);
                for (int i = 0; i < tasksArray.length(); i++) {
                    String taskText = tasksArray.getString(i);
                    addTaskToList(taskText);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkAllTasksCompleted() {
        boolean allTasksCompleted = true;
        for (View taskItem : allTaskItems) {
            CheckBox taskCheckbox = taskItem.findViewById(R.id.taskCheckbox);
            if (!taskCheckbox.isChecked()) {
                allTasksCompleted = false;
                break;
            }
        }

        if (allTasksCompleted) {
            showCongratulatoryDialog();
        }
    }

    private void showCongratulatoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.congratulations_dialog, null);
        builder.setView(dialogView)
                .setCancelable(false);

        Button resetButton = dialogView.findViewById(R.id.resetButton);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetAllTasks();
                dialog.dismiss(); // Dismiss the dialog when Reset All button is clicked
            }
        });

        dialog = builder.create(); // Assign the created dialog to the member variable
        dialog.show();
    }

    private void resetAllTasks() {
        taskList.removeAllViews();
        allTaskItems.clear();
        saveTasksToSharedPreferences();
    }
}


