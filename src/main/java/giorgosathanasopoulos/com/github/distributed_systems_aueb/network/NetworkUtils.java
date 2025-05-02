package giorgosathanasopoulos.com.github.distributed_systems_aueb.network;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.logger.Logger;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.json.JsonUtils;

public class NetworkUtils {
    public static boolean sendMessage(Socket p_To, String p_Json) {
        if (p_To == null) {
            Logger.error("NetworkUtils::sendMessage called with null receiver");
            return false;
        }
        String addr = p_To.getRemoteSocketAddress().toString();

        if (p_Json == null) {
            Logger.error("NetworkUtils::sendMessage " + addr + " called with null message");
            return false;
        }

        try {
            OutputStream os = p_To.getOutputStream();
            synchronized (os) {
                os.write((p_Json + "\n").getBytes());
            }
        } catch (IOException e) {
            Logger.error(
                    "NetworkUtils::sendMessage " + addr + " failed to send message to client: "
                            + e.getLocalizedMessage());
            return false;
        }

        return true;
    }

    public static boolean sendMessage(Socket p_To, Message p_Message) {
        return sendMessage(p_To, JsonUtils.toJson(p_Message));
    }
}
