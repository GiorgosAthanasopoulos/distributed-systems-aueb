package giorgosathanasopoulos.com.github.distributed_systems_aueb.cli;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.command.CommandProcessor;

public class CLI {

    private static final CommandProcessor s_CommandProcessor = new CommandProcessor();

    public static void run(String[] p_Args) {
        s_CommandProcessor.process(p_Args);
    }
}
