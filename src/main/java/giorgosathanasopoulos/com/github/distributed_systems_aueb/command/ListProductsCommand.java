package giorgosathanasopoulos.com.github.distributed_systems_aueb.command;

import java.io.File;
import java.util.Optional;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.file.FileUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.json.JsonUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.ListProductsRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Message.UserAgent;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Request.Action;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.uid.UID;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.logger.Logger;

public class ListProductsCommand implements Command {

    @Override
    public boolean execute(Object... p_Args) {
        String json;

        switch ((Integer) p_Args.length) {
            case Integer i when i >= 1 -> {
                String arg = (String) p_Args[0];

                if (new File(arg).exists()) {
                    Optional<String> jsonOptional = FileUtils.readFile(arg, false);
                    if (jsonOptional.isEmpty()) {
                        Logger.error(
                                "ListProductsCommand::execute failed to read json file: "
                                        + FileUtils.getError());
                        return false;
                    }

                    json = jsonOptional.get();

                    ListProductsRequest request = JsonUtils.fromJson(json,
                            ListProductsRequest.class);
                    if (request == null) {
                        Logger.error(
                                "ListProductsCommand::execute failed to parse json: "
                                        + JsonUtils.getError());
                        return false;
                    }
                    request.setId(UID.getNextUID());

                    json = JsonUtils.toJson(request);
                } else {
                    ListProductsRequest request = new ListProductsRequest(
                            UserAgent.CLIENT,
                            UID.getNextUID(),
                            Action.LIST_PRODUCTS, arg);
                    json = JsonUtils.toJson(request);
                }
            }
            default -> {
                Logger.error(CommandConfig.c_NOT_ENOUGH_ARGS_MSG);
                return false;
            }
        }

        CommandNetworkHandler.handleMessage(json, "ListProductsCommand:::execute");
        return false;
    }

    @Override
    public String help() {
        return CommandConfig.c_LIST_PRODUCTS_COMMAND
                + " {path to list_products.json} -- see example/list_products.json\n\t" +
                CommandConfig.c_LIST_PRODUCTS_COMMAND + " {storeName}";
    }

}
