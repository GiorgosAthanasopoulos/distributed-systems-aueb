package giorgosathanasopoulos.com.github.distributed_systems_aueb.command;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.json.JsonUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.master.MasterConfig;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Response;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Response.Status;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.logger.Logger;

public class CommandNetworkHandler {
    public static void handleMessage(String p_Json, String p_ClassMethod) {
        new Thread(() -> {
            try (Socket server = new Socket(MasterConfig.c_HOST, MasterConfig.c_PORT)) {
                server.getOutputStream().write((p_Json + "\n").getBytes());

                Scanner sc = new Scanner(server.getInputStream());
                while (!sc.hasNextLine()) {
                }
                String jsonResponse = sc.nextLine();

                Response response = JsonUtils.fromJson(jsonResponse, Response.class);
                if (response == null) {
                    Logger.error(p_ClassMethod + " failed to read response from server");
                }

                if (response.getStatus() == Status.SUCCESS)
                    Logger.info(p_ClassMethod + " received successful response from server: "
                            + response.getMessage());
                else
                    Logger.error(
                            p_ClassMethod + " received unsuccessful response from server: "
                                    + response.getMessage());

                sc.close();
            } catch (IOException e) {
                Logger.error(
                        p_ClassMethod + " failed to connect to server: " + e.getLocalizedMessage());
            }
        }).start();
    }
}
