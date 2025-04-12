package giorgosathanasopoulos.com.github.distributed_systems_aueb.command;

public class ClearConsoleCommand implements Command {

    @Override
    public boolean execute(Object... p_Args) {
        CommandUtils.clearConsole();
        return false;
    }

    @Override
    public String help() {
        return CommandConfig.c_CLEAR_CONSOLE_COMMAND + " -- clears the console output\n\t" +
                CommandConfig.c_CLEAR_CONSOLE_COMMAND_2;
    }

}
