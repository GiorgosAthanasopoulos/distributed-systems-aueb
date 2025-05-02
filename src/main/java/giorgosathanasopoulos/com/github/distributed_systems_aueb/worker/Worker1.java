package giorgosathanasopoulos.com.github.distributed_systems_aueb.worker;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.json.JsonUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.logger.Logger;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.master.MasterConfig;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.model.Filters;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.model.Product;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.model.Store;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.NetworkUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.RemoveProductRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Request;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Response;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.AddProductRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.AddStoreRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.FilterStoresRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.IncreaseQuantityRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.ShowSalesFoodTypeRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.ShowSalesStoreTypeRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.ListProductsRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.ListProductsResponse;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.ListStoresResponse;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Message;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Message.UserAgent;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Request.Action;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Response.About;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Response.Status;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.reducer.ReducerConfig;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.uid.UID;

public class Worker1 {
    private static final Response c_INVALID_JSON_RESPONSE = new Response(UserAgent.MASTER, -1, Status.FAILURE,
            "Invalid json");

    private final Socket c_Server;
    private final Socket c_Reducer;

    private final List<Store> c_STORES;

    public Worker1() {
        c_Server = connectToServer(MasterConfig.c_HOST, MasterConfig.c_PORT);
        sendHandshake(c_Server);
        c_Reducer = connectToServer(ReducerConfig.c_HOST, ReducerConfig.c_PORT);
        sendHandshake(c_Reducer);

        c_STORES = new ArrayList<>();

        listenForMessages(c_Server);
        listenForMessages(c_Reducer);
    }

    private Socket connectToServer(String p_Addr, int p_Port) {
        try {
            Socket socket = new Socket(p_Addr, p_Port);
            Logger.info(String.format("Worker::connectToServer worker %s connected to server %s",
                    socket.getLocalSocketAddress(), socket.getRemoteSocketAddress()));
            return socket;
        } catch (IOException e) {
            Logger.error("Worker1::connectToServer failed to connect to server: " + e.getMessage());
            return null;
        }
    }

    private void sendHandshake(Socket p_To) {
        NetworkUtils.sendMessage(p_To, new Request(UserAgent.WORKER, UID.getNextUID(), Action.WORKER_HANDSHAKE));
    }

    private void listenForMessages(Socket p_To) {
        String addr = p_To.getLocalSocketAddress().toString();

        Scanner sc;
        try {
            sc = new Scanner(p_To.getInputStream());
        } catch (IOException e) {
            Logger
                    .error("Worker::listenForMessages " + addr + "failed to create socket scanner: " + e.getMessage());
            return;
        }

        while (!p_To.isClosed()) {
            if (!sc.hasNextLine())
                continue;

            String json = sc.nextLine().trim();
            Logger.info("Worker::clientThread " + addr + " received message: " + json);
            new Thread(() -> processMessage(p_To, json)).start();
        }

        Logger.info("Worker::listenForMessages worker " + addr + " disconnected");
        sc.close();
    }

    private void processMessage(Socket p_To, String p_Json) {
        if (p_Json == null || p_Json.isBlank() || p_Json.isEmpty()) {
            Logger.error("Worker::processMessage received empty message");
            return;
        }

        handleMessage(p_To, p_Json).ifPresent((Response r) -> NetworkUtils.sendMessage(p_To, JsonUtils.toJson(r)));
    }

    private Optional<Response> handleMessage(Socket p_To, String p_Json) {
        String addr = p_To.getLocalSocketAddress().toString();

        Optional<Message> messageOptional = JsonUtils.fromJson(p_Json, Message.class);
        if (messageOptional.isEmpty()) {
            Logger.error("Worker::handleMessage " + addr + " received invalid json message");
            return Optional.of(c_INVALID_JSON_RESPONSE);
        }
        Message message = messageOptional.get();

        return switch (message.getType()) {
            case REQUEST -> handleRequest(p_To, message, p_Json);
            case RESPONSE -> handleResponse(p_To, message, p_Json);
        };
    }

    private Optional<Response> handleResponse(Socket p_To, Message message, String p_Json) {
        // TODO: skip for now
        return Optional.empty();
    }

