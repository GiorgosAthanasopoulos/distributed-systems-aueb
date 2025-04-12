package giorgosathanasopoulos.com.github.distributed_systems_aueb.reducer;

import java.io.IOException;
import java.net.Socket;
import java.util.Optional;
import java.util.Scanner;

import com.google.gson.JsonSyntaxException;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.master.MasterConfig;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.json.JsonUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.logger.Logger;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Message;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Message.UserAgent;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Request;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Response;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Response.Status;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.TCPClient;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.uid.UID;

// TODO: rewrite with cleaner code
public final class Reducer implements TCPClient {
    private static final Response c_INVALID_JSON_RESPONSE = new Response(UserAgent.MASTER, -1, "", Status.FAILURE,
            "Invalid json");

    private final Socket c_Server;

    public Reducer() {
        c_Server = connectToServer(0);
        sendHandshake();
        listenForMessages();
    }

    @Override
    public void sendHandshake() {
        Request handshake = new Request(
                Request.UserAgent.REDUCER,
                UID.getNextUID(),
                "",
                Request.Action.REDUCER_HANDSHAKE);
        String json = JsonUtils.toJson(handshake);
        sendMessage(json);
    }

    @Override
    public void listenForMessages() {
        try (Scanner sc = new Scanner(c_Server.getInputStream())) {
            while (!c_Server.isClosed()) {
                if (!sc.hasNextLine())
                    continue;

                String json = sc.nextLine();
                Message message = JsonUtils.fromJson(json, Message.class);
                message.setSrc(json);
                Logger.info(
                        "Reducer::listenForMessages " +
                                c_Server.getLocalSocketAddress() +
                                " received message: " +
                                json);

                Optional<Response> responseOptional = handleMessage(json);
                responseOptional.ifPresent((Response response) -> {
                    String jsonResponse = JsonUtils.toJson(response);
                    sendMessage(jsonResponse);
                });
            }
        } catch (IOException e) {
            Logger.error(
                    "Reducer::listenForMessages " +
                            c_Server.getLocalSocketAddress() +
                            " failed to listen for messages: " +
                            e.getLocalizedMessage());
        }
    }

    @Override
    public Optional<Response> handleMessage(String p_Json) {
        try {
            Message message = JsonUtils.fromJson(p_Json, Message.class);

            if (message.getType() == Message.Type.RESPONSE) {
                return handleResponse(p_Json);
            } else if (message.getType() == Message.Type.REQUEST) {
                return handleRequest(p_Json);
            }

            Logger.error(
                    "Reducer::handleMessage " +
                            c_Server.getLocalSocketAddress() +
                            " failed to handle message: " +
                            p_Json);
            Response invalidMessage = new Response(
                    Response.UserAgent.MASTER,
                    message.getId(),
                    "",
                    Response.Status.FAILURE,
                    "Invalid message");
            return Optional.of(invalidMessage);
        } catch (JsonSyntaxException e) {
            Logger.error(
                    "Reducer::handleRequest " +
                            c_Server.getLocalSocketAddress() +
                            " failed to parse json: " +
                            e.getLocalizedMessage());
            return Optional.of(c_INVALID_JSON_RESPONSE);
        }
    }

    @Override
    public Optional<Response> handleResponse(String p_Json) {
        // TODO:
        return Optional.empty();
    }

    @Override
    public Optional<Response> handleRequest(String p_Json) {
        Request request = JsonUtils.fromJson(p_Json, Request.class);
        if (request == null) {
            Logger.error("Reducer::handleRequest failed to parse request json");
            return Optional.of(c_INVALID_JSON_RESPONSE);
        }

        // TODO:
        return Optional.empty();
    }

    @Override
    public Socket connectToServer(int p_Count) {
        if (p_Count > ReducerConfig.c_REDUCER_RECONNECT_ATTEMPTS) {
            Logger.error("Reducer::connectToServer reached max attemps trying to connect to server.Exiting...");
            Thread.currentThread().interrupt();
        }

        try {
            Socket server = new Socket(MasterConfig.c_HOST, MasterConfig.c_PORT);
            return server;
        } catch (IOException e) {
            Logger.error(
                    "Reducer::Reducer " +
                            c_Server.getLocalSocketAddress() +
                            " failed to connect to master: " +
                            e.getLocalizedMessage());
            return connectToServer(p_Count++);
        }

    }

    @Override
    public void sendMessage(String p_Json) {
        try {
            c_Server.getOutputStream().write((p_Json + "\n").getBytes());
        } catch (IOException e) {
            Logger.error(
                    "Reducer::sendMessage " +
                            c_Server.getLocalSocketAddress() +
                            " failed to send message: " +
                            e.getLocalizedMessage());
        }
    }
}
