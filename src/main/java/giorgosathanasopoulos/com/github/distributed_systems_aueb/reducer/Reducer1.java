package giorgosathanasopoulos.com.github.distributed_systems_aueb.reducer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.json.JsonUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.logger.Logger;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.master.MasterConfig;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.model.Filters;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.model.Store;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.FilterStoresIntermediateRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.FilterStoresResponse;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.ListStoresIntermediateRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.ListStoresResponse;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Message;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Message.UserAgent;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.NetworkUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Request;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Response;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.ShowSalesFoodTypeIntermediateRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Response.About;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Response.Status;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.StatsResponsePayload;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.worker.WorkerConfig;

public class Reducer1 {
    private static final Response c_INVALID_JSON_RESPONSE = new Response(UserAgent.MASTER, -1, Status.FAILURE,
            "Invalid json");

    private final ServerSocket c_Reducer;

    private final Socket c_Server;
    private final Object c_SERVER_LOCK = new Object();

    private final Map<Integer, StatsResponsePayload> c_Srp;
    private final Object c_SRP_LOCK = new Object();

    public Reducer1() {
        c_Reducer = initReducer(ReducerConfig.c_PORT, ReducerConfig.c_BACKLOG);
        c_Server = connectToServer(MasterConfig.c_HOST, MasterConfig.c_PORT);
        c_Srp = new HashMap<>();
        serverLoop();
    }

    private ServerSocket initReducer(int p_Port, int p_Backlog) {
        try {
            ServerSocket server = new ServerSocket(p_Port, p_Backlog);
            Logger.info("Reducer::initServer listening on port: " + p_Port);
            return server;
        } catch (IOException e) {
            Logger.error(
                    "Master::initServer failed to create server: " + e.getLocalizedMessage());
            System.exit(1);
            return null;
        }
    }

    private Socket connectToServer(String p_Addr, int p_Port) {
        try {
            Socket socket = new Socket(p_Addr, p_Port);
            Logger.info(String.format("Reducer::connectToServer worker %s connected to server %s",
                    socket.getLocalSocketAddress(), socket.getRemoteSocketAddress()));
            return socket;
        } catch (IOException e) {
            Logger.error("Reducer::connectToServer failed to connect to server: " + e.getMessage());
            return null;
        }
    }

    private void serverLoop() {
        while (!c_Reducer.isClosed()) {
            try {
                Socket worker = c_Reducer.accept();
                Logger.info("Reducer::serverLoop acceptedClient: " + worker.getRemoteSocketAddress());
                new Thread(() -> workerThread(worker)).start();
            } catch (IOException e) {
                Logger.error("Reducer::serverLoop failed to accept client: " + e.getMessage());
            }
        }

        try {
            c_Reducer.close();
        } catch (IOException e) {
            Logger.error("Reducer::serverLoop failed to close server: " + e.getMessage());
        }
    }

    private void workerThread(Socket p_Worker) {
        if (p_Worker == null || p_Worker.isClosed()) {
            Logger.error("Reducer::workerThread started with null socket");
            return;
        }

        String addr = p_Worker.getRemoteSocketAddress().toString();

        Scanner sc;
        try {
            sc = new Scanner(p_Worker.getInputStream());
        } catch (IOException e) {
            Logger.error("Reducer::workerThread " + addr + " failed to start receiving messages from client");
            return;
        }

        while (!p_Worker.isClosed()) {
            if (!sc.hasNextLine())
                continue;

            String json = sc.nextLine().trim();
            Logger.info(
                    "Reducer::workerThread " +
                            addr +
                            " received message: " +
                            json);
            processWorkerMessage(p_Worker, json);
        }

        Logger.info("Reducer::workerThread worker" + addr + " disconnected...");
        sc.close();
    }

    private void processWorkerMessage(Socket p_Worker, String p_Json) {
        String addr = p_Worker.getRemoteSocketAddress().toString();

        if (p_Json == null || p_Json.isEmpty() || p_Json.isBlank()) {
            Logger.error("Reducer::processWorkerMessage " + addr + " received null message");
            return;
        }

        Optional<Response> response = handleMessage(p_Worker, p_Json);
        synchronized (c_SERVER_LOCK) {
            response.ifPresent(r -> NetworkUtils.sendMessage(c_Server, r));
        }
    }

    private Optional<Response> handleMessage(Socket p_Worker, String p_Json) {
        String addr = p_Worker.getRemoteSocketAddress().toString();

        Optional<Message> messageOptional = JsonUtils.fromJson(p_Json, Message.class);
        if (messageOptional.isEmpty()) {
            Logger.error("Reducer::handleMessage " + addr + " failed to parse json message");
            return Optional.of(c_INVALID_JSON_RESPONSE);
        }
        Message message = messageOptional.get();

        return switch (message.getType()) {
            case REQUEST -> handleRequest(p_Worker, p_Json);
            case RESPONSE -> handleResponse(p_Worker, p_Json);
        };
    }

