package giorgosathanasopoulos.com.github.distributed_systems_aueb.reducer;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.json.JSONArray;
import org.json.JSONObject;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.json.JsonUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.logger.Logger;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.*;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Message.UserAgent;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Response.Status;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.uid.UID;

public class Reducer implements AutoCloseable {
    private Socket masterSocket;
    private PrintWriter out;
    private BufferedReader in;
    private final Object connectionLock = new Object();

    // Map/Reduce data structures
    private final ConcurrentHashMap<String, Map<String, Object>> intermediateResults = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, MapReduceResult> finalResults = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();

    private static final class MapReduceResult {
        private static final long RESULT_TTL_MS = TimeUnit.MINUTES.toMillis(30);
        final JSONObject aggregatedData;
        final long timestamp;

        public MapReduceResult(JSONObject data) {
            this.aggregatedData = data;
            this.timestamp = System.currentTimeMillis();
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > RESULT_TTL_MS;
        }
    }

    public Reducer() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
        connectToMaster();
        performHandshake();
        startListening();
        startHeartbeat();
        startCleanupTask();
    }

    private void startCleanupTask() {
        cleanupExecutor.scheduleAtFixedRate(() -> {
            finalResults.entrySet().removeIf(entry -> entry.getValue().isExpired());
        }, 1, 1, TimeUnit.HOURS);
    }

    private void connectToMaster() {
        int attempts = 0;
        while (attempts < ReducerConfig.c_REDUCER_RECONNECT_ATTEMPTS) {
            try {
                synchronized (connectionLock) {
                    masterSocket = new Socket(ReducerConfig.c_MASTER_HOST, ReducerConfig.c_MASTER_PORT);
                    masterSocket.setSoTimeout(ReducerConfig.c_REDUCER_SOCKET_TIMEOUT_MS);
                    out = new PrintWriter(masterSocket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(masterSocket.getInputStream()));
                    Logger.info("Reducer connected to master");
                    return;
                }
            } catch (IOException e) {
                attempts++;
                Logger.warn("Reducer connection attempt " + attempts + " failed: " + e.getMessage());
                if (attempts >= ReducerConfig.c_REDUCER_RECONNECT_ATTEMPTS) {
                    Logger.error("Failed to connect to master after " + attempts + " attempts");
                    System.exit(1);
                }
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    Logger.error("Reducer connection interrupted");
                    System.exit(1);
                }
            }
        }
    }

    public boolean isConnected() {
        synchronized (connectionLock) {
            return masterSocket != null
                    && masterSocket.isConnected()
                    && !masterSocket.isClosed();
        }
    }

    private void performHandshake() {
        Request handshake = new Request(
                UserAgent.REDUCER,
                UID.getNextUID(),
                Request.Action.REDUCER_HANDSHAKE);
        sendRequest(handshake);
    }

    private void startListening() {
        new Thread(() -> {
            Thread.currentThread().setName("Reducer-Listener");
            while (true) {
                try {
                    String messageJson;
                    synchronized (connectionLock) {
                        if (in == null || !in.ready()) {
                            Thread.sleep(100);
                            continue;
                        }
                        messageJson = in.readLine();
                    }

                    if (messageJson == null) {
                        Logger.error("Connection to master lost");
                        reconnect();
                        continue;
                    }

                    processMessage(JsonUtils.fromJson(messageJson, Message.class));

                } catch (IOException | InterruptedException e) {
                    Logger.error("Error in reducer listening thread: " + e.getMessage());
                    reconnect();
                }
            }
        }).start();
    }

    private void startHeartbeat() {
        new Thread(() -> {
            Thread.currentThread().setName("Reducer-Heartbeat");
            while (true) {
                try {
                    TimeUnit.MILLISECONDS.sleep(ReducerConfig.c_REDUCER_HEARTBEAT_INTERVAL_MS);
                    Request heartbeat = new Request(
                            UserAgent.REDUCER,
                            UID.getNextUID(),
                            Request.Action.HEARTBEAT);
                    sendRequest(heartbeat);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Logger.error("Reducer heartbeat interrupted");
                }
            }
        }).start();
    }

    private void processMessage(Message message) {
        if (message == null) {
            Logger.warn("Received null message");
            return;
        }

        try {
            if (message.getType() == Message.Type.REQUEST) {
                Request request = JsonUtils.fromJson(JsonUtils.toJson(message), Request.class);
                if (request != null) {
                    processRequest(request);
                } else {
                    Logger.warn("Failed to parse request from message");
                }
            }
        } catch (Exception e) {
            Logger.error("Error processing message: " + e.getMessage());
        }
    }

    private void processRequest(Request request) {
        if (request == null) {
            Logger.warn("Received null request");
            return;
        }

        try {
            switch (request.getAction()) {
                case FILTER_STORES:
                    processAsMapReduce(request, this::mapStores, this::reduceStores);
                    break;
                case SHOW_SALES_STORE_TYPE:
                    processAsMapReduce(request, this::mapSalesByType, this::reduceSales);
                    break;
                case SHOW_SALES_FOOD_TYPE:
                    processAsMapReduce(request, this::mapSalesByFood, this::reduceSales);
                    break;
                default:
                    Logger.warn("Unsupported request type: " + request.getAction());
            }
        } catch (Exception e) {
            Logger.error("Error processing request: " + e.getMessage());
            sendErrorResponse(request, "Processing error: " + e.getMessage());
        }
    }

    private void processAsMapReduce(Request request,
            Function<JSONObject, Map<String, Object>> mapper,
            BiFunction<Map<String, Object>, Map<String, Object>, Map<String, Object>> reducer) {
        JSONObject inputData = new JSONObject(request.getSrc());
        String requestId = inputData.getString("requestId");

        // Map phase
        Map<String, Object> mappedData = mapper.apply(inputData);

        // Reduce phase
        Map<String, Object> currentResults = intermediateResults.compute(requestId, (key, existing) -> {
            if (existing == null)
                return mappedData;
            return reducer.apply(existing, mappedData);
        });

        // Store final result when complete
        if (inputData.optBoolean("isFinalBatch", false)) {
            JSONObject finalResult = new JSONObject(currentResults);
            finalResults.put(requestId, new MapReduceResult(finalResult));
            intermediateResults.remove(requestId);
        }

        sendSuccessResponse(request, "Processed successfully");
    }

    // Mapper implementations
    private Map<String, Object> mapStores(JSONObject input) {
        Map<String, Object> result = new HashMap<>();
        JSONArray stores = input.getJSONArray("stores");
        for (int i = 0; i < stores.length(); i++) {
            JSONObject store = stores.getJSONObject(i);
            String storeId = store.getString("id");
            result.put(storeId, store);
        }
        return result;
    }

    private Map<String, Object> mapSalesByType(JSONObject input) {
        Map<String, Object> result = new HashMap<>();
        String storeType = input.getString("storeType");
        double amount = input.getDouble("amount");
        result.put(storeType, amount);
        return result;
    }

    private Map<String, Object> mapSalesByFood(JSONObject input) {
        Map<String, Object> result = new HashMap<>();
        String productType = input.getString("productType");
        double amount = input.getDouble("amount");
        result.put(productType, amount);
        return result;
    }

    // Reducer implementations
    private Map<String, Object> reduceStores(Map<String, Object> current, Map<String, Object> newData) {
        current.putAll(newData);
        return current;
    }

    private Map<String, Object> reduceSales(Map<String, Object> current, Map<String, Object> newData) {
        newData.forEach((key, value) -> {
            double currentValue = (double) current.getOrDefault(key, 0.0);
            double newValue = (double) value;
            current.put(key, currentValue + newValue);
        });
        return current;
    }

    public JSONObject getAggregatedResult(String requestId) {
        MapReduceResult result = finalResults.get(requestId);
        if (result == null) {
            Logger.warn("No results found for requestId: " + requestId);
            return new JSONObject()
                    .put("status", "not_found")
                    .put("requestId", requestId)
                    .put("timestamp", System.currentTimeMillis());
        }
        return result.aggregatedData
                .put("status", "success")
                .put("requestId", requestId)
                .put("timestamp", result.timestamp);
    }

    private void sendRequest(Request request) {
        if (request == null) {
            Logger.warn("Attempted to send null request");
            return;
        }

        synchronized (connectionLock) {
            if (out != null) {
                try {
                    String jsonRequest = JsonUtils.toJson(request);
                    if (jsonRequest != null) {
                        out.println(jsonRequest);
                    } else {
                        Logger.error("Failed to serialize request");
                    }
                } catch (Exception e) {
                    Logger.error("Error sending request: " + e.getMessage());
                }
            } else {
                Logger.warn("Output stream is null, cannot send request");
            }
        }
    }

    private void sendResponse(Response response) {
        if (response == null) {
            Logger.warn("Attempted to send null response");
            return;
        }

        synchronized (connectionLock) {
            if (out != null) {
                try {
                    String jsonResponse = JsonUtils.toJson(response);
                    if (jsonResponse != null) {
                        out.println(jsonResponse);
                    } else {
                        Logger.error("Failed to serialize response");
                    }
                } catch (Exception e) {
                    Logger.error("Error sending response: " + e.getMessage());
                }
            } else {
                Logger.warn("Output stream is null, cannot send response");
            }
        }
    }

    private void sendSuccessResponse(Request request, String message) {
        if (request == null) {
            Logger.warn("Attempted to send response to null request");
            return;
        }

        Response response = new Response(
                UserAgent.REDUCER,
                request.getId(),
                Status.SUCCESS,
                message);
        sendResponse(response);
    }

    private void sendErrorResponse(Request request, String errorMessage) {
        if (request == null) {
            Logger.warn("Attempted to send error response to null request");
            return;
        }

        Response response = new Response(
                UserAgent.REDUCER,
                request.getId(),
                Status.FAILURE,
                "Error: " + errorMessage +
                        " | Timestamp: " + System.currentTimeMillis() +
                        " | RequestAction: " + request.getAction());
        sendResponse(response);
    }

    private void reconnect() {
        Logger.info("Attempting to reconnect to master...");
        cleanup();
        connectToMaster();
        performHandshake();
    }

    private void cleanup() {
        synchronized (connectionLock) {
            IOException exception = null;

            if (out != null) {
                out.close();
            }

            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                exception = e;
                Logger.error("Error closing input stream: " + e.getMessage());
            }

            try {
                if (masterSocket != null) {
                    masterSocket.close();
                }
            } catch (IOException e) {
                exception = e;
                Logger.error("Error closing socket: " + e.getMessage());
            }

            out = null;
            in = null;
            masterSocket = null;

            if (exception != null) {
                throw new UncheckedIOException("Cleanup failed", exception);
            }
        }
    }

    @Override
    public void close() {
        try {
            cleanupExecutor.shutdown();
            if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
            cleanup();
            Logger.info("Reducer shutdown completed");
        }
    }
}
