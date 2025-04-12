package giorgosathanasopoulos.com.github.distributed_systems_aueb.master;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Optional;
import java.util.Scanner;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.worker.WorkerConfig;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.json.JsonUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.logger.Logger;
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
import giorgosathanasopoulos.com.github.distributed_systems_aueb.reducer.Reducer1;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.worker.Worker1;

public class Master {

    private static final Response c_INVALID_JSON_RESPONSE = new Response(UserAgent.MASTER, -1, "", Status.FAILURE,
            "Invalid json");

    private static final int c_INVALID_ACTION = -1;
    private static final int c_HANDSHAKE = -2;
    private static final int c_STATS = -3;

    private final ServerSocket c_Server;

    private final Socket[] c_Workers = new Socket[WorkerConfig.c_WORKER_COUNT];
    private int m_LastAddedWorkerIdx = 0;
    private final Object c_WORKERS_LOCK = new Object();

    @SuppressWarnings("unused")
    private Socket m_Reducer;
    private final Object c_REDUCER_LOCK = new Object();

    private final HashMap<Integer, Socket> c_ToSendResponses = new HashMap<>();

    public Master() {
        c_Server = initServer(0);
        initWorkers();
        initReducer();
        serverLoop();
    }

    private ServerSocket initServer(int p_Count) {
        if (p_Count > MasterConfig.c_MAX_INIT_RETRY_ATTEMPTS) {
            Logger.error("Master::initServer reached max amount of init retries. Exiting...");
            System.exit(1);
        }

        try {
            ServerSocket server = new ServerSocket(MasterConfig.c_PORT, MasterConfig.c_BACKLOG);
            Logger.info("Master::initServer listening on port: " + MasterConfig.c_PORT);
            return server;
        } catch (IOException e) {
            Logger.error(
                    "Master::initServer failed to create server: " + e.getLocalizedMessage());
            return initServer(p_Count++);
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
        String sockAddr = p_Socket.getRemoteSocketAddress().toString();

        try (Scanner sc = new Scanner(p_Socket.getInputStream())) {
            while (!p_Socket.isClosed()) {
                if (!sc.hasNextLine())
                    continue;

                String json = sc.nextLine();
                Logger.info(
                        "Master::clientThread " +
                                sockAddr +
                                " received message: " +
                                json);

                new Thread(() -> processClientMessage(p_Socket, json)).start();
            }
        } catch (IOException e) {
            Logger.error(
                    "Master::clientThread client " + sockAddr + " failed to read message: " + e.getLocalizedMessage());
        } finally {
            Logger.info(
                    "Master::clientThread client " +
                            sockAddr +
                            " disconnected");
        }
    }

    private void processClientMessage(Socket p_Socket, String p_Json) {
        Optional<Response> response = handleMessage(p_Socket, p_Json);
        response.ifPresent(r -> sendResponse(p_Socket, r));
    }

    private void sendResponse(Socket p_Socket, Response p_Response) {
        String addr = p_Socket.getRemoteSocketAddress().toString();

        try {
            String json = JsonUtils.toJson(p_Response);
            synchronized (p_Socket.getOutputStream()) {
                p_Socket.getOutputStream().write((json + "\n").getBytes());
            }
        } catch (IOException e) {
            Logger.error(
                    "Master::sendResponse " + addr + " failed to send response to client: " + e.getLocalizedMessage());
        }
    }

    private Optional<Response> handleMessage(Socket p_Socket, String p_Json) {
        Message message = JsonUtils.fromJson(p_Json, Message.class);
        if (message == null) {
            Logger.error("Master::handleMessage failed to parse json message");
            return Optional.of(c_INVALID_JSON_RESPONSE);
        }

        return switch (message.getType()) {
            case REQUEST -> handleRequest(p_Socket, p_Json);
            case RESPONSE -> handleResponse(p_Socket, p_Json);
            default -> invalidMessageResponse(message);
        };
    }

    private Optional<Response> invalidMessageResponse(Message p_Message) {
        Logger.error("Master::invalidMessageResponse invalid message type");
        return Optional
                .of(new Response(UserAgent.MASTER, p_Message.getId(), JsonUtils.toJson(p_Message), Status.FAILURE,
                        "Invalid message type"));
    }

    private Optional<Response> handleResponse(Socket p_Socket, String p_Json) {
        String addr = p_Socket.getRemoteSocketAddress().toString();

        Response response = JsonUtils.fromJson(p_Json, Response.class);
        if (response == null) {
            Logger.error("Master::handleResponse " + addr + " failed to parse response json");
            return Optional.of(c_INVALID_JSON_RESPONSE);
        }
        int id = response.getId();

        // logResponseStatus(response);
        if (c_ToSendResponses.containsKey(id)) {
            try {
                Socket client = c_ToSendResponses.get(id);
                synchronized (client.getOutputStream()) {
                    client.getOutputStream().write((p_Json + "\n").getBytes());
                }
                c_ToSendResponses.remove(id);
            } catch (IOException e) {
                Logger.error("Master::handleResponse " + addr + " failed to send response back to client: "
                        + e.getLocalizedMessage());
                return Optional.of(new Response(UserAgent.MASTER, id, p_Json, Status.FAILURE,
                        "Failed to send worker response back to client: " + e.getLocalizedMessage()));
            }
        }
        return Optional.empty();
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
        String addr = p_Socket.getRemoteSocketAddress().toString();

        Request request = JsonUtils.fromJson(p_Json, Request.class);
        if (request == null) {
            Logger.error("Master::handleRequest " + addr + " failed to parse json");
            return Optional.of(c_INVALID_JSON_RESPONSE);
        }

        int workerId = getWorkerId(request, p_Json);
        switch (workerId) {
            case c_INVALID_ACTION -> {
                return Optional
                        .of(new Response(UserAgent.MASTER, request.getId(), p_Json, Status.FAILURE,
                                "Invalid action"));
            }
            case c_HANDSHAKE -> {
                return handleHandshake(p_Socket, request);
            }
            case c_STATS -> {
                return handleStats(p_Socket, request, p_Json);
            }
            default -> {
            }
        }

        c_ToSendResponses.put(request.getId(), p_Socket);
        return forwardRequestToWorker(p_Socket, workerId, request, p_Json);
    }

    private Optional<Response> handleHandshake(Socket p_Socket, Request p_Request) {
        switch (p_Request.getUserAgent()) {
            case WORKER -> {
                synchronized (c_WORKERS_LOCK) {
                    c_Workers[m_LastAddedWorkerIdx++] = p_Socket;
                }
                return Optional
                        .of(new Response(UserAgent.MASTER, p_Request.getId(), p_Request.getSrc(), Status.SUCCESS,
                                "Welcome worker"));
            }
            case REDUCER -> {
                synchronized (c_REDUCER_LOCK) {
                    m_Reducer = p_Socket;
                }
                return Optional
                        .of(new Response(UserAgent.MASTER, p_Request.getId(), p_Request.getSrc(), Status.SUCCESS,
                                "Welcome reducer"));
            }
            default -> {
                return Optional.of(new Response(UserAgent.MASTER, p_Request.getId(), "", Status.FAILURE,
                        "Invalid handshake"));
            }
        }
    }

    private int getWorkerId(Request p_Request, String p_Json) {
        switch (p_Request.getAction()) {
            case ADD_PRODUCT, ADD_STORE, DECREASE_QUANTITY, INCREASE_QUANTITY, REMOVE_PRODUCT, LIST_PRODUCTS -> {
                int hash = hash(getStoreNameFromRequest(p_Request, p_Json));
                return Math.abs(hash) % c_Workers.length;
            }
            case SHOW_SALES_FOOD_TYPE, SHOW_SALES_STORE_TYPE, LIST_STORES, FILTER_STORES -> {
                return c_STATS;
            }
            case WORKER_HANDSHAKE, REDUCER_HANDSHAKE -> {
                return c_HANDSHAKE; // Handle special cases directly
            }
            default -> {
                return c_INVALID_ACTION; // Invalid action
            }
        }
    }

    private String getStoreNameFromRequest(Request p_Request, String p_Json) {
        switch (p_Request.getAction()) {
            case ADD_PRODUCT:
                AddProductRequest addProductRequest = JsonUtils.fromJson(p_Json, AddProductRequest.class);
                if (addProductRequest == null) {
                    Logger.error("Master::getStoreNameFromRequest failed to parse add product request json");
                    return "";
                }
                return addProductRequest.getStoreName();
            case ADD_STORE:
                AddStoreRequest addStoreRequest = JsonUtils.fromJson(p_Json, AddStoreRequest.class);
                if (addStoreRequest == null) {
                    Logger.error("Master::getStoreNameFromRequest  failed to parse add store request json");
                    return "";
                }
                return addStoreRequest.getStore().getStoreName();
            case DECREASE_QUANTITY:
                DecreaseQuantityRequest decreaseQuantityRequest = JsonUtils.fromJson(p_Json,
                        DecreaseQuantityRequest.class);
                if (decreaseQuantityRequest == null) {
                    Logger.error("Master::getStoreNameFromRequest  failed to parse decrease quantity request json");
                    return "";
                }
                return decreaseQuantityRequest.getStoreName();
            case INCREASE_QUANTITY:
                IncreaseQuantityRequest increaseQuantityRequest = JsonUtils.fromJson(p_Json,
                        IncreaseQuantityRequest.class);
                if (increaseQuantityRequest == null) {
                    Logger.error("Master::getStoreNameFromRequest failed to parse increase quantity request json");
                    return "";
                }
                return increaseQuantityRequest.getStoreName();
            case REMOVE_PRODUCT:
                RemoveProductRequest removeProductRequest = JsonUtils.fromJson(p_Json, RemoveProductRequest.class);
                if (removeProductRequest == null) {
                    Logger.error("Master::getStoreNameFromRequest  failed to parse remove product json");
                    return "";
                }
                return removeProductRequest.getStoreName();
            case SHOW_SALES_FOOD_TYPE:
            case SHOW_SALES_STORE_TYPE:
            case LIST_STORES:
            case LIST_PRODUCTS:
                ListProductsRequest listProductsRequest = JsonUtils.fromJson(p_Json, ListProductsRequest.class);
                if (listProductsRequest == null) {
                    Logger.error("Master::getStoreNameFromRequest failed to parse list products json");
                    return "";
                }
                return listProductsRequest.getStoreName();
            case REDUCER_HANDSHAKE:
            case WORKER_HANDSHAKE:
            default:
                return "";
        }
    }

    private Optional<Response> forwardRequestToWorker(Socket p_Socket, int p_WorkerId, Request p_Request,
            String p_Json) {
        String addr = p_Socket.getRemoteSocketAddress().toString();

        if (!isValidWorkerId(p_WorkerId))
            return Optional
                    .of(new Response(UserAgent.MASTER, p_Request.getId(), p_Json, Status.FAILURE,
                            "Invalid worker ID"));

        try {
            synchronized (c_WORKERS_LOCK) {
                c_Workers[p_WorkerId].getOutputStream().write((p_Json + "\n").getBytes());
            }
            return Optional.empty();
        } catch (IOException e) {
            Logger.error("Master::forwardRequestToWorker " + addr + " failed to forward message to worker: "
                    + e.getLocalizedMessage());
            return Optional.of(new Response(UserAgent.MASTER, p_Request.getId(), p_Json, Status.FAILURE,
                    "Failed to forward message to worker: " + e.getLocalizedMessage()));
        }
    }

    private boolean isValidWorkerId(int p_WorkerId) {
        return p_WorkerId >= 0 && p_WorkerId < c_Workers.length;
    }

    private int hash(String p_StoreName) {
        return p_StoreName != null ? p_StoreName.hashCode() : 0;
    }

    private Optional<Response> handleStats(Socket p_Socket, Request p_Request, String p_Json) {
        // TODO:
        return Optional.empty();
    }
}
