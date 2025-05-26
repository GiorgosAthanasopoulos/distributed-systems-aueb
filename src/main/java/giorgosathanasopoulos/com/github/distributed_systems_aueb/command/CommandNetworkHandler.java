package giorgosathanasopoulos.com.github.distributed_systems_aueb.command;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.json.JsonUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.logger.Logger;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.master.MasterConfig;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.model.Product;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.model.Store;
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
                // if (response.getAbout() != About.DEFAULT) {
                // logResponseInformation(response, jsonResponse, p_ClassMethod);
                // }
            } else
                Logger.error(
                        p_ClassMethod + " received unsuccessful response from server: "
                                + response.getMessage());

            if (response.getAbout() != About.DEFAULT) {
                switch (response.getAbout()) {
                    case FILTER_STORES_REQUEST:
                        break;
                    case LIST_PRODUCTS_REQUEST:
                        Optional<ListProductsResponse> responseOptional2 = JsonUtils.fromJson(jsonResponse,
                                ListProductsResponse.class);
                        if (responseOptional2.isEmpty()) {
                            Logger.error("CommandNetworkHandler::handleMessage invalid list products repsonse json");
                            sc.close();
                            return;
                        }
                        ListProductsResponse response2 = responseOptional2.get();
                        List<Product> products = response2.getProducts();
                        Logger.info(JsonUtils.toJson(products));
                        break;
                    case LIST_STORES_REQUEST:
                        Optional<ListStoresResponse> responseOptional3 = JsonUtils.fromJson(jsonResponse,
                                ListStoresResponse.class);
                        if (responseOptional3.isEmpty()) {
                            Logger.error("CommandNetworkHandler::handleMessage invalid list stores response json");
                            sc.close();
                            return;
                        }
                        ListStoresResponse response3 = responseOptional3.get();
                        List<Store> stores = response3.getStores();
                        Logger.info(JsonUtils.toJson(stores));
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
}
