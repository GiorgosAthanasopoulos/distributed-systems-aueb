package giorgosathanasopoulos.com.github.distributed_systems_aueb.command;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.file.FileUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.json.JsonUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.DecreaseQuantityRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Message.UserAgent;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.uid.UID;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Request.Action;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.logger.Logger;

import java.util.Optional;

public class DecreaseQuantityCommand implements Command {

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
                                                        "DecreaseQuantityCommand::execute failed to read json file: "
                                                                        + FileUtils.getError());
                                        return false;
                                }

                                json = jsonOptional.get();

                                Optional<DecreaseQuantityRequest> requestOptional = JsonUtils.fromJson(json,
                                                DecreaseQuantityRequest.class);
                                if (requestOptional.isEmpty()) {
                                        Logger.error(
                                                        "DecreaseQuantityCommand::execute failed to parse json file");
                                        return false;
                                }
                                DecreaseQuantityRequest request = requestOptional.get();
                                request.setId(UID.getNextUID());
                                json = JsonUtils.toJson(requestOptional);
                        }
                        case Integer i when i > 1 -> {
                                if (p_Args.length < 3) {
                                        Logger.error(CommandConfig.c_NOT_ENOUGH_ARGS_MSG);
                                        return false;
                                }

                                DecreaseQuantityRequest request = getDecreaseRawRequest(p_Args);
                                json = JsonUtils.toJson(request);
                        }
                        default -> {
                                Logger.error(CommandConfig.c_NOT_ENOUGH_ARGS_MSG);
                                return false;
                        }
                }

                CommandNetworkHandler.handleMessage(json, "DecreaseQuantityCommand::execute");
                return false;
        }

        private static DecreaseQuantityRequest getDecreaseRawRequest(Object... p_Args) {
                assert (p_Args.length >= 3);

                String storeName = (String) p_Args[0];
                String productName = (String) p_Args[1];
                String quantityString = (String) p_Args[2];

                int quantity;
                try {
                        quantity = Integer.parseInt(quantityString);
                } catch (NumberFormatException e) {
                        Logger.error(
                                        "DecreaseQuantityCommand::getDecreaseRawRequest invalid number format for quantity");
                        return null;
                }

                return new DecreaseQuantityRequest(UserAgent.CLIENT, UID.getNextUID(),
                                Action.DECREASE_QUANTITY, storeName, productName, quantity);
        }

        @Override
        public String help() {
                return (CommandConfig.c_DECREASE_QUANTITY_COMMAND
                                + " {path to decrease_quantity.json} -- see example/decrease_quantity.json\n\t"
                                + CommandConfig.c_DECREASE_QUANTITY_COMMAND
                                + " {storeName} {productName} {decreaseAmount}");
        }
}