    private Optional<Response> handleResponse(Socket p_Worker, String p_Json) {
        String addr = p_Worker.getRemoteSocketAddress().toString();

        Optional<Response> responseOptional = JsonUtils.fromJson(p_Json, Response.class);
        if (responseOptional.isEmpty()) {
            Logger.error("Reducer::handleResponse " + addr + " failed to parse response json");
            return Optional.of(c_INVALID_JSON_RESPONSE);
        }
        Response response = responseOptional.get();

        int id = response.getId();

        return Optional.empty();
    }

    private Optional<Response> handleRequest(Socket p_Worker, String p_Json) {
        String addr = p_Worker.getRemoteSocketAddress().toString();

        Optional<Request> requestOptional = JsonUtils.fromJson(p_Json, Request.class);
        if (requestOptional.isEmpty()) {
            Logger.error("Reducer::handleRequest " + addr + " received invalid request json");
            return Optional.of(c_INVALID_JSON_RESPONSE);
        }
        Request request = requestOptional.get();

        switch (request.getAction()) {
            case ADD_STORE,
                    ADD_PRODUCT,
                    REMOVE_PRODUCT,
                    LIST_PRODUCTS,
                    INCREASE_QUANTITY,
                    DECREASE_QUANTITY,
                    REDUCER_HANDSHAKE -> {
                return Optional.of(
                        new Response(UserAgent.REDUCER, request.getId(), Status.FAILURE, "Invalid reducer request"));
            }
            case HEARTBEAT -> {
                return Optional.empty();
            }
            case WORKER_HANDSHAKE -> {
                return Optional
                        .of(new Response(UserAgent.REDUCER, request.getId(), Status.SUCCESS, "Welcome worker " + addr));
            }
            case FILTER_STORES -> {
                int id = request.getId();

                if (c_Srp.containsKey(id)) {
                    StatsResponsePayload srp = c_Srp.get(id);
                    srp.addResponse(p_Json);
                    if (srp.isReady()) {
                        synchronized (c_SERVER_LOCK) {
                            NetworkUtils.sendMessage(srp.getSocket(),
                                    handleFilterStoresRequest(id));
                        }
                    }
                } else {
                    StatsResponsePayload srp = new StatsResponsePayload(c_Server, WorkerConfig.c_WORKER_COUNT);
                    srp.addResponse(p_Json);
                    c_Srp.put(id, srp);
                }

                return Optional.empty();
            }
            case LIST_STORES -> {
                int id = request.getId();

                if (c_Srp.containsKey(id)) {
                    StatsResponsePayload srp = c_Srp.get(id);
                    srp.addResponse(p_Json);
                    if (srp.isReady()) {
                        synchronized (c_SERVER_LOCK) {
                            NetworkUtils.sendMessage(srp.getSocket(),
                                    handleListStoresRequest(id));
                        }
                    }
                } else {
                    StatsResponsePayload srp = new StatsResponsePayload(c_Server, WorkerConfig.c_WORKER_COUNT);
                    srp.addResponse(p_Json);
                    c_Srp.put(id, srp);
                }

                return Optional.empty();
            }
            case SHOW_SALES_FOOD_TYPE -> {
                int id = request.getId();

                if (c_Srp.containsKey(id)) {
                    StatsResponsePayload srp = c_Srp.get(id);
                    srp.addResponse(p_Json);
                    if (srp.isReady()) {
                        synchronized (c_SERVER_LOCK) {
                            NetworkUtils.sendMessage(srp.getSocket(),
                                    handleShowSalesFoodTypeRequest(id));
                        }
                    }
                } else {
                    StatsResponsePayload srp = new StatsResponsePayload(c_Server, WorkerConfig.c_WORKER_COUNT);
                    srp.addResponse(p_Json);
                    c_Srp.put(id, srp);
                }

                return Optional.empty();
            }
            case SHOW_SALES_STORE_TYPE -> {
                int id = request.getId();

                if (c_Srp.containsKey(id)) {
                    StatsResponsePayload srp = c_Srp.get(id);
                    srp.addResponse(p_Json);
                    if (srp.isReady()) {
                        synchronized (c_SERVER_LOCK) {
                            NetworkUtils.sendMessage(srp.getSocket(),
                                    handleShowSalesStoreTypeRequest(id));
                        }
                    }
                } else {
                    StatsResponsePayload srp = new StatsResponsePayload(c_Server, WorkerConfig.c_WORKER_COUNT);
                    srp.addResponse(p_Json);
                    c_Srp.put(id, srp);
                }

                return Optional.empty();
            }
            default -> {
                return Optional.of(
                        new Response(UserAgent.REDUCER, request.getId(), Status.FAILURE, "Invalid reducer request"));
            }
        }
    }

