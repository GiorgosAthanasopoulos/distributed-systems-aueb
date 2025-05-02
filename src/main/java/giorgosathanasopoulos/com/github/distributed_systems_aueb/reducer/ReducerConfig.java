package giorgosathanasopoulos.com.github.distributed_systems_aueb.reducer;

public class ReducerConfig {
    public static final String c_HOST = "localhost";
    public static final int c_PORT = 8081;
    public static final int c_BACKLOG = 10;

    // Network settings
    public static final String c_MASTER_HOST = "localhost";
    public static final int c_MASTER_PORT = 8080;

    // Connection settings
    public static final int c_REDUCER_RECONNECT_ATTEMPTS = 5;
    public static final int c_REDUCER_SOCKET_TIMEOUT_MS = 5000;
    public static final int c_REDUCER_HEARTBEAT_INTERVAL_MS = 30000;
}
