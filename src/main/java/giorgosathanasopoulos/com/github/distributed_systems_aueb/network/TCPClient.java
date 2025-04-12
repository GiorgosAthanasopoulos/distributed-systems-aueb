package giorgosathanasopoulos.com.github.distributed_systems_aueb.network;

import java.net.Socket;
import java.util.Optional;

public interface TCPClient {
    Socket connectToServer(int p_Count);

    void sendHandshake();

    void listenForMessages();

    Optional<Response> handleMessage(String p_Json);

    Optional<Response> handleResponse(String p_Json);

    Optional<Response> handleRequest(String p_Json);

    void sendMessage(String p_Json);
}