    private Response handleListStoresRequest(int id) {
        StatsResponsePayload srp;
        synchronized (c_SRP_LOCK) {
            srp = c_Srp.get(id);
        }
        List<Store> stores = new ArrayList<>();

        for (String part : srp.getResponses()) {
            Optional<ListStoresIntermediateRequest> intermediateOptional = JsonUtils.fromJson(part,
                    ListStoresIntermediateRequest.class);
            if (intermediateOptional.isEmpty()) {
                return new Response(UserAgent.REDUCER, id, Status.FAILURE,
                        "One of intermediate requests contain invalid json");
            }

            ListStoresIntermediateRequest intermediate = intermediateOptional.get();
            if (intermediate == null || intermediate.getStores() == null) {
                return new Response(UserAgent.REDUCER, id, Status.FAILURE,
                        "One of intermediate requests contain invalid stores");
            }

            stores.addAll(intermediate.getStores());
        }

        return new ListStoresResponse(id, stores);
    }

    private Response handleFilterStoresRequest(int id) {
        StatsResponsePayload srp;
        synchronized (c_SRP_LOCK) {
            srp = c_Srp.get(id);
        }
        List<Store> stores = new ArrayList<>();
        Filters filters = null;

        for (String part : srp.getResponses()) {
            Optional<FilterStoresIntermediateRequest> intermediateOptional = JsonUtils.fromJson(part,
                    FilterStoresIntermediateRequest.class);
            if (intermediateOptional.isEmpty()) {
                return new Response(UserAgent.REDUCER, id, Status.FAILURE,
                        "One of intermediate requests contain invalid json");
            }

            FilterStoresIntermediateRequest intermediate = intermediateOptional.get();
            if (intermediate == null || intermediate.getStores() == null) {
                return new Response(UserAgent.REDUCER, id, Status.FAILURE,
                        "One of intermediate requests contain invalid stores");
            }

            filters = intermediate.getFilters();
            stores.addAll(intermediate.getStores());
        }
        if (filters == null)
            return new Response(UserAgent.REDUCER, id, Status.FAILURE, "Invalid filters");

        Iterator<Store> iterator = stores.iterator();
        while (iterator.hasNext()) {
            Store store = iterator.next();
            if (!filters.abides(store)) {
                iterator.remove();
            }
        }

        return new FilterStoresResponse(id, stores);
    }

    // TODO: implement handleShowSalesFoodTypeRequest
    private Response handleShowSalesFoodTypeRequest(int id) {
        StatsResponsePayload srp;
        synchronized (c_SRP_LOCK) {
            srp = c_Srp.get(id);
        }
        List<Store> stores = new ArrayList<>();
        String foodType;

        for (String part : srp.getResponses()) {
            Optional<ShowSalesFoodTypeIntermediateRequest> intermediateOptional = JsonUtils.fromJson(part,
                    ShowSalesFoodTypeIntermediateRequest.class);
            if (intermediateOptional.isEmpty())
                return new Response(UserAgent.REDUCER, id, Status.FAILURE,
                        "One of the intermediate request parts contain invalid json",
                        About.SHOW_SALES_FOOD_TYPE_REQUEST);
            ShowSalesFoodTypeIntermediateRequest intermediate = intermediateOptional.get();

            if (intermediate == null)
                return new Response(UserAgent.REDUCER, id, Status.FAILURE,
                        "One of the intermediate request parts is null", About.SHOW_SALES_FOOD_TYPE_REQUEST);

            if (intermediate.getStores() == null)
                return new Response(UserAgent.REDUCER, id, Status.FAILURE,
                        "One of the intermediate request parts contain null stores",
                        About.SHOW_SALES_FOOD_TYPE_REQUEST);
            stores.addAll(intermediate.getStores());

            if (intermediate.getFoodType() == null || intermediate.getFoodType().isBlank())
                return new Response(UserAgent.REDUCER, id, Status.FAILURE,
                        "One of the intermediate request parts contain empty food type filters",
                        About.SHOW_SALES_FOOD_TYPE_REQUEST);
            foodType = intermediate.getFoodType();
        }

        return null;
    }

    // TODO: implement handleShowSalesStoreTypeRequest
    private Response handleShowSalesStoreTypeRequest(int id) {
        return null;
    }
}
