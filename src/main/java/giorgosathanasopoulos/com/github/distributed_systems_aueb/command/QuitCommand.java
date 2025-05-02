package giorgosathanasopoulos.com.github.distributed_systems_aueb.command;

public class QuitCommand implements Command {

    @Override
    public boolean execute(Object... p_Args) {
        System.exit(1);
        return true;
    }

    @Override
    public String help() {
        return CommandConfig.c_QUIT_COMMAND + " -- exits the REPL";
    }
}
