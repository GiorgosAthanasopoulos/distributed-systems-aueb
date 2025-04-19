package giorgosathanasopoulos.com.github.distributed_systems_aueb.terminal;

import java.io.IOException;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.logger.Logger;

public class WindowsTerminalUtils implements IPlatformTerminalUtils {

    @Override
    public void clearConsole() {
        try {
            Runtime.getRuntime().exec(new String[] { "cls" });
        } catch (IOException e) {
            Logger.error("CommandUtils::clearConsoleWindows an error occurred when trying to clear console: "
                    + e.getLocalizedMessage());
        }
    }

}
