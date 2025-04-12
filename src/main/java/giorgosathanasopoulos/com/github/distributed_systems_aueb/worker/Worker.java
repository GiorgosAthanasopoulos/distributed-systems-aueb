package giorgosathanasopoulos.com.github.distributed_systems_aueb.worker;

import com.google.gson.JsonSyntaxException;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.master.MasterConfig;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.json.JsonUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.logger.Logger;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.model.Product;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.model.Store;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.AddProductRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.AddStoreRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.DecreaseQuantityRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.IncreaseQuantityRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.ListProductsRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Message;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Message.UserAgent;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.RemoveProductRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Request;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Response;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Response.Status;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.TCPClient;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.uid.UID;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

// TODO: rewrite with cleaner code
public final class Worker implements TCPClient {

    private static final Response c_INVALID_JSON_RESPONSE = new Response(UserAgent.WORKER, -1, "", Status.FAILURE,
            "Invalid json");

    private final Socket c_Server;

    private final List<Store> c_Stores = new ArrayList<>();
    private static final Object c_LOCK = new Object();

    public Worker() {
        c_Server = connectToServer(0);
        sendHandshake();
        listenForMessages();
    }

    @Override
    public void sendHandshake() {
        Request handshake = new Request(
                Request.UserAgent.WORKER,
                UID.getNextUID(),
                "",
                Request.Action.WORKER_HANDSHAKE);
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
                        "Worker::listenForMessages " +
                                c_Server.getLocalSocketAddress() +
                                " received message: " +
                                json);

