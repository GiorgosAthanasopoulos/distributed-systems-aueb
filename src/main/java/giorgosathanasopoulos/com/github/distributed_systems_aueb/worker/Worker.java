package giorgosathanasopoulos.com.github.distributed_systems_aueb.worker;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.*;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.json.JsonUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.logger.Logger;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.*;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.model.Product;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.model.Store;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.uid.UID;

public class Worker {
    private final Map<String, Store> stores = new HashMap<>();
    // private final Object storesLock = new Object();
    private Socket masterSocket;
    private ExecutorService threadPool;
    private final BlockingQueue<Response> responseQueue;
    private volatile boolean running = true;

    public Worker() {
        this.threadPool = Executors.newFixedThreadPool(WorkerConfig.c_WORKER_THREAD_POOL_SIZE);
        this.responseQueue = new LinkedBlockingQueue<>(WorkerConfig.c_WORKER_MAX_REQUEST_QUEUE);
        connectToMaster();
        startResponseHandler();
        startListening();
    }

    private void connectToMaster() {
        int attempts = 0;
        while (attempts < WorkerConfig.c_WORKER_RECONNECT_ATTEMPTS && running) {
            try {
                masterSocket = new Socket(WorkerConfig.c_MASTER_HOST, WorkerConfig.c_MASTER_PORT);
                masterSocket.setSoTimeout(WorkerConfig.c_SOCKET_TIMEOUT_MS);

                Logger.info("Worker connected to master at " +
                        WorkerConfig.c_MASTER_HOST + ":" + WorkerConfig.c_MASTER_PORT);

                // Send handshake to master
                Request handshake = new Request(Message.UserAgent.WORKER, UID.getNextUID(),
                        Request.Action.WORKER_HANDSHAKE);
                NetworkUtils.sendMessage(masterSocket, JsonUtils.toJson(handshake));
                return;
            } catch (IOException e) {
                attempts++;
                Logger.error("Worker connection attempt " + attempts + " failed: " + e.getMessage());
                try {
                    Thread.sleep(WorkerConfig.c_WORKER_RECONNECT_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        Logger.error("Worker failed to connect to master after " + attempts + " attempts");
        System.exit(1);
    }

    private void startListening() {
        threadPool.execute(() -> {
            try (Scanner scanner = new Scanner(masterSocket.getInputStream())) {
                while (running) {
                    if (!scanner.hasNextLine()) {
                        Thread.sleep(100);
                        continue;
                    }

                    String json = scanner.nextLine().trim();
                    Logger.info("Worker received message: " + json);

                    // Submit task to thread pool
                    threadPool.execute(() -> processMessage(json));
                }
            } catch (IOException e) {
                if (running) {
                    Logger.error("Worker listening error: " + e.getMessage());
                    attemptReconnect();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                cleanup();
            }
        });
    }

    private void startResponseHandler() {
        threadPool.execute(() -> {
            while (running) {
                try {
                    Response response = responseQueue.poll(100, TimeUnit.MILLISECONDS);
                    if (response != null) {
                        sendResponse(response);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    Logger.error("Response handler error: " + e.getMessage());
                }
            }
        });
    }

    private void sendResponse(Response response) {
        synchronized (masterSocket) {
            NetworkUtils.sendMessage(masterSocket, JsonUtils.toJson(response));
            Logger.info("Worker sent response: " + response.getId());
        }
    }

    private void processMessage(String json) {
        try {
            Optional<Request> requestOptional = JsonUtils.fromJson(json, Request.class);
            if (requestOptional.isEmpty()) {
                Logger.error("Worker failed to parse request");
                return;
            }
            Request request = requestOptional.get();

            Response response = handleRequest(request, json);
            if (response != null) {
                responseQueue.put(response);
            }
        } catch (Exception e) {
            Logger.error("Worker error processing message: " + e.getMessage());
        }
    }

    private Response handleRequest(Request request, String originalJson) {
        try {
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
                            Response.Status.FAILURE, "Unsupported action");
            }
        } catch (Exception e) {
            Logger.error("Worker error handling request: " + e.getMessage());
            return new Response(Message.UserAgent.WORKER, request.getId(),
                    Response.Status.FAILURE, "Internal error: " + e.getMessage());
        }
    }

    // All the existing handleXXXRequest methods remain exactly the same
    // (handleAddStoreRequest, handleAddProductRequest, etc.)
    private Response handleAddStoreRequest(String json) {
        Optional<AddStoreRequest> requestOptional = JsonUtils.fromJson(json, AddStoreRequest.class);
        if (requestOptional.isEmpty()) {
            return new Response(Message.UserAgent.WORKER, -1,
                    Response.Status.FAILURE, "Invalid AddStoreRequest");
        }
        AddStoreRequest request = requestOptional.get();

        Store store = request.getStore();
        synchronized (stores) {
            if (stores.containsKey(store.getStoreName())) {
                return new Response(Message.UserAgent.WORKER, request.getId(),
                        Response.Status.FAILURE, "Store already exists");
            }

            stores.put(store.getStoreName(), store);
            return new Response(Message.UserAgent.WORKER, request.getId(),
                    Response.Status.SUCCESS, "Store added successfully");
        }
    }

    private Response handleAddProductRequest(String json) {
        Optional<AddProductRequest> requestOptional = JsonUtils.fromJson(json, AddProductRequest.class);
        if (requestOptional.isEmpty()) {
            return new Response(Message.UserAgent.WORKER, -1,
                    Response.Status.FAILURE, "Invalid AddProductRequest");
        }
        AddProductRequest request = requestOptional.get();

        synchronized (stores) {
            Store store = stores.get(request.getStoreName());
            if (store == null) {
                return new Response(Message.UserAgent.WORKER, request.getId(),
                        Response.Status.FAILURE, "Store not found");
            }

            Product product = request.getProduct();
            store.addProduct(product);
            return new Response(Message.UserAgent.WORKER, request.getId(),
                    Response.Status.SUCCESS, "Product added successfully");
        }
    }

    private Response handleRemoveProductRequest(String json) {
        Optional<RemoveProductRequest> requestOptional = JsonUtils.fromJson(json, RemoveProductRequest.class);
        if (requestOptional.isEmpty()) {
            return new Response(Message.UserAgent.WORKER, -1,
                    Response.Status.FAILURE, "Invalid RemoveProductRequest");
        }
        RemoveProductRequest request = requestOptional.get();

        synchronized (stores) {
            Store store = stores.get(request.getStoreName());
            if (store == null) {
                return new Response(Message.UserAgent.WORKER, request.getId(),
                        Response.Status.FAILURE, "Store not found");
            }

            if (store.removeProduct(request.getProductName())) {
                return new Response(Message.UserAgent.WORKER, request.getId(),
                        Response.Status.SUCCESS, "Product removed successfully");
            } else {
                return new Response(Message.UserAgent.WORKER, request.getId(),
                        Response.Status.FAILURE, "Product not found");
            }
        }
    }

    private Response handleIncreaseQuantityRequest(String json) {
        Optional<IncreaseQuantityRequest> requestOptional = JsonUtils.fromJson(json, IncreaseQuantityRequest.class);
        if (requestOptional.isEmpty()) {
            return new Response(Message.UserAgent.WORKER, -1,
                    Response.Status.FAILURE, "Invalid IncreaseQuantityRequest");
        }
        IncreaseQuantityRequest request = requestOptional.get();

        synchronized (stores) {
            Store store = stores.get(request.getStoreName());
            if (store == null) {
                return new Response(Message.UserAgent.WORKER, request.getId(),
                        Response.Status.FAILURE, "Store not found");
            }

            Optional<Product> productOptional = store.getProduct(request.getProductName());
            if (productOptional.isEmpty()) {
                return new Response(Message.UserAgent.WORKER, request.getId(),
                        Response.Status.FAILURE, "Product not found");
            }
            Product product = productOptional.get();

            product.increaseQuantity(request.getQuantity());
            return new Response(Message.UserAgent.WORKER, request.getId(),
                    Response.Status.SUCCESS, "Quantity increased successfully");
        }
    }

    private Response handleDecreaseQuantityRequest(String json) {
        Optional<DecreaseQuantityRequest> requestOptional = JsonUtils.fromJson(json, DecreaseQuantityRequest.class);
        if (requestOptional.isEmpty()) {
            return new Response(Message.UserAgent.WORKER, -1,
                    Response.Status.FAILURE, "Invalid DecreaseQuantityRequest");
        }
        DecreaseQuantityRequest request = requestOptional.get();

        synchronized (stores) {
            Store store = stores.get(request.getStoreName());
            if (store == null) {
                return new Response(Message.UserAgent.WORKER, request.getId(),
                        Response.Status.FAILURE, "Store not found");
            }

            Optional<Product> productOptional = store.getProduct(request.getProductName());
            if (productOptional.isEmpty()) {
                return new Response(Message.UserAgent.WORKER, request.getId(),
                        Response.Status.FAILURE, "Product not found");
            }
            Product product = productOptional.get();

            if (!product.decreaseQuantity(request.getQuantity())) {
                return new Response(Message.UserAgent.WORKER, request.getId(),
                        Response.Status.FAILURE, "Insufficient quantity");
            }
            return new Response(Message.UserAgent.WORKER, request.getId(),
                    Response.Status.SUCCESS, "Quantity decreased successfully");
        }
    }

    private Response handleListProductsRequest(String json) {
        Optional<ListProductsRequest> requestOptional = JsonUtils.fromJson(json, ListProductsRequest.class);
        if (requestOptional.isEmpty()) {
            return new Response(Message.UserAgent.WORKER, -1,
                    Response.Status.FAILURE, "Invalid ListProductsRequest");
        }
        ListProductsRequest request = requestOptional.get();

        synchronized (stores) {
            Store store = stores.get(request.getStoreName());
            if (store == null) {
                return new Response(Message.UserAgent.WORKER, request.getId(),
                        Response.Status.FAILURE, "Store not found");
            }

            return new Response(Message.UserAgent.WORKER, request.getId(),
                    Response.Status.SUCCESS, "Products retrieved successfully");
        }
    }

    private void attemptReconnect() {
        Logger.info("Worker attempting to reconnect...");
        cleanup();
        connectToMaster();
        startListening();
    }

    private void cleanup() {
        try {
            if (masterSocket != null && !masterSocket.isClosed()) {
                masterSocket.close();
            }
        } catch (IOException e) {
            Logger.error("Worker error closing socket: " + e.getMessage());
        }
    }

    public void shutdown() {
        running = false;
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        cleanup();
    }
}
