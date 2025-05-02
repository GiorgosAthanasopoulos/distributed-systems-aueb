package giorgosathanasopoulos.com.github.distributed_systems_aueb.command;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.logger.Logger;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.file.FileUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.json.JsonUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.RemoveProductRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Request.Action;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.uid.UID;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Message.UserAgent;

import java.util.Optional;

public class RemoveProductCommand implements Command {

    @Override
    public boolean execute(Object... p_Args) {
        String json;

        switch ((Integer) p_Args.length) {
            case Integer i when i == 1 -> {
                // json
                String path = (String) p_Args[0];

                Optional<String> jsonOptional = FileUtils.readFile(path, false);
                if (jsonOptional.isEmpty()) {
                    Logger.error(
                            "RemoveProductCommand::execute failed to read json file: " + FileUtils.getError());
                    return false;
                }

                json = jsonOptional.get();

                Optional<RemoveProductRequest> requestOptional = JsonUtils.fromJson(json, RemoveProductRequest.class);
                if (requestOptional.isEmpty()) {
                    Logger.error("RemoveProductCommand::execute failed to parse json file");
                    return false;
                }
                RemoveProductRequest request = requestOptional.get();
                request.setId(UID.getNextUID());

                json = JsonUtils.toJson(requestOptional);
            }
            case Integer i when i > 1 -> {
                if (p_Args.length < 2) {
                    Logger.error(
                            CommandConfig.c_NOT_ENOUGH_ARGS_MSG);
                    return false;
                }

                String storeName = (String) p_Args[0];
                String productName = (String) p_Args[1];

                RemoveProductRequest request = new RemoveProductRequest(UserAgent.CLIENT, UID.getNextUID(),
                        Action.REMOVE_PRODUCT, storeName, productName);

                json = JsonUtils.toJson(request);
            }
            default -> {
                Logger.error(CommandConfig.c_NOT_ENOUGH_ARGS_MSG);
                return false;
            }
        }

        CommandNetworkHandler.handleMessage(json, "RemoveProductCommand::execute");
        return false;
    }

    @Override
    public String help() {
        return CommandConfig.c_REMOVE_PRODUCT_COMMAND
                + " {path to remove_product.json} -- see example/remove_product.json\n\t" +
                CommandConfig.c_REMOVE_PRODUCT_COMMAND + " {storeName} {productName}";
    }
}