    private Optional<Response> handleRequest(Socket p_To, Message p_Message, String p_Json) {
        String addr = p_To.getLocalSocketAddress().toString();

        Optional<Request> requestOptional = JsonUtils.fromJson(p_Json, Request.class);
        if (requestOptional.isEmpty()) {
            Logger.error("Worker::handleRequest " + addr + " received invalid request json");
            return Optional.of(c_INVALID_JSON_RESPONSE);
        }
        Request request = requestOptional.get();

        switch (request.getAction()) {
            case ADD_STORE -> {
                return handleAddStoreRequest(p_To, request, p_Json);
            }
            case LIST_STORES -> {
                return handleListStoresRequest(p_To, request, p_Json);
            }
            case FILTER_STORES -> {
                return handleFilterStoresRequest(p_To, request, p_Json);
            }

            case ADD_PRODUCT -> {
                return handleAddProductRequest(p_To, request, p_Json);
            }
            case LIST_PRODUCTS -> {
                return handleListProductsRequest(p_To, request, p_Json);
            }
            case REMOVE_PRODUCT -> {
                return handleRemoveProductRequest(p_To, request, p_Json);
            }
            case DECREASE_QUANTITY -> {
                return handleDecreaseQuantityRequest(p_To, request, p_Json);
            }
            case INCREASE_QUANTITY -> {
                return handleIncreaseQuantityRequest(p_To, request, p_Json);
            }

            case SHOW_SALES_FOOD_TYPE -> {
                return handleShowSalesFoodTypeRequest(p_To, request, p_Json);
            }
            case SHOW_SALES_STORE_TYPE -> {
                return handleShowSalesStoreType(p_To, request, p_Json);
            }

            case WORKER_HANDSHAKE, REDUCER_HANDSHAKE, HEARTBEAT -> {
                return Optional.of(
                        new Response(UserAgent.WORKER, p_Message.getId(), Status.FAILURE, "Invalid request to worker"));
            }
        }

        return Optional.of(new Response(UserAgent.WORKER, p_Message.getId(), Status.FAILURE, "Unknown error occurred"));
    }

    private Optional<Response> handleAddStoreRequest(Socket p_To, Request p_Request, String p_Json) {
        String addr = p_To.getLocalSocketAddress().toString();

        Optional<AddStoreRequest> requestOptional = JsonUtils.fromJson(p_Json, AddStoreRequest.class);
        if (requestOptional.isEmpty()) {
            Logger.error("Worker::handleAddStoreRequest " + addr + " received invalid add store request json");
            return Optional.of(c_INVALID_JSON_RESPONSE);
        }
        AddStoreRequest request = requestOptional.get();

        Store store = request.getStore();
        if (store == null) {
            Logger.error("Worker::handleAddStoreRequest " + addr + " received invalid store");
            return Optional.of(new Response(UserAgent.WORKER, request.getId(), Status.FAILURE, "Invalid store"));
        }

        Optional<Store> storeNameExists = c_STORES.stream().filter(s -> s.getStoreName().equals(store.getStoreName()))
                .findAny();
        if (storeNameExists.isPresent()) {
            Logger.error("Worker::handleAddStoreRequest " + addr + " received duplicate store name");
            return Optional.of(new Response(UserAgent.WORKER, request.getId(), Status.FAILURE, "Duplicate store name"));
        }

        c_STORES.add(store);
        return Optional.of(new Response(UserAgent.WORKER, request.getId(), Status.SUCCESS, "Store added"));
    }

    private Optional<Response> handleListStoresRequest(Socket p_To, Request p_Request, String p_Json) {
        return Optional.of(new ListStoresResponse(p_Request.getId(), c_STORES));
    }

    private Optional<Response> handleFilterStoresRequest(Socket p_To, Request p_Request, String p_Json) {
        String addr = p_To.getLocalSocketAddress().toString();

        Optional<FilterStoresRequest> requestOptional = JsonUtils.fromJson(p_Json, FilterStoresRequest.class);
        if (requestOptional.isEmpty()) {
            Logger.error(
                    "Worker::handleFilterStoresRequest " + addr + " received invalid filter stores request json");
            return Optional.of(c_INVALID_JSON_RESPONSE);
        }
        FilterStoresRequest request = requestOptional.get();

        Filters filters = request.getFilters();
        if (filters == null) {
            Logger.error(
                    "Worker::handleFilterStoresRequest " + addr + " received invalid filter stores request filters");
            return Optional.of(new Response(UserAgent.WORKER, p_Request.getId(), Status.FAILURE, "Invalid filters"));
        }

        NetworkUtils.sendMessage(c_Reducer, p_Json);
        return Optional.empty();
    }

