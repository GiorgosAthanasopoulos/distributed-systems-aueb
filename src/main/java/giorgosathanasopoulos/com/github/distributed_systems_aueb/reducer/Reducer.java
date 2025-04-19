package giorgosathanasopoulos.com.github.distributed_systems_aueb.reducer;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.json.JsonUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.logger.Logger;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.*;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Message.UserAgent;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Response.Status;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.uid.UID;

public class Reducer {
    private Socket masterSocket;
    private PrintWriter out;
    private BufferedReader in;
    private final Object connectionLock = new Object();

    // Data structures for aggregation
    private final Map<String, List<JsonObject>> filteredStores = new HashMap<>();
    private final Map<String, Double> salesByStoreType = new HashMap<>();
    private final Map<String, Double> salesByProductType = new HashMap<>();

    public Reducer() {
        connectToMaster();
        performHandshake();
        startListening();
        startHeartbeat();
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
                Logger.warn("Reducer connection attempt " + attempts + " failed");
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

    private void performHandshake() {
        Request handshake = new Request(
                UserAgent.REDUCER,
                UID.getNextUID(),
                Request.Action.REDUCER_HANDSHAKE);
        // "Reducer handshake",
        // "Reducer1");
        sendRequest(handshake);
    }

    private void startListening() {
        new Thread(() -> {
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

                    Message message = JsonUtils.fromJson(messageJson, Message.class);
                    if (message == null) {
                        Logger.error("Failed to parse message from master");
                        continue;
                    }

                    processMessage(message);

                } catch (IOException | InterruptedException e) {
                    Logger.error("Error in reducer listening thread: " + e.getMessage());
                    reconnect();
                }
            }
        }).start();
    }

    private void startHeartbeat() {
        new Thread(() -> {
            while (true) {
                try {
                    TimeUnit.MILLISECONDS.sleep(ReducerConfig.c_REDUCER_HEARTBEAT_INTERVAL_MS);
                    Request heartbeat = new Request(
                            UserAgent.REDUCER,
                            UID.getNextUID(),
                            Request.Action.HEARTBEAT);
                    // "Reducer heartbeat",
                    // "Reducer");
                    sendRequest(heartbeat);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Logger.error("Reducer heartbeat interrupted");
                }
            }
        }).start();
    }

    private void processMessage(Message message) {
        if (message.getType() == Message.Type.REQUEST) {
            Request request = JsonUtils.fromJson(JsonUtils.toJson(message), Request.class);
            if (request != null) {
                processRequest(request);
            }
        }
    }

    private void processRequest(Request request) {
        switch (request.getAction()) {
            case FILTER_STORES:
                processFilterStoresRequest(request);
                break;
            case SHOW_SALES_STORE_TYPE:
                processSalesByStoreTypeRequest(request);
                break;
            case SHOW_SALES_FOOD_TYPE:
                processSalesByFoodTypeRequest(request);
                break;
            default:
                Logger.warn("Received unsupported request type: " + request.getAction());
        }
    }

    private void processFilterStoresRequest(Request request) {
        try {
            JsonObject filterData = new JsonObject(request.getSrc());
            String requestId = filterData.getString("requestId");
            JSONArray stores = filterData.getJSONArray("stores");

            synchronized (filteredStores) {
                List<JsonObject> storeList = filteredStores.getOrDefault(requestId, new ArrayList<>());
                for (int i = 0; i < stores.length(); i++) {
                    storeList.add(stores.getJsonObject(i));
                }
                filteredStores.put(requestId, storeList);
            }

            // Send acknowledgment
            Response response = new Response(
                    UserAgent.REDUCER,
                    request.getId(),
                    // request.getSrc(),
                    Status.SUCCESS,
                    "Stores received for filtering");
            sendResponse(response);

        } catch (Exception e) {
            Logger.error("Error processing filter stores request: " + e.getMessage());
            sendErrorResponse(request, "Error processing filter stores");
        }
    }

    private void processSalesByStoreTypeRequest(Request request) {
        try {
            JsonObject salesData = new JsonObject(request.getSrc());
            String storeType = salesData.getString("storeType");
            double amount = salesData.getDouble("amount");

            synchronized (salesByStoreType) {
                double current = salesByStoreType.getOrDefault(storeType, 0.0);
                salesByStoreType.put(storeType, current + amount);
            }

            Response response = new Response(
                    UserAgent.REDUCER,
                    request.getId(),
                    // request.getSrc(),
                    Status.SUCCESS,
                    "Sales data received for store type: " + storeType);
            sendResponse(response);

        } catch (Exception e) {
            Logger.error("Error processing sales by store type request: " + e.getMessage());
            sendErrorResponse(request, "Error processing sales by store type");
        }
    }

    private void processSalesByFoodTypeRequest(Request request) {
        try {
            JsonObject salesData = new JsonObject(request.getSrc());
            String productType = salesData.getString("productType");
            double amount = salesData.getDouble("amount");

            synchronized (salesByProductType) {
                double current = salesByProductType.getOrDefault(productType, 0.0);
                salesByProductType.put(productType, current + amount);
            }

            Response response = new Response(
                    UserAgent.REDUCER,
                    request.getId(),
                    // request.getSrc(),
                    Status.SUCCESS,
                    "Sales data received for product type: " + productType);
            sendResponse(response);

        } catch (Exception e) {
            Logger.error("Error processing sales by food type request: " + e.getMessage());
            sendErrorResponse(request, "Error processing sales by food type");
        }
    }

    private void sendRequest(Request request) {
        synchronized (connectionLock) {
            if (out != null) {
                out.println(JsonUtils.toJson(request));
            }
        }
    }

    private void sendResponse(Response response) {
        synchronized (connectionLock) {
            if (out != null) {
                out.println(JsonUtils.toJson(response));
            }
        }
    }

    private void sendErrorResponse(Request request, String errorMessage) {
        Response response = new Response(
                UserAgent.REDUCER,
                request.getId(),
                // request.getSrc(),
                Status.FAILURE,
                errorMessage);
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
            try {
                if (out != null)
                    out.close();
                if (in != null)
                    in.close();
                if (masterSocket != null)
                    masterSocket.close();
            } catch (IOException e) {
                Logger.error("Error closing resources: " + e.getMessage());
            }
            out = null;
            in = null;
            masterSocket = null;
        }
    }

    // Methods to get aggregated data (called by Master when needed)
    public synchronized List<JsonObject> getFilteredStores(String requestId) {
        return filteredStores.getOrDefault(requestId, new ArrayList<>());
    }

    public synchronized Map<String, Double> getSalesByStoreType() {
        return new HashMap<>(salesByStoreType);
    }

    public synchronized Map<String, Double> getSalesByProductType() {
        return new HashMap<>(salesByProductType);
    }
}
