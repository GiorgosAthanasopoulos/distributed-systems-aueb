package giorgosathanasopoulos.com.github.distributed_systems_aueb.command;

public class QuitCommand implements Command {

    @Override
    public boolean execute(Object... p_Args) {
        return true;
    }

    @Override
    public String help() {
        return CommandConfig.c_QUIT_COMMAND + " -- exits the REPL\n\t" +
                CommandConfig.c_QUIT_COMMAND_2;
    }
}
