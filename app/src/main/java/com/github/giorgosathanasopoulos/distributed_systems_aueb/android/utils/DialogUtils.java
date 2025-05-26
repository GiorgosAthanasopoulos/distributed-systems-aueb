package com.github.giorgosathanasopoulos.distributed_systems_aueb.android.utils;

import androidx.appcompat.app.AlertDialog;
import android.content.Context;

public class DialogUtils {
    public static void showAlertDialog(Context context, String title, String message) {
        showAlertDialog(context, title, message, "Ok", null);
    }

    public static void showAlertDialog(Context context, String title, String message, String positive, Runnable positiveTask) {
        showAlertDialog(context, title, message, positive, positiveTask, null, null, null, null);
    }

    public static void showAlertDialog(Context context, String title, String message, String positive, Runnable positiveTask, String negative, Runnable negativeTask) {
        showAlertDialog(context, title, message, positive, positiveTask, null, null, negative, negativeTask);
    }

    public static void showAlertDialog(Context context, String title, String message, String positive, Runnable positiveTask, String neutral, Runnable neutralTask, String negative, Runnable negativeTask) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positive, (dialog, which) -> {
                    if (positiveTask != null)
                        positiveTask.run();
                })
                .setNeutralButton(neutral, (dialog, which) -> {
                    if (neutralTask != null)
                        neutralTask.run();
                })
                .setNegativeButton(negative, (dialog, which) -> {
                    if (negativeTask != null)
                        negativeTask.run();
                })
                .show();
    }
}
