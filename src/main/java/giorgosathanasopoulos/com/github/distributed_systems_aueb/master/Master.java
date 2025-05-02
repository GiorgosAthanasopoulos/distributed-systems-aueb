package giorgosathanasopoulos.com.github.distributed_systems_aueb.master;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Optional;
import java.util.Scanner;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.json.JsonUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.logger.Logger;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.AddProductRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.AddStoreRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.BuyProductRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.DecreaseQuantityRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.IncreaseQuantityRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.ListProductsRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Message;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Message.UserAgent;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.NetworkUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.RemoveProductRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Request;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Response;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Response.Status;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.reducer.Reducer1;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.worker.Worker1;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.worker.WorkerConfig;

public class Master {

    private static final Response c_INVALID_JSON_RESPONSE = new Response(UserAgent.MASTER, -1, Status.FAILURE,
            "Invalid json");

    private static final int c_INVALID_ACTION = -1;
    private static final int c_HANDSHAKE = -2;
    private static final int c_STATS = -3;
    private static final int c_HEARTBEAT = -4;

    private final ServerSocket c_Server;

    private final Socket[] c_Workers = new Socket[WorkerConfig.c_WORKER_COUNT];
    private int m_LastAddedWorkerIdx = 0;
    private final Object c_WORKERS_LOCK = new Object();

    @SuppressWarnings("unused")
    private Socket m_Reducer = null;
    private final Object c_REDUCER_LOCK = new Object();

    private final HashMap<Integer, Socket> c_ToSendResponses = new HashMap<>();
    private final Object c_RESPONSES_LOCK = new Object();

    public Master() {
        c_Server = initServer();
        initWorkers();
        initReducer();
        serverLoop();
    }

    private ServerSocket initServer() {
        try {
            ServerSocket server = new ServerSocket(MasterConfig.c_PORT, MasterConfig.c_BACKLOG);
            Logger.info("Master::initServer listening on port: " + MasterConfig.c_PORT);
            return server;
        } catch (IOException e) {
            Logger.error(
                    "Master::initServer failed to create server: " + e.getLocalizedMessage());
            System.exit(1);
            return null;
        }
    }

    private void initWorkers() {
        for (int i = 0; i < WorkerConfig.c_WORKER_COUNT; i++)
            new Thread(
                    Worker1::new).start();

        Logger.info(
                "Master::initReducer started worker threads");
    }

    private void initReducer() {
        new Thread(Reducer1::new).start();

        Logger.info(
                "Master::initReducer started reducer thread");
    }

    private void serverLoop() {
        while (!c_Server.isClosed())
            try {
                Socket socket = c_Server.accept();
                Logger.info("Master::serverLoop accepted client: " + socket.getRemoteSocketAddress());
                new Thread(() -> clientThread(socket)).start();
            } catch (IOException e) {
                Logger.error("Master::serverLoop failed to accept client: " + e.getLocalizedMessage());
            }

        try {
            c_Server.close();
        } catch (IOException e) {
            Logger.error("Master::serverLoop failed to close server: " + e.getLocalizedMessage());
        }
    }

    private void clientThread(Socket p_Socket) {
        if (p_Socket == null || p_Socket.isClosed()) {
            Logger.error("Master::clientThread started with null socket");
            return;
        }

        String addr = p_Socket.getRemoteSocketAddress().toString();

        Scanner sc;
        try {
            sc = new Scanner(p_Socket.getInputStream());
        } catch (IOException e) {
            Logger.error("Master::clientThread " + addr + " failed to start receiving messages from client");
            return;
        }

        while (!p_Socket.isClosed()) {
            if (!sc.hasNextLine())
                continue;

            String json = sc.nextLine().trim();
            Logger.info(
                    "Master::clientThread " +
                            addr +
                            " received message: " +
                            json);
            new Thread(() -> processClientMessage(p_Socket, json)).start();
            ;
        }

        Logger.info("Master::clientThread client " + addr + " disconnected...");
        sc.close();
    }

    private void processClientMessage(Socket p_Socket, String p_Json) {
        if (p_Socket == null || p_Socket.isClosed()) {
            Logger.error("Master::processClientMessage received message from null client");
            return;
        }

        String addr = p_Socket.getRemoteSocketAddress().toString();

        if (p_Json == null || p_Json.isBlank() || p_Json.isEmpty()) {
            Logger.error("Master::processClientMessage " + addr + " received null client message");
            return;
        }

        Optional<Response> response = handleMessage(p_Socket, p_Json);
        response.ifPresent(r -> NetworkUtils.sendMessage(p_Socket, r));
    }

    private Optional<Response> handleMessage(Socket p_Socket, String p_Json) {
        String addr = p_Socket.getRemoteSocketAddress().toString();

        Optional<Message> messageOptional = JsonUtils.fromJson(p_Json, Message.class);
        if (messageOptional.isEmpty()) {
            Logger.error("Master::handleMessage " + addr + " failed to parse json message");
            return Optional.of(c_INVALID_JSON_RESPONSE);
        }
        Message message = messageOptional.get();

        return switch (message.getType()) {
            case REQUEST -> handleRequest(p_Socket, p_Json);
            case RESPONSE -> handleResponse(p_Socket, p_Json);
        };
    }

