package giorgosathanasopoulos.com.github.distributed_systems_aueb.command;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.json.JsonUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.uid.UID;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Message.UserAgent;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Request;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Request.Action;

public class ListStoresCommand implements Command {

    @Override
    public boolean execute(Object... p_Args) {
        Request request = new Request(UserAgent.CLIENT, UID.getNextUID(),
                Action.LIST_STORES);
        String json = JsonUtils.toJson(request);
        CommandNetworkHandler.handleMessage(json, "ListStoresCommand::execute");
        return false;
    }

    @Override
    public String help() {
        return CommandConfig.c_LIST_STORES_COMMAND + " -- lists all the available stores";
    }

}
