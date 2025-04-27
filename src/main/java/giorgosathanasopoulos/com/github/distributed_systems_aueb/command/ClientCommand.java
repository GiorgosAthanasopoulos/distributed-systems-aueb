package giorgosathanasopoulos.com.github.distributed_systems_aueb.command;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.Client;

public class ClientCommand implements Command {

    @Override
    public boolean execute(Object... p_Args) {
        Client.main(null);
        return false;
    }

    @Override
    public String help() {
        return CommandConfig.c_CLIENT_COMMAND + " -- runs the client program";
    }

}
