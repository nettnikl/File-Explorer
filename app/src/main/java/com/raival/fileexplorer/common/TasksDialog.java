package com.raival.fileexplorer.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.raival.fileexplorer.App;
import com.raival.fileexplorer.R;
import com.raival.fileexplorer.interfaces.QTab;
import com.raival.fileexplorer.interfaces.QTask;
import com.raival.fileexplorer.interfaces.RegularTask;

import java.io.File;
import java.util.ArrayList;

public class TasksDialog extends BottomSheetDialogFragment {
    private final ArrayList<QTask> tasks;
    private ViewGroup container;
    private final QTab tab;

    public TasksDialog(ArrayList<QTask> tasks, QTab qTab) {
        this.tasks = tasks;
        tab = qTab;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.common_tasks_dialog_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        container = view.findViewById(R.id.container);

        if (tasks.size() != 0) {
            view.findViewById(R.id.place_holder).setVisibility(View.GONE);
        }

        for (QTask task : tasks) {
            addTask(task, !(task instanceof RegularTask) || validate((RegularTask) task));
        }

    }

    @Override
    public int getTheme() {
        return R.style.ThemeOverlay_Material3_BottomSheetDialog;
    }

    private boolean validate(RegularTask task) {
        for (File file : task.getFilesList()) {
            if (!file.exists())
                return false;
        }
        return true;
    }

    private void addTask(QTask task, boolean valid) {
        View v = getLayoutInflater().inflate(R.layout.common_tasks_dialog_item, container, false);

        ((TextView) v.findViewById(R.id.label)).setText(task.getName());
        ((TextView) v.findViewById(R.id.task_details)).setText(task.getDetails());

        v.findViewById(R.id.background).setOnClickListener(view -> {
            if (!valid) {
                App.showWarning("This task is invalid, some files are missing, long click to execute it");
                return;
            }
            execute(task);
        });
        v.findViewById(R.id.remove).setOnClickListener(view -> {
            tasks.remove(task);
            container.removeView(v);
        });

        v.setOnLongClickListener((view -> {
            if (!valid) {
                execute(task);
                return true;
            }
            return false;
        }));

        container.addView(v);
    }

    private void execute(QTask task) {
        tab.handleTask(task);
        tasks.remove(task);
        dismiss();
    }
}