package giorgosathanasopoulos.com.github.distributed_systems_aueb.command;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.json.JsonUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.ListStoresRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.UMID;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Message.UserAgent;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Request.Action;

public class ListStoresCommand implements Command {

    @Override
    public boolean execute(Object... p_Args) {
        ListStoresRequest request = new ListStoresRequest(UserAgent.CLIENT, UMID.getNextUniqueMessageId(), "",
                Action.LIST_STORES);
        String json = JsonUtils.toJson(request);
        CommandNetworkHandler.handleMessage(json, "ListStoresCommand::execute");
        return false;
    }

    @Override
    public String help() {
        return CommandConfig.c_LIST_STORES_COMMAND + " -- lists all the available stores\n\t" +
                CommandConfig.c_LIST_STORES_COMMAND_2;
    }

}
