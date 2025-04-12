package giorgosathanasopoulos.com.github.distributed_systems_aueb.command;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.master.Master;

public class ServerCommand implements Command {

    @Override
    public boolean execute(Object... p_Args) {
        new Master();
        return false;
    }

    @Override
    public String help() {
        return CommandConfig.c_SERVER_COMMAND + " -- runs the server";
    }
}
