package giorgosathanasopoulos.com.github.distributed_systems_aueb.logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;

public class LoggerConfig {

    public static boolean c_LogToStdout = true;
    public static boolean c_LogToFile = true;

    public static final String c_LOG_FILE_PATH = "log.txt";
    public static OutputStream c_LogFile;
    public static final String c_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter c_DATE_FORMATTER = DateTimeFormatter.ofPattern(c_DATE_FORMAT);

    static {
        try {
            c_LogFile = new FileOutputStream(c_LOG_FILE_PATH, true);
        } catch (IOException e) {
            System.err.println("LoggerConfig::LoggerConfig failed to create log file: " + e.getMessage());
            c_LogFile = null;
            c_LogToStdout = false;
        }
    }
}