    private Optional<Response> handleResponse(Socket p_Socket, String p_Json) {
        String addr = p_Socket.getRemoteSocketAddress().toString();

        Optional<Response> responseOptional = JsonUtils.fromJson(p_Json,
                Response.class);
        if (responseOptional.isEmpty()) {
            Logger.error("Master::handleResponse " + addr + " failed to parse response json");
            return Optional.of(c_INVALID_JSON_RESPONSE);
        }
        Response response = responseOptional.get();

        int id = response.getId();
        synchronized (c_RESPONSES_LOCK) {
            if (c_ToSendResponses.containsKey(id)) {
                NetworkUtils.sendMessage(c_ToSendResponses.get(id), p_Json);
                c_ToSendResponses.remove(id);
            }
        }

        return Optional.empty();
    }

    private Optional<Response> handleRequest(Socket p_Socket, String p_Json) {
        String addr = p_Socket.getRemoteSocketAddress().toString();

        Optional<Request> requestOptional = JsonUtils.fromJson(p_Json, Request.class);
        if (requestOptional.isEmpty()) {
            Logger.error("Master::handleRequest " + addr + " failed to parse json");
            return Optional.of(c_INVALID_JSON_RESPONSE);
        }
        Request request = requestOptional.get();

        int workerId = getWorkerId(request, p_Json);
        switch (workerId) {
            case c_INVALID_ACTION -> {
                return Optional
                        .of(new Response(UserAgent.MASTER, request.getId(), Status.FAILURE,
                                "Invalid action"));
            }
            case c_HANDSHAKE -> {
                return handleHandshake(p_Socket, request);
            }
            case c_STATS -> {
                synchronized (c_RESPONSES_LOCK) {
                    c_ToSendResponses.put(request.getId(), p_Socket);
                }
                return handleStats(p_Socket, request, p_Json);
            }
            case c_HEARTBEAT -> {
                return Optional.empty();
            }
            default -> {
                synchronized (c_RESPONSES_LOCK) {
                    c_ToSendResponses.put(request.getId(), p_Socket);
                }
                return forwardRequestToWorker(p_Socket, workerId, request, p_Json);
            }
        }
    }

    private Optional<Response> handleHandshake(Socket p_Socket, Request p_Request) {
        String addr = p_Socket.getRemoteSocketAddress().toString();

        switch (p_Request.getUserAgent()) {
            case WORKER -> {
                synchronized (c_WORKERS_LOCK) {
                    if (m_LastAddedWorkerIdx < c_Workers.length)
                        c_Workers[m_LastAddedWorkerIdx++] = p_Socket;
                }
                return Optional
                        .of(new Response(UserAgent.MASTER, p_Request.getId(), Status.SUCCESS,
                                "Welcome worker " + addr));
            }
            case REDUCER -> {
                synchronized (c_REDUCER_LOCK) {
                    m_Reducer = p_Socket;
                }
                return Optional
                        .of(new Response(UserAgent.MASTER, p_Request.getId(), Status.SUCCESS,
                                "Welcome reducer " + addr));
            }
            default -> {
                return Optional.of(new Response(UserAgent.MASTER, p_Request.getId(), Status.FAILURE,
                        "Invalid handshake from " + addr));
            }
        }
    }

    private int getWorkerId(Request p_Request, String p_Json) {
        switch (p_Request.getAction()) {
            case ADD_PRODUCT, ADD_STORE, DECREASE_QUANTITY, INCREASE_QUANTITY, REMOVE_PRODUCT, LIST_PRODUCTS,
                    BUY_PRODUCT -> {
                Optional<String> storeName = getStoreNameFromRequest(p_Request, p_Json);
                if (storeName.isEmpty()) {
                    Logger.error("Master::getWorkerId store name from request is invalid");
                    return c_INVALID_ACTION;
                }

                int hash = hash(storeName.get());
                synchronized (c_WORKERS_LOCK) {
                    return Math.abs(hash) % c_Workers.length;
                }
            }
            case SHOW_SALES_FOOD_TYPE, SHOW_SALES_STORE_TYPE, LIST_STORES, FILTER_STORES -> {
                return c_STATS;
            }
            case WORKER_HANDSHAKE, REDUCER_HANDSHAKE -> {
                return c_HANDSHAKE;
            }
            case HEARTBEAT -> {
                return c_HEARTBEAT;
            }
            default -> {
                return c_INVALID_ACTION;
            }
        }
    }

