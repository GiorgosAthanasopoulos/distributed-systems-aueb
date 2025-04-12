package giorgosathanasopoulos.com.github.distributed_systems_aueb.command;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.repl.REPL;

public class REPLCommand implements Command {

    @Override
    public boolean execute(Object... p_Args) {
        new REPL().run();
        return false;
    }

    @Override
    public String help() {
        return CommandConfig.c_REPL_COMMAND + " -- runs the manager in REPL mode\n\t" +
                CommandConfig.c_REPL_COMMAND_2;
    }
}
