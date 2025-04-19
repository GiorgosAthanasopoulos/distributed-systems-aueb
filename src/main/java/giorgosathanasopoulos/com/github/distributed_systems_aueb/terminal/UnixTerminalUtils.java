package giorgosathanasopoulos.com.github.distributed_systems_aueb.terminal;

import java.io.IOException;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.logger.Logger;

public class UnixTerminalUtils implements IPlatformTerminalUtils {

    @Override
    public void clearConsole() {
        try {
            Runtime.getRuntime().exec(new String[] { "sh", "-c", "clear" });
        } catch (IOException e) {
            Logger.error("CommandUtils::clearConsoleLinux an error occurred when trying to clear console: "
                    + e.getLocalizedMessage());
        }
    }

}
