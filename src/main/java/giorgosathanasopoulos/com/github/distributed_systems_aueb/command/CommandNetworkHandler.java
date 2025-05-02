package giorgosathanasopoulos.com.github.distributed_systems_aueb.command;

import java.io.IOException;
import java.net.Socket;
import java.util.Optional;
import java.util.Scanner;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.json.JsonUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.logger.Logger;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.master.MasterConfig;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.ListProductsResponse;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.ListStoresResponse;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.NetworkUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Response;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Response.About;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Response.Status;

public class CommandNetworkHandler {
    public static void handleMessage(String p_Json, String p_ClassMethod) {
        new Thread(() -> {
            Socket server = null;
            try {
                server = new Socket(MasterConfig.c_HOST, MasterConfig.c_PORT);
            } catch (IOException e) {
                Logger
                        .error("CommandNetworkHandler::handleMessage failed to connect to server: " + e.getMessage());
                return;
            }
            assert server != null;

            NetworkUtils.sendMessage(server, p_Json);

            Scanner sc = null;
            try {
                sc = new Scanner(server.getInputStream());
            } catch (IOException e) {
                Logger.error("CommandNetworkHandler::handleMessage failed to create scanner: " + e.getMessage());
                try {
                    server.close();
                } catch (IOException e1) {
                    Logger.error(
                            "CommandNetworkHandler::handleMessage failed to early close server: " + e1.getMessage());
                    return;
                }
                return;
            }
            assert sc != null;

            // while (!sc.hasNextLine()) {
            // }
            String jsonResponse = sc.nextLine();

            Optional<Response> responseOptional = JsonUtils.fromJson(jsonResponse, Response.class);
            if (responseOptional.isEmpty()) {
                Logger.error(p_ClassMethod + " failed to read response from server");
                sc.close();
                try {
                    server.close();
                } catch (IOException e) {
                    Logger.error(
                            "CommandNetworkHandler::handleMessage failed to early close server 2: " + e.getMessage());
                    return;
                }
                return;
            }
            Response response = responseOptional.get();

            if (response.getStatus() == Status.SUCCESS) {
                Logger.info(p_ClassMethod + " received successful response from server: "
                        + response.getMessage());
                if (response.getAbout() != About.DEFAULT) {
                    logResponseInformation(response, jsonResponse, p_ClassMethod);
                }
            } else
                Logger.error(
                        p_ClassMethod + " received unsuccessful response from server: "
                                + response.getMessage());

            // TODO: implement cli side stats receival
            if (response.getAbout() != About.DEFAULT) {
                switch (response.getAbout()) {
                    case FILTER_STORES_REQUEST:
                        break;
                    case LIST_PRODUCTS_REQUEST:
                        break;
                    case LIST_STORES_REQUEST:
                        Optional<ListStoresResponse> responseOptional1 = JsonUtils.fromJson(p_Json,
                                ListStoresResponse.class);
                        if (responseOptional1.isEmpty()) {
                            Logger.error("CommandNetworkHandler::handleMessage invalid list stores response json");
                            sc.close();
                            return;
                        }
                        ListStoresResponse response1 = responseOptional1.get();
                        Logger.info(JsonUtils.toJson(response1.getStores()));
                        break;
                    case REDUCER_INFO:
                        break;
                    case SHOW_SALES_FOOD_TYPE_REQUEST:
                        break;
                    case SHOW_SALES_STORE_TYPE_REQUEST:
                        break;
                    default:
                        break;
                }
            }

            sc.close();
            try {
                server.close();
            } catch (IOException e) {
                Logger
                        .error("CommandNetworkHandler::handleMessage failed to close server socket: " + e.getMessage());
                return;
            }
        }).start();
    }

    private static void logResponseInformation(Response p_Response, String p_Json, String p_ClassMethod) {
        switch (p_Response.getAbout()) {
            case REDUCER_INFO -> {
            }

            case LIST_STORES_REQUEST -> {
            }
            case FILTER_STORES_REQUEST -> {
            }

            case LIST_PRODUCTS_REQUEST -> {
                Optional<ListProductsResponse> listProductsResponse = JsonUtils.fromJson(p_Json,
                        ListProductsResponse.class);

                if (listProductsResponse.isEmpty()) {
                    Logger.error(
                            p_ClassMethod + " could not parse list products request response");
                    break;
                }

                Logger.info(
                        p_ClassMethod + " received products list for store " + listProductsResponse.get().toString());
            }

            case SHOW_SALES_FOOD_TYPE_REQUEST -> {
            }
            case SHOW_SALES_STORE_TYPE_REQUEST -> {
            }

            case DEFAULT -> {
            }

            default -> {
            }
        }
    }
}
