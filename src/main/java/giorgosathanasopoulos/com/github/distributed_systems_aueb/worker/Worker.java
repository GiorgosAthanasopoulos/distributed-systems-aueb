package giorgosathanasopoulos.com.github.distributed_systems_aueb.worker;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.json.JsonUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.logger.Logger;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.AddProductRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.AddStoreRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.DecreaseQuantityRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.IncreaseQuantityRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.ListProductsRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Message;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.NetworkUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.RemoveProductRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Request;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Response;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Response.Status;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.model.Product;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.model.Store;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.uid.UID;

public class Worker {
    private final Map<String, Store> stores = new HashMap<>();
    private Socket masterSocket;

    public Worker() {
        connectToMaster();
        startListening();
    }

    private void connectToMaster() {
        try {
            masterSocket = new Socket(WorkerConfig.c_MASTER_HOST, WorkerConfig.c_MASTER_PORT);
            Logger.info("Worker1::connectToMaster connected to master at " +
                    WorkerConfig.c_MASTER_HOST + ":" + WorkerConfig.c_MASTER_PORT);

            // Send handshake to master
            Request handshake = new Request(Message.UserAgent.WORKER, UID.getNextUID(),
                    Request.Action.WORKER_HANDSHAKE);
            NetworkUtils.sendMessage(masterSocket, JsonUtils.toJson(handshake));
        } catch (IOException e) {
            Logger.error("Worker1::connectToMaster failed to connect to master: " + e.getMessage());
            System.exit(1);
        }
    }

    private void startListening() {
        try (Scanner scanner = new Scanner(masterSocket.getInputStream())) {
            while (true) {
                if (!scanner.hasNextLine())
                    continue;

                String json = scanner.nextLine().trim();
                Logger.info("Worker1::startListening received message: " + json);

                processMessage(json);
            }
        } catch (IOException e) {
            Logger.error("Worker1::startListening error reading from master: " + e.getMessage());
        } finally {
            try {
                if (masterSocket != null)
                    masterSocket.close();
            } catch (IOException e) {
                Logger.error("Worker1::startListening error closing socket: " + e.getMessage());
            }
        }
    }

    private void processMessage(String json) {
        Request request = JsonUtils.fromJson(json, Request.class);
        if (request == null) {
            Logger.error("Worker1::processMessage failed to parse request");
            return;
        }

        Response response = handleRequest(request, json);
        if (response != null) {
            NetworkUtils.sendMessage(masterSocket, JsonUtils.toJson(response));
        }
    }

    private Response handleRequest(Request request, String originalJson) {
        switch (request.getAction()) {
            case ADD_STORE:
                return handleAddStoreRequest(originalJson);
            case ADD_PRODUCT:
                return handleAddProductRequest(originalJson);
            case REMOVE_PRODUCT:
                return handleRemoveProductRequest(originalJson);
            case INCREASE_QUANTITY:
                return handleIncreaseQuantityRequest(originalJson);
            case DECREASE_QUANTITY:
                return handleDecreaseQuantityRequest(originalJson);
            case LIST_PRODUCTS:
                return handleListProductsRequest(originalJson);
            default:
                return new Response(Message.UserAgent.WORKER, request.getId(),
                        Status.FAILURE, "Unsupported action");
        }
    }

    private Response handleAddStoreRequest(String json) {
        AddStoreRequest request = JsonUtils.fromJson(json, AddStoreRequest.class);
        if (request == null) {
            return new Response(Message.UserAgent.WORKER, -1,
                    Status.FAILURE, "Invalid AddStoreRequest");
        }

        Store store = request.getStore();
        synchronized (stores) {
            if (stores.containsKey(store.getStoreName())) {
                return new Response(Message.UserAgent.WORKER, request.getId(),
                        Status.FAILURE, "Store already exists");
            }

            stores.put(store.getStoreName(), store);
            return new Response(Message.UserAgent.WORKER, request.getId(),
                    Status.SUCCESS, "Store added successfully");
        }
    }

