package giorgosathanasopoulos.com.github.distributed_systems_aueb.command;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.terminal.TerminalUtils;

public class ClearConsoleCommand implements Command {

    @Override
    public boolean execute(Object... p_Args) {
        TerminalUtils.clearConsole();
        return false;
    }

    @Override
    public String help() {
        return CommandConfig.c_CLEAR_CONSOLE_COMMAND + " -- clears the console output";
    }

}
