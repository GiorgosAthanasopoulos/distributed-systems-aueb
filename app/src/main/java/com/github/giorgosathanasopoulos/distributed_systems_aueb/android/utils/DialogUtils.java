package com.github.giorgosathanasopoulos.distributed_systems_aueb.android.utils;

import androidx.appcompat.app.AlertDialog;
import android.content.Context;

public class DialogUtils {
    public static void showAlertDialog(Context context, String title, String message) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Ok", (dialog, which) -> {})
                .show();
    }
}