    private Response handleAddProductRequest(String json) {
        AddProductRequest request = JsonUtils.fromJson(json, AddProductRequest.class);
        if (request == null) {
            return new Response(Message.UserAgent.WORKER, -1,
                    Status.FAILURE, "Invalid AddProductRequest");
        }

        synchronized (stores) {
            Store store = stores.get(request.getStoreName());
            if (store == null) {
                return new Response(Message.UserAgent.WORKER, request.getId(),
                        Status.FAILURE, "Store not found");
            }

            Product product = request.getProduct();
            store.addProduct(product);
            return new Response(Message.UserAgent.WORKER, request.getId(),
                    Status.SUCCESS, "Product added successfully");
        }
    }

    private Response handleRemoveProductRequest(String json) {
        RemoveProductRequest request = JsonUtils.fromJson(json, RemoveProductRequest.class);
        if (request == null) {
            return new Response(Message.UserAgent.WORKER, -1,
                    Status.FAILURE, "Invalid RemoveProductRequest");
        }

        synchronized (stores) {
            Store store = stores.get(request.getStoreName());
            if (store == null) {
                return new Response(Message.UserAgent.WORKER, request.getId(),
                        Status.FAILURE, "Store not found");
            }

            if (store.removeProduct(request.getProductName())) {
                return new Response(Message.UserAgent.WORKER, request.getId(),
                        Status.SUCCESS, "Product removed successfully");
            } else {
                return new Response(Message.UserAgent.WORKER, request.getId(),
                        Status.FAILURE, "Product not found");
            }
        }
    }

    private Response handleIncreaseQuantityRequest(String json) {
        IncreaseQuantityRequest request = JsonUtils.fromJson(json, IncreaseQuantityRequest.class);
        if (request == null) {
            return new Response(Message.UserAgent.WORKER, -1,
                    Status.FAILURE, "Invalid IncreaseQuantityRequest");
        }

        synchronized (stores) {
            Store store = stores.get(request.getStoreName());
            if (store == null) {
                return new Response(Message.UserAgent.WORKER, request.getId(),
                        Status.FAILURE, "Store not found");
            }

            Optional<Product> productOptional = store.getProduct(request.getProductName());
            if (productOptional.isEmpty()) {
                return new Response(Message.UserAgent.WORKER, request.getId(),
                        Status.FAILURE, "Product not found");
            }
            Product product = productOptional.get();

            product.increaseQuantity(request.getQuantity());
            return new Response(Message.UserAgent.WORKER, request.getId(),
                    Status.SUCCESS, "Quantity increased successfully");
        }
    }

    private Response handleDecreaseQuantityRequest(String json) {
        DecreaseQuantityRequest request = JsonUtils.fromJson(json, DecreaseQuantityRequest.class);
        if (request == null) {
            return new Response(Message.UserAgent.WORKER, -1,
                    Status.FAILURE, "Invalid DecreaseQuantityRequest");
        }

        synchronized (stores) {
            Store store = stores.get(request.getStoreName());
            if (store == null) {
                return new Response(Message.UserAgent.WORKER, request.getId(),
                        Status.FAILURE, "Store not found");
            }

            Optional<Product> productOptional = store.getProduct(request.getProductName());
            if (productOptional.isEmpty()) {
                return new Response(Message.UserAgent.WORKER, request.getId(),
                        Status.FAILURE, "Product not found");
            }
            Product product = productOptional.get();

            if (!product.decreaseQuantity(request.getQuantity())) {
                return new Response(Message.UserAgent.WORKER, request.getId(),
                        Status.FAILURE, "Insufficient quantity");
            }
            return new Response(Message.UserAgent.WORKER, request.getId(),
                    Status.SUCCESS, "Quantity decreased successfully");
        }
    }

    private Response handleListProductsRequest(String json) {
        ListProductsRequest request = JsonUtils.fromJson(json, ListProductsRequest.class);
        if (request == null) {
            return new Response(Message.UserAgent.WORKER, -1,
                    Status.FAILURE, "Invalid ListProductsRequest");
        }

        synchronized (stores) {
            Store store = stores.get(request.getStoreName());
            if (store == null) {
                return new Response(Message.UserAgent.WORKER, request.getId(),
                        Status.FAILURE, "Store not found");
            }

            return new Response(Message.UserAgent.WORKER, request.getId(),
                    Status.SUCCESS, "Products retrieved successfully");
        }
    }
}
