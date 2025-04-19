package giorgosathanasopoulos.com.github.distributed_systems_aueb.logger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;

public class LoggerConfig {

    public static final boolean c_LOG_TO_STDOUT = true;
    public static final boolean c_LOG_TO_FILE = true;

    public static OutputStream s_LogFile = null;
    public static final String c_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter c_DATE_FORMATTER = DateTimeFormatter.ofPattern(c_DATE_FORMAT);

    static {
        try {
            s_LogFile = new FileOutputStream("log.txt");
        } catch (FileNotFoundException e) {
            Logger.error(
                    "LoggerConfig::LoggerConfig failed to statically initialize LOG_FILE: " +
                            e.getLocalizedMessage());
        }
    }
}