                new Thread(() -> {
                    Optional<Response> responseOptional = handleMessage(json);
                    responseOptional.ifPresent((Response response) -> {
                        String jsonResponse = JsonUtils.toJson(response);
                        sendMessage(jsonResponse);
                    });
                }).start();
            }
        } catch (IOException e) {
            Logger.error(
                    "Worker::listenForMessages " +
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
                    "Master::handleMessage " +
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
                    "Worker::handleRequest " +
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
        try {
            Request request = JsonUtils.fromJson(p_Json, Request.class);

            switch (request.getAction()) {
                case ADD_PRODUCT -> {
                    synchronized (c_LOCK) {
                        AddProductRequest addProductRequest = JsonUtils.fromJson(p_Json, AddProductRequest.class);
                        Optional<Store> storeOptional = c_Stores
                                .stream()
                                .filter(store -> store
                                        .getStoreName()
                                        .equals(addProductRequest.getStoreName()))
                                .findFirst();

                        if (storeOptional.isEmpty()) {
                            Response invalidStore = new Response(
                                    UserAgent.WORKER,
                                    request.getId(),
                                    p_Json,
                                    Status.FAILURE,
                                    "Invalid store name");
                            return Optional.of(invalidStore);
                        }

                        storeOptional
                                .get()
                                .addProduct(addProductRequest.getProduct());
                        return Optional.of(
                                new Response(
                                        UserAgent.WORKER,
                                        request.getId(),
                                        p_Json,
                                        Status.SUCCESS,
                                        "Product added"));
                    }
                }
                case ADD_STORE -> {
                    synchronized (c_LOCK) {
                        AddStoreRequest addStoreRequest = JsonUtils.fromJson(
                                p_Json,
                                AddStoreRequest.class);
                        Optional<Store> storeOptional = c_Stores
                                .stream()
                                .filter((Store store) -> store
                                        .getStoreName()
                                        .equals(
                                                addStoreRequest
                                                        .getStore()
                                                        .getStoreName()))
                                .findFirst();

                        if (storeOptional.isEmpty()) {
                            c_Stores.add(addStoreRequest.getStore());
                            return Optional.of(
                                    new Response(
                                            UserAgent.WORKER,
                                            request.getId(),
                                            p_Json,
                                            Status.SUCCESS,
                                            "Store added"));
                        }

                        Response storeExists = new Response(
                                UserAgent.WORKER,
                                request.getId(),
                                p_Json,
                                Status.FAILURE,
                                "Store exists");
                        return Optional.of(storeExists);
                    }
                }
                case DECREASE_QUANTITY -> {
                    synchronized (c_LOCK) {
                        DecreaseQuantityRequest decreaseQuantityRequest = JsonUtils.fromJson(
                                p_Json,
                                DecreaseQuantityRequest.class);
                        Optional<Store> storeOptional = c_Stores
                                .stream()
                                .filter((Store store) -> store
                                        .getStoreName()
                                        .equals(
                                                decreaseQuantityRequest.getStoreName()))
                                .findFirst();

                        if (storeOptional.isEmpty()) {
                            Response invalidStore = new Response(
                                    UserAgent.WORKER,
                                    request.getId(),
                                    p_Json,
                                    Status.FAILURE,
                                    "Invalid store name");
                            return Optional.of(invalidStore);
                        }

                        Optional<Product> productOptional = storeOptional
                                .get()
                                .getProducts()
                                .stream()
                                .filter((Product product) -> product
                                        .getProductName()
                                        .equals(
                                                decreaseQuantityRequest.getProductName()))
                                .findAny();

                        if (productOptional.isEmpty()) {
                            Response invalidStore = new Response(
                                    UserAgent.WORKER,
                                    request.getId(),
                                    p_Json,
                                    Status.FAILURE,
                                    "Invalid product name");
                            return Optional.of(invalidStore);
                        }

                        Product product = productOptional.get();
                        product.setAvailableAmount(
                                product.getAvailableAmount() -
                                        decreaseQuantityRequest.getQuantity());
                        return Optional.of(
                                new Response(
                                        UserAgent.WORKER,
                                        request.getId(),
                                        p_Json,
                                        Status.SUCCESS,
                                        "Product quantity decreased"));
                    }
                }
                case INCREASE_QUANTITY -> {
                    synchronized (c_LOCK) {
                        IncreaseQuantityRequest increaseQuantityRequest = JsonUtils.fromJson(
                                p_Json,
                                IncreaseQuantityRequest.class);
                        Optional<Store> storeOptional = c_Stores
                                .stream()
                                .filter((Store store) -> store
                                        .getStoreName()
                                        .equals(
                                                increaseQuantityRequest.getStoreName()))
                                .findFirst();

                        if (storeOptional.isEmpty()) {
                            Response invalidStore = new Response(
                                    UserAgent.WORKER,
                                    request.getId(),
                                    p_Json,
                                    Status.FAILURE,
                                    "Invalid store name");
                            return Optional.of(invalidStore);
                        }

                        Optional<Product> productOptional = storeOptional
                                .get()
                                .getProducts()
                                .stream()
                                .filter((Product product) -> product
                                        .getProductName()
                                        .equals(
                                                increaseQuantityRequest.getProductName()))
                                .findAny();

                        if (productOptional.isEmpty()) {
                            Response invalidStore = new Response(
                                    UserAgent.WORKER,
                                    request.getId(),
                                    p_Json,
                                    Status.FAILURE,
                                    "Invalid product name");
                            return Optional.of(invalidStore);
                        }

                        Product product = productOptional.get();
                        product.setAvailableAmount(
                                product.getAvailableAmount() +
                                        increaseQuantityRequest.getQuantity());
                        return Optional.of(
                                new Response(
                                        UserAgent.WORKER,
                                        request.getId(),
                                        p_Json,
                                        Status.SUCCESS,
                                        "Product quantity increased"));
                    }
                }
                case REMOVE_PRODUCT -> {
                    synchronized (c_LOCK) {
                        RemoveProductRequest removeProductRequest = JsonUtils.fromJson(
                                p_Json,
                                RemoveProductRequest.class);
                        Optional<Store> storeOptional = c_Stores
                                .stream()
                                .filter((Store store) -> store
                                        .getStoreName()
                                        .equals(removeProductRequest.getStoreName()))
                                .findFirst();

                        if (storeOptional.isEmpty()) {
                            Response invalidStore = new Response(
                                    UserAgent.WORKER,
                                    request.getId(),
                                    p_Json,
                                    Status.FAILURE,
                                    "Invalid store name");
                            return Optional.of(invalidStore);
                        }

                        Optional<Product> productOptional = storeOptional
                                .get()
                                .getProducts()
                                .stream()
                                .filter((Product product) -> product
                                        .getProductName()
                                        .equals(
                                                removeProductRequest.getProductName()))
                                .findAny();

                        if (productOptional.isEmpty()) {
                            Response invalidStore = new Response(
                                    UserAgent.WORKER,
                                    request.getId(),
                                    p_Json,
                                    Status.FAILURE,
                                    "Invalid product name");
                            return Optional.of(invalidStore);
                        }

                        storeOptional
                                .get()
                                .removeProduct(
                                        removeProductRequest.getProductName());
                        return Optional.of(
                                new Response(
                                        UserAgent.WORKER,
                                        request.getId(),
                                        p_Json,
                                        Status.SUCCESS,
                                        "Product removed"));
                    }
                }
                case SHOW_SALES_FOOD_TYPE -> {
                    // TODO: send all stores to reducer
                }
                case SHOW_SALES_STORE_TYPE -> {
                    // TODO: send all stores to reducer
                }
                case LIST_STORES -> {
                    // TODO: send all stores back to master
                }
                case FILTER_STORES -> {
                    // TODO: send all stores to reducer
                }
                case LIST_PRODUCTS -> {
                    // TODO:
                    synchronized (c_LOCK) {
                        ListProductsRequest listProductsRequest = JsonUtils.fromJson(p_Json, ListProductsRequest.class);
                        if (listProductsRequest == null) {
                        }
                    }
                }
                case WORKER_HANDSHAKE -> {
                    return Optional.empty();
                }
                case REDUCER_HANDSHAKE -> {
                    return Optional.empty();
                }
                // default -> {
                // Response invalidAction = new Response(
                // UserAgent.WORKER,
                // request.getId(),
                // p_Json,
                // Status.FAILURE,
                // "Invalid action");
                // return Optional.of(invalidAction);
                // }
            }
        } catch (JsonSyntaxException e) {
            Logger.error(
                    "Worker::handleRequest " +
                            c_Server.getLocalSocketAddress() +
                            " couldn't parse json: " +
                            e.getLocalizedMessage());
            return Optional.of(c_INVALID_JSON_RESPONSE);
        }

        Response unknownError = new Response(
                UserAgent.WORKER,
                -1,
                p_Json,
                Status.FAILURE,
                "Unknown error occurred");
        return Optional.of(unknownError);
    }

    @Override
    public Socket connectToServer(int p_Count) {
        if (p_Count > WorkerConfig.c_WORKER_RECONNECT_ATTEMPTS) {
            Logger.error(
                    "Worker::connectToServer reached max amount of retries for trying to connect to server.Exiting...");
            Thread.currentThread().interrupt();
        }

        try {
            Socket server = new Socket(MasterConfig.c_HOST, MasterConfig.c_PORT);
            return server;
        } catch (IOException e) {
            Logger.error(
                    "Worker::Worker " +
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
                    "Worker::sendMessage " +
                            c_Server.getLocalSocketAddress() +
                            " failed to send message: " +
                            e.getLocalizedMessage());
        }
    }
}