    private Optional<Response> handleAddProductRequest(Socket p_To, Request p_Request, String p_Json) {
        String addr = p_To.getLocalSocketAddress().toString();

        Optional<AddProductRequest> requestOptional = JsonUtils.fromJson(p_Json, AddProductRequest.class);
        if (requestOptional.isEmpty()) {
            Logger.error("Worker::handleAddProductRequest " + addr + " received invalid add product request json");
            return Optional.of(c_INVALID_JSON_RESPONSE);
        }
        AddProductRequest request = requestOptional.get();

        Optional<Store> storeExists = c_STORES.stream().filter(s -> s.getStoreName().equals(request.getStoreName()))
                .findAny();
        if (storeExists.isEmpty()) {
            Logger.error("Worker::handleAddProductRequest " + addr + " received invalid store name");
            return Optional
                    .of(new Response(UserAgent.WORKER, p_Request.getId(), Status.FAILURE, "Store name doesn't exist"));
        }

        Store store = storeExists.get();
        Product product = request.getProduct();

        if (product == null) {
            Logger.error("Worker::handleAdddProductRequest " + addr + " received invalid product");
            return Optional.of(new Response(UserAgent.WORKER, p_Request.getId(), Status.FAILURE, "Invalid product"));
        }

        Optional<Product> productExists = store.getProduct(product.getName());
        if (productExists.isPresent()) {
            Logger.error("Worker::handleAddProductRequest " + addr + " received duplicate product name");
            return Optional.of(
                    new Response(UserAgent.WORKER, p_Request.getId(), Status.FAILURE, "Product name already exists"));
        }

        store.addProduct(product);
        return Optional.of(new Response(UserAgent.WORKER, p_Request.getId(), Status.SUCCESS, "Product added"));
    }

    private Optional<Response> handleRemoveProductRequest(Socket p_To, Request p_Request, String p_Json) {
        String addr = p_To.getLocalSocketAddress().toString();

        Optional<RemoveProductRequest> requestOptional = JsonUtils.fromJson(p_Json, RemoveProductRequest.class);
        if (requestOptional.isEmpty()) {
            Logger.error(
                    "Worker::handleRemoveProductRequest " + addr + " received invalid remove product request json");
            return Optional.of(c_INVALID_JSON_RESPONSE);
        }
        RemoveProductRequest request = requestOptional.get();

        String storeName = request.getStoreName();
        Optional<Store> storeExists = c_STORES.stream().filter(s -> s.getStoreName().equals(storeName)).findAny();
        if (storeName == null || storeExists.isEmpty()) {
            Logger.error("Worker::handleRemoveProductRequest " + addr
                    + " received remove product request with invalid store name");
            return Optional.of(new Response(UserAgent.WORKER, p_Request.getId(), Status.FAILURE, "Invalid store name"));
        }

        Store store = storeExists.get();

        String productName = request.getProductName();
        Optional<Product> productExists = store.getProduct(productName);
        if (productName == null || productExists.isEmpty()) {
            Logger.error("Worker::handleRemoveProductRequest" + addr
                    + " received remove product request with invalid product name");
            return Optional
                    .of(new Response(UserAgent.WORKER, p_Request.getId(), Status.FAILURE, "Invalid product name"));
        }

        store.removeProduct(productName);
        return Optional.of(new Response(UserAgent.WORKER, p_Request.getId(), Status.SUCCESS, "Product removed"));
    }

    private Optional<Response> handleIncreaseQuantityRequest(Socket p_To, Request p_Request, String p_Json) {
        String addr = p_To.getLocalSocketAddress().toString();

        Optional<IncreaseQuantityRequest> requestOptional = JsonUtils.fromJson(p_Json, IncreaseQuantityRequest.class);
        if (requestOptional.isEmpty()) {
            Logger.error("Worker::handleIncreaseQuantityRequest " + addr
                    + " received invalid increase quantity request json");
            return Optional.of(c_INVALID_JSON_RESPONSE);
        }
        IncreaseQuantityRequest request = requestOptional.get();

        String storeName = request.getStoreName();
        Optional<Store> storeExists = c_STORES.stream().filter(s -> s.getStoreName().equals(storeName)).findAny();
        if (storeName == null || storeExists.isEmpty()) {
            Logger.error("Worker::handleIncreaseQuantityRequest " + addr
                    + " received increase quantity request with invalid store name");
            return Optional.of(new Response(UserAgent.WORKER, p_Request.getId(), Status.FAILURE, "Invalid store name"));
        }

        Store store = storeExists.get();

        String productName = request.getProductName();
        Optional<Product> productExists = store.getProduct(productName);
        if (productName == null || productExists.isEmpty()) {
            Logger.error("Worker::handleIncreaseQuantityRequest " + addr
                    + " received increase quantity request with invalid product name");
            return Optional
                    .of(new Response(UserAgent.WORKER, p_Request.getId(), Status.FAILURE, "Invalid product name"));
        }

        productExists.get().increaseQuantity(request.getQuantity());
        return Optional
                .of(new Response(UserAgent.WORKER, p_Request.getId(), Status.SUCCESS, "Product quantity increased"));
    }

