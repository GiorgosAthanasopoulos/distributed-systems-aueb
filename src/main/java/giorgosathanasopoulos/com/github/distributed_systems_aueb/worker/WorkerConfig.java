package giorgosathanasopoulos.com.github.distributed_systems_aueb.worker;

public class WorkerConfig {
    // Worker cluster configuration
    public static final int c_WORKER_COUNT = 3;
    public static final int c_WORKER_RECONNECT_ATTEMPTS = 5;
    public static final long c_WORKER_RECONNECT_DELAY_MS = 5000; // 5 seconds

    // Network configuration
    public static final String c_MASTER_HOST = "localhost";
    public static final int c_MASTER_PORT = 6969;
    public static final int c_WORKER_PORT_START = 8081; // First worker port

    // Performance settings
    public static final int c_WORKER_THREAD_POOL_SIZE = 10;
    public static final int c_WORKER_MAX_REQUEST_QUEUE = 100;

    // Timeout settings
    public static final int c_SOCKET_TIMEOUT_MS = 30000; // 30 seconds
    public static final int c_HEARTBEAT_INTERVAL_MS = 10000; // 10 seconds

    // Resource paths
    public static final String c_STORE_LOGOS_PATH = "/var/store_logos/";
    public static final String c_WORKER_LOG_FILE = "/var/log/worker.log";
}
