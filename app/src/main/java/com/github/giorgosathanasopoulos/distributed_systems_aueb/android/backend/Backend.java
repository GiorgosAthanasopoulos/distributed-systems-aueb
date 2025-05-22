package com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;

import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.json.JsonUtils;
import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.network.request.Request;
import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.network.response.Response;

import java.io.IOException;
import java.net.Socket;
import java.util.Optional;
import java.util.Scanner;

public class Backend {
    private static Socket socket;

    public static Optional<IOException> init(Context context) {
        try {
            socket = new Socket(Config.c_SERVER_HOST, Config.c_SERVER_PORT);
            return Optional.empty();
        } catch (IOException e) {
            return Optional.of(e);
        }
    }

    public static Optional<Response> sendRequest(Request request) {
        if (socket == null || request == null) return Optional.empty();
        try {
            socket.getOutputStream().write((JsonUtils.toJson(request) + "\n").getBytes());
            Scanner sc = new Scanner(socket.getInputStream());
            String response = sc.nextLine();
            Optional<Response> responseOptional = JsonUtils.fromJson(response, Response.class);
            sc.close();
            return responseOptional;
        } catch (IOException ignored) {
            return Optional.empty();
        }
    }
}
