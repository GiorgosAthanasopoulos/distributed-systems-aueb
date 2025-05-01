package giorgosathanasopoulos.com.github.distributed_systems_aueb.command;

import java.io.IOException;
import java.net.Socket;
import java.util.Optional;
import java.util.Scanner;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.json.JsonUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.logger.Logger;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.master.MasterConfig;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.ListProductsResponse;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Response;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Response.About;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Response.Status;

public class CommandNetworkHandler {
    public static void handleMessage(String p_Json, String p_ClassMethod) {
        new Thread(() -> {
            try (Socket server = new Socket(MasterConfig.c_HOST, MasterConfig.c_PORT)) {
                server.getOutputStream().write((p_Json + "\n").getBytes());

                Scanner sc = new Scanner(server.getInputStream());
                while (!sc.hasNextLine()) {
                }
                String jsonResponse = sc.nextLine();

                Optional<Response> responseOptional = JsonUtils.fromJson(jsonResponse, Response.class);
                if (responseOptional.isEmpty()) {
                    Logger.error(p_ClassMethod + " failed to read response from server");
                    sc.close();
                    ;
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

                sc.close();
            } catch (IOException e) {
                Logger.error(
                        p_ClassMethod + " failed to connect to server: " + e.getLocalizedMessage());
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
