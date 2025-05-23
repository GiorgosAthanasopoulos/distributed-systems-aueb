package com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.json.JsonUtils;
import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.model.Filters;
import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.network.Message;
import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.network.request.FilterStoresRequest;
import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.network.request.Request;
import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.network.response.Response;
import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.uid.UID;

import java.io.IOException;
import java.net.Socket;
import java.util.Optional;
import java.util.Scanner;

public class Backend {
    private static HandlerThread handlerThread;
    private static Handler handler;

    public interface ResponseCallback {
        void onResult(Result<String, String> result);
    }

    public static void init() {
        handlerThread = new HandlerThread("BackendHandlerThread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    private static boolean sendRequest(Request request, ResponseCallback responseCallback) {
        if (handler == null) return false;

        handler.post(() -> {
            try {
                Socket socket = new Socket(Config.c_SERVER_HOST, Config.c_SERVER_PORT);

                String json = JsonUtils.toJson(request);
                socket.getOutputStream().write((json + "\n").getBytes());

                Scanner sc = new Scanner(socket.getInputStream());
                String jsonResponse = sc.nextLine();
                Optional<Response> responseOptional = JsonUtils.fromJson(jsonResponse, Response.class);
                if (responseOptional.isEmpty())
                    new Handler(Looper.getMainLooper()).post(() -> responseCallback.onResult(Result.error("Failed to parse response json")));
                else {
                    Response response = responseOptional.get();
                    if (response.getStatus() == Response.Status.FAILURE)
                        new Handler(Looper.getMainLooper()).post(() -> responseCallback.onResult(Result.error(response.getMessage())));
                    else
                        new Handler(Looper.getMainLooper()).post(() -> responseCallback.onResult(Result.ok(jsonResponse)));
                }
                sc.close();

                socket.close();
            } catch (IOException e) {
                new Handler(Looper.getMainLooper()).post(() -> responseCallback.onResult(Result.error("Failed to send request: " + e.getMessage())));
            }
        });
        return true;
    }

    public static boolean sendFilterStoresRequest(Filters filters, ResponseCallback responseCallback) {
        FilterStoresRequest filterStoresRequest = new FilterStoresRequest(Message.UserAgent.CLIENT, UID.getNextUID(), filters);
        return sendRequest(filterStoresRequest, responseCallback);
    }

    public static void destroy() {
        if (handlerThread != null) {
            handlerThread.quitSafely();
        }
    }
}
