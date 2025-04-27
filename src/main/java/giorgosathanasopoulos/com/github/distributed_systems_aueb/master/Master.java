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
import giorgosathanasopoulos.com.github.distributed_systems_aueb.reducer.Reducer;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.uid.UID;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.worker.Worker;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.worker.WorkerConfig;

public class Master {

    private static final Response c_INVALID_JSON_RESPONSE = new Response(UserAgent.MASTER, -1, Status.FAILURE,
            "Invalid json");

    private static final int c_INVALID_ACTION = -1;
    private static final int c_HANDSHAKE = -2;
    private static final int c_STATS = -3;

    private final ServerSocket c_Server;

    private final Socket[] c_Workers = new Socket[WorkerConfig.c_WORKER_COUNT];
    private int m_LastAddedWorkerIdx = 0;
    private final Object c_WORKERS_LOCK = new Object();

    @SuppressWarnings("unused")
    private Socket m_Reducer = null;
    private final Object c_REDUCER_LOCK = new Object();

    private final HashMap<Integer, Socket> c_ToSendResponses = new HashMap<>();

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
                    Worker::new).start();

        Logger.info(
                "Master::initReducer started worker threads");
    }

    private void initReducer() {
        new Thread(Reducer::new).start();

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
        if (p_Socket == null) {
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
        }

        Logger.info("Master::clientThread client " + addr + " disconnected...");
        sc.close();
    }

    private void processClientMessage(Socket p_Socket, String p_Json) {
        if (p_Socket == null) {
            Logger.error("Master::processClientMessage received client message from null client");
            return;
        }

        String addr = p_Socket.getRemoteSocketAddress().toString();

        if (p_Json == null) {
            Logger.error("Master::processClientMessage " + addr + " received null client message");
            return;
        }

        Optional<Response> response = handleMessage(p_Socket, p_Json);
        response.ifPresent(r -> NetworkUtils.sendMessage(p_Socket, r));
    }

    private Optional<Response> handleMessage(Socket p_Socket, String p_Json) {
        Optional<Response> empty = Optional.empty();

        if (p_Socket == null) {
            Logger.error("Master::handleMessage received message from null socket");
            return empty;
        }

        String addr = p_Socket.getRemoteSocketAddress().toString();

        if (p_Json == null) {
            Logger.error("Master::handleMessage " + addr + " received null message");
            return empty;
        }
        Message message = JsonUtils.fromJson(p_Json, Message.class);
        if (message == null) {
            Logger.error("Master::handleMessage " + addr + " failed to parse json message");
            return Optional.of(c_INVALID_JSON_RESPONSE);
        }

        return switch (message.getType()) {
            case REQUEST -> handleRequest(p_Socket, p_Json);
            case RESPONSE -> handleResponse(p_Socket, p_Json);
            default -> invalidMessageResponse(message);
        };
    }

    private Optional<Response> invalidMessageResponse(Message p_Message) {
        if (p_Message == null) {
            Logger.error("Master::invalidMessageResponse called with null response");
            return Optional.empty();
        }

        Logger.error("Master::invalidMessageResponse invalid message type");
        return Optional
                .of(new Response(UserAgent.MASTER, p_Message.getId(), Status.FAILURE,
                        "Invalid message type"));
    }

    private Optional<Response> handleResponse(Socket p_Socket, String p_Json) {
        Optional<Response> empty = Optional.empty();

        if (p_Socket == null) {
            Logger.error("Master::handleResponse received response from null socket");
            return empty;
        }

        String addr = p_Socket.getRemoteSocketAddress().toString();

        if (p_Json == null) {
            Logger.error("Master::handleResponse " + addr + " received null request");
            return empty;
        }

        Response response = JsonUtils.fromJson(p_Json, Response.class);
        if (response == null) {
            Logger.error("Master::handleResponse " + addr + " failed to parse response json");
            return Optional.of(c_INVALID_JSON_RESPONSE);
        }

        int id = response.getId();

        // logResponseStatus(response);
        if (c_ToSendResponses.containsKey(id)) {
            NetworkUtils.sendMessage(c_ToSendResponses.get(id), p_Json);
            c_ToSendResponses.remove(id);
        }

        return empty;
    }

    // private void logResponseStatus(Response response) {
    // switch (response.getStatus()) {
    // case SUCCESS -> Logger.info("Master::logResponseStatus received successful
    // response: " +
    // response.getMessage());
    // case FAILURE -> Logger.error("Master::logResponseStatus received unsuccessful
    // response: " +
    // response.getMessage());
    // }
    // }

    private Optional<Response> handleRequest(Socket p_Socket, String p_Json) {
        Optional<Response> empty = Optional.empty();

        if (p_Socket == null) {
            Logger.error("Master::handleRequest received request from null socket");
            return empty;
        }

        String addr = p_Socket.getRemoteSocketAddress().toString();

        if (p_Json == null) {
            Logger.error("Master::handleRequest " + addr + " received null request");
            return empty;
        }

        Request request = JsonUtils.fromJson(p_Json, Request.class);
        if (request == null) {
            Logger.error("Master::handleRequest " + addr + " failed to parse json");
            return Optional.of(c_INVALID_JSON_RESPONSE);
        }

        if (request.getUserAgent() == UserAgent.CLIENT)
            request.setId(UID.getNextUID());

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
                c_ToSendResponses.put(request.getId(), p_Socket);
                return handleStats(p_Socket, request, p_Json);
            }
            default -> {
                c_ToSendResponses.put(request.getId(), p_Socket);
                return forwardRequestToWorker(p_Socket, workerId, request, p_Json);
            }
        }

    }

    private Optional<Response> handleHandshake(Socket p_Socket, Request p_Request) {
        Optional<Response> empty = Optional.empty();

        if (p_Socket == null) {
            Logger.error("Master::handleHandshake received handshake from null socket");
            return empty;
        }

        String addr = p_Socket.getRemoteSocketAddress().toString();

        if (p_Request == null) {
            Logger.error("Master::handleHandshake " + addr + " received null request");
            return empty;
        }

        switch (p_Request.getUserAgent()) {
            case WORKER -> {
                synchronized (c_WORKERS_LOCK) {
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
        if (p_Request == null) {
            Logger.error("Master::getWorkerId received null request");
            return c_INVALID_ACTION;
        }

        if (p_Json == null) {
            Logger.error("Master::getWorkerId received null json message");
            return c_INVALID_ACTION;
        }

        switch (p_Request.getAction()) {
            case ADD_PRODUCT, ADD_STORE, DECREASE_QUANTITY, INCREASE_QUANTITY, REMOVE_PRODUCT, LIST_PRODUCTS -> {
                Optional<String> storeName = getStoreNameFromRequest(p_Request, p_Json);

                if (storeName.isEmpty()) {
                    Logger.error("Master::getWorkerId store name from request is invalid");
                    return c_INVALID_ACTION;
                }

                int hash = hash(storeName.get());
                return Math.abs(hash) % c_Workers.length;
            }
            case SHOW_SALES_FOOD_TYPE, SHOW_SALES_STORE_TYPE, LIST_STORES, FILTER_STORES -> {
                return c_STATS;
            }
            case WORKER_HANDSHAKE, REDUCER_HANDSHAKE -> {
                return c_HANDSHAKE;
            }
            default -> {
                return c_INVALID_ACTION;
            }
        }
    }

    private Optional<String> getStoreNameFromRequest(Request p_Request, String p_Json) {
        Optional<String> empty = Optional.empty();

        if (p_Request == null) {
            Logger.error("Master::getStoreNameFromRequest called with null request");
            return empty;
        }

        if (p_Json == null) {
            Logger.error("Master::getStoreNameFromRequest called with null json");
            return empty;
        }

        switch (p_Request.getAction()) {
            case ADD_STORE:
                AddStoreRequest addStoreRequest = JsonUtils.fromJson(p_Json, AddStoreRequest.class);
                if (addStoreRequest == null) {
                    Logger.error("Master::getStoreNameFromRequest  failed to parse add store request json");
                    return empty;
                }
                return Optional.of(addStoreRequest.getStore().getStoreName());
            case LIST_STORES:
            case FILTER_STORES:
                return empty;

            case ADD_PRODUCT:
                AddProductRequest addProductRequest = JsonUtils.fromJson(p_Json, AddProductRequest.class);
                if (addProductRequest == null) {
                    Logger.error("Master::getStoreNameFromRequest failed to parse add product request json");
                    return empty;
                }
                return Optional.of(addProductRequest.getStoreName());
            case LIST_PRODUCTS:
                ListProductsRequest listProductsRequest = JsonUtils.fromJson(p_Json, ListProductsRequest.class);
                if (listProductsRequest == null) {
                    Logger.error("Master::getStoreNameFromRequest failed to parse list products json");
                    return empty;
                }
                return Optional.of(listProductsRequest.getStoreName());
            case REMOVE_PRODUCT:
                RemoveProductRequest removeProductRequest = JsonUtils.fromJson(p_Json, RemoveProductRequest.class);
                if (removeProductRequest == null) {
                    Logger.error("Master::getStoreNameFromRequest  failed to parse remove product json");
                    return empty;
                }
                return Optional.of(removeProductRequest.getStoreName());
            case DECREASE_QUANTITY:
                DecreaseQuantityRequest decreaseQuantityRequest = JsonUtils.fromJson(p_Json,
                        DecreaseQuantityRequest.class);
                if (decreaseQuantityRequest == null) {
                    Logger.error("Master::getStoreNameFromRequest  failed to parse decrease quantity request json");
                    return empty;
                }
                return Optional.of(decreaseQuantityRequest.getStoreName());
            case INCREASE_QUANTITY:
                IncreaseQuantityRequest increaseQuantityRequest = JsonUtils.fromJson(p_Json,
                        IncreaseQuantityRequest.class);
                if (increaseQuantityRequest == null) {
                    Logger.error("Master::getStoreNameFromRequest failed to parse increase quantity request json");
                    return empty;
                }
                return Optional.of(increaseQuantityRequest.getStoreName());

            case SHOW_SALES_FOOD_TYPE:
            case SHOW_SALES_STORE_TYPE:
                return empty;

            case REDUCER_HANDSHAKE:
            case WORKER_HANDSHAKE:
                return empty;

            default:
                return empty;
        }
    }

    private Optional<Response> forwardRequestToWorker(Socket p_Socket, int p_WorkerId, Request p_Request,
            String p_Json) {
        Optional<Response> empty = Optional.empty();

        if (p_Socket == null) {
            Logger.error("Master::forwardRequestToWorker called with null receiver");
            return empty;
        }

        String addr = p_Socket.getRemoteSocketAddress().toString();

        if (!isValidWorkerId(p_WorkerId)) {
            Logger.error("Master::forwardRequestToWorker " + addr + " called with invalid worker id: " + p_WorkerId);
            return empty;
        }

        if (p_Request == null) {
            Logger.error("Master::forwardRequestToWorker " + addr + " called with null request");
            return empty;
        }

        NetworkUtils.sendMessage(c_Workers[p_WorkerId], p_Json);
        return Optional.empty();
    }

    private boolean isValidWorkerId(int p_WorkerId) {
        return p_WorkerId >= 0 && p_WorkerId < c_Workers.length;
    }

    private int hash(String p_StoreName) {
        return p_StoreName != null ? p_StoreName.hashCode() : 0;
    }

    // SHOW_SALES_FOOD_TYPE, SHOW_SALES_STORE_TYPE, LIST_STORES, FILTER_STORES
    private Optional<Response> handleStats(Socket p_Socket, Request p_Request, String p_Json) {
        switch (p_Request.getAction()) {
            case SHOW_SALES_FOOD_TYPE -> {
            }
            case SHOW_SALES_STORE_TYPE -> {
            }
            case LIST_STORES -> {
            }
            case FILTER_STORES -> {
            }
            default -> {
            }
        }
        return Optional.empty();
    }
}