    private Optional<String> getStoreNameFromRequest(Request p_Request, String p_Json) {
        switch (p_Request.getAction()) {
            case ADD_STORE:
                Optional<AddStoreRequest> addStoreRequest = JsonUtils.fromJson(p_Json, AddStoreRequest.class);
                if (addStoreRequest.isEmpty()) {
                    Logger.error("Master::getStoreNameFromRequest  failed to parse add store request json");
                    return Optional.empty();
                }
                return Optional.of(addStoreRequest.get().getStore().getStoreName());
            case LIST_STORES:
            case FILTER_STORES:
                return Optional.empty();

            case BUY_PRODUCT:
                Optional<BuyProductRequest> buyProductRequest = JsonUtils.fromJson(p_Json, BuyProductRequest.class);
                if (buyProductRequest.isEmpty()) {
                    Logger.error("Master::getStoreNameFromRequest failed to parse buy product request json");
                    return Optional.empty();
                }
                return Optional.of(buyProductRequest.get().getStoreName());
            case ADD_PRODUCT:
                Optional<AddProductRequest> addProductRequest = JsonUtils.fromJson(p_Json, AddProductRequest.class);
                if (addProductRequest.isEmpty()) {
                    Logger.error("Master::getStoreNameFromRequest failed to parse add product request json");
                    return Optional.empty();
                }
                return Optional.of(addProductRequest.get().getStoreName());
            case LIST_PRODUCTS:
                Optional<ListProductsRequest> listProductsRequest = JsonUtils.fromJson(p_Json,
                        ListProductsRequest.class);
                if (listProductsRequest.isEmpty()) {
                    Logger.error("Master::getStoreNameFromRequest failed to parse list products json");
                    return Optional.empty();
                }
                return Optional.of(listProductsRequest.get().getStoreName());
            case REMOVE_PRODUCT:
                Optional<RemoveProductRequest> removeProductRequest = JsonUtils.fromJson(p_Json,
                        RemoveProductRequest.class);
                if (removeProductRequest.isEmpty()) {
                    Logger.error("Master::getStoreNameFromRequest  failed to parse remove product json");
                    return Optional.empty();
                }
                return Optional.of(removeProductRequest.get().getStoreName());
            case DECREASE_QUANTITY:
                Optional<DecreaseQuantityRequest> decreaseQuantityRequest = JsonUtils.fromJson(p_Json,
                        DecreaseQuantityRequest.class);
                if (decreaseQuantityRequest.isEmpty()) {
                    Logger
                            .error("Master::getStoreNameFromRequest  failed to parse decrease quantity request json");
                    return Optional.empty();
                }
                return Optional.of(decreaseQuantityRequest.get().getStoreName());
            case INCREASE_QUANTITY:
                Optional<IncreaseQuantityRequest> increaseQuantityRequest = JsonUtils.fromJson(p_Json,
                        IncreaseQuantityRequest.class);
                if (increaseQuantityRequest.isEmpty()) {
                    Logger.error("Master::getStoreNameFromRequest failed to parse increase quantity request json");
                    return Optional.empty();
                }
                return Optional.of(increaseQuantityRequest.get().getStoreName());

            case SHOW_SALES_FOOD_TYPE:
            case SHOW_SALES_STORE_TYPE:
                return Optional.empty();

            case REDUCER_HANDSHAKE:
            case WORKER_HANDSHAKE:
                return Optional.empty();

            default:
                return Optional.empty();
        }
    }

    private Optional<Response> forwardRequestToWorker(Socket p_Socket, int p_WorkerId, Request p_Request,
            String p_Json) {
        String addr = p_Socket.getRemoteSocketAddress().toString();

        if (!isValidWorkerId(p_WorkerId)) {
            Logger
                    .error("Master::forwardRequestToWorker " + addr + " called with invalid worker id: " + p_WorkerId);
            return Optional.of(new Response(UserAgent.MASTER, p_Request.getId(), Status.FAILURE,
                    "Invalid worker id requested from master"));
        }

        synchronized (c_WORKERS_LOCK) {
            NetworkUtils.sendMessage(c_Workers[p_WorkerId], p_Json);
        }
        return Optional.empty();
    }

    private boolean isValidWorkerId(int p_WorkerId) {
        synchronized (c_WORKERS_LOCK) {
            return p_WorkerId >= 0 && p_WorkerId < c_Workers.length;
        }
    }

    private int hash(String p_StoreName) {
        return p_StoreName != null ? p_StoreName.hashCode() : 0;
    }

    private Optional<Response> handleStats(Socket p_Socket, Request p_Request, String p_Json) {
        for (int i = 0; i < m_LastAddedWorkerIdx; ++i) {
            synchronized (c_WORKERS_LOCK) {
                if (!NetworkUtils.sendMessage(c_Workers[i], p_Json)) {
                    Logger.error("Master::handleStats " + p_Socket.getRemoteSocketAddress()
                            + " unable to send requests to workers");
                    return Optional.of(new Response(UserAgent.MASTER, p_Request.getId(), Status.FAILURE,
                            "Unable to forward stats request to workers"));
                }
            }
        }

        return Optional.empty();
    }

}