    private Optional<Response> handleDecreaseQuantityRequest(Socket p_To, Request p_Request, String p_Json) {
        String addr = p_To.getLocalSocketAddress().toString();

        Optional<IncreaseQuantityRequest> requestOptional = JsonUtils.fromJson(p_Json, IncreaseQuantityRequest.class);
        if (requestOptional.isEmpty()) {
            Logger.error("Worker::handleDecreaseQuantityRequest " + addr
                    + " received invalid decrease quantity request json");
            return Optional.of(c_INVALID_JSON_RESPONSE);
        }
        IncreaseQuantityRequest request = requestOptional.get();

        String storeName = request.getStoreName();
        Optional<Store> storeExists = c_STORES.stream().filter(s -> s.getStoreName().equals(storeName)).findAny();
        if (storeName == null || storeExists.isEmpty()) {
            Logger.error("Worker::handleDecreaseQuantityRequest " + addr
                    + " received quantity quantity request with invalid store name");
            return Optional.of(new Response(UserAgent.WORKER, p_Request.getId(), Status.FAILURE, "Invalid store name"));
        }

        Store store = storeExists.get();

        String productName = request.getProductName();
        Optional<Product> productExists = store.getProduct(productName);
        if (productName == null || productExists.isEmpty()) {
            Logger.error("Worker::handleDecreaseQuantityRequest " + addr
                    + " received decrease quantity request with invalid product name");
            return Optional
                    .of(new Response(UserAgent.WORKER, p_Request.getId(), Status.FAILURE, "Invalid product name"));
        }

        productExists.get().decreaseQuantity(request.getQuantity());
        return Optional
                .of(new Response(UserAgent.WORKER, p_Request.getId(), Status.SUCCESS, "Product quantity decreased"));
    }

    private Optional<Response> handleListProductsRequest(Socket p_To, Request p_Request, String p_Json) {
        String addr = p_To.getLocalSocketAddress().toString();

        Optional<ListProductsRequest> requestOptional = JsonUtils.fromJson(p_Json, ListProductsRequest.class);
        if (requestOptional.isEmpty()) {
            Logger.error(
                    "Worker::handleListProductsRequest " + addr + " received invalid list products request json");
            return Optional.of(c_INVALID_JSON_RESPONSE);
        }
        ListProductsRequest request = requestOptional.get();

        String storeName = request.getStoreName();
        Optional<Store> storeExists = c_STORES.stream().filter(s -> s.getStoreName().equals(storeName)).findAny();
        if (storeName == null || storeExists.isEmpty()) {
            Logger.error(
                    "Worker::handleListProductsRequest " + addr + " received invalid list products request store name");
            return Optional.of(new Response(UserAgent.WORKER, p_Request.getId(), Status.FAILURE, "Invalid store name"));
        }

        Store store = storeExists.get();
        return Optional.of(new ListProductsResponse(p_Request.getId(), storeName, store.getProducts()));
    }

    private Optional<Response> handleShowSalesFoodTypeRequest(Socket p_To, Request p_Request, String p_Json) {
        String addr = p_To.getLocalSocketAddress().toString();

        Optional<ShowSalesFoodTypeRequest> requestOptional = JsonUtils.fromJson(p_Json, ShowSalesFoodTypeRequest.class);
        if (requestOptional.isEmpty()) {
            Logger.error("Worker::handleShowSalesFoodTypeRequest " + addr
                    + " received invalid show sales food type request json");
            return Optional.of(c_INVALID_JSON_RESPONSE);
        }
        ShowSalesFoodTypeRequest request = requestOptional.get();

        String foodType = request.getFoodType();
        if (foodType == null || foodType.isEmpty() || foodType.isBlank()) {
            Logger.error("Worker::handleShowSalesFoodTypeRequest " + addr
                    + " received invalid show sales food type request food type");
            return Optional.of(new Response(UserAgent.WORKER, p_Request.getId(), Status.FAILURE, "Invalid food type"));
        }

        NetworkUtils.sendMessage(c_Reducer, p_Json);
        return Optional.empty();
    }

    private Optional<Response> handleShowSalesStoreType(Socket p_To, Request p_Request, String p_Json) {
        String addr = p_To.getLocalSocketAddress().toString();

        Optional<ShowSalesStoreTypeRequest> requestOptional = JsonUtils.fromJson(p_Json,
                ShowSalesStoreTypeRequest.class);
        if (requestOptional.isEmpty()) {
            Logger.error("Worker::handleShowSalesStoreType" + addr
                    + " received invalid show sales store type request json");
            return Optional.of(c_INVALID_JSON_RESPONSE);
        }
        ShowSalesStoreTypeRequest request = requestOptional.get();

        String storeType = request.getStoreType();
        if (storeType == null || storeType.isEmpty() || storeType.isBlank()) {
            Logger.error("Worker::handleShowSalesStoreType" + addr
                    + " received invalid show sales store type request store type");
            return Optional.of(new Response(UserAgent.WORKER, p_Request.getId(), Status.FAILURE, "Invalid store type"));
        }

        NetworkUtils.sendMessage(c_Reducer, p_Json);
        return Optional.empty();
    }
}
