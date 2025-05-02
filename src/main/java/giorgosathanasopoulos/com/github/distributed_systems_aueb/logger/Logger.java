package giorgosathanasopoulos.com.github.distributed_systems_aueb.logger;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;

public class Logger {

    private static final Object c_LOCK = new Object();

    public enum Level {
        INFO,
        WARN,
        ERROR,
    }

    public static void log(Level p_Level, String p_Message) {
        String timestamp = LocalDateTime.now()
                .format(LoggerConfig.c_DATE_FORMATTER);
        String logMessage = String.format(
                "[%s] [%s] %s",
                timestamp,
                p_Level,
                p_Message);

        if (LoggerConfig.c_LogToFile && LoggerConfig.c_LogFile != null)
            logFile(
                    logMessage,
                    LoggerConfig.c_LogFile);

        if (LoggerConfig.c_LogToStdout)
            logStd(p_Level, logMessage);
    }

    private static void logFile(String p_Message, OutputStream p_File) {
        String timestamp = LocalDateTime.now()
                .format(LoggerConfig.c_DATE_FORMATTER);

        synchronized (c_LOCK) {
            try {
                p_File.write(p_Message.getBytes());
                p_File.write(System.lineSeparator().getBytes());
                p_File.flush();
            } catch (IOException e) {
                System.err.printf(
                        "[%s] [%s] %s%s",
                        timestamp,
                        Level.ERROR,
                        e.getLocalizedMessage(),
                        System.lineSeparator());
                System.err.flush();
            }
        }
    }

    private static void logStd(Level p_Level, String p_Message) {
        synchronized (c_LOCK) {
            if (p_Level == Level.ERROR) {
                System.err.println(p_Message);
                System.err.flush();
            } else {
                System.out.println(p_Message);
                System.out.flush();
            }
        }
    }

    public static void info(String p_Message) {
        log(Level.INFO, p_Message);
    }

    public static void warn(String p_Message) {
        log(Level.WARN, p_Message);
    }

    public static void error(String p_Message) {
        log(Level.ERROR, p_Message);
    }
}
