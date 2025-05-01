package giorgosathanasopoulos.com.github.distributed_systems_aueb.command;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.file.FileUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.json.JsonUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.IncreaseQuantityRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Message.UserAgent;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Request.Action;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.uid.UID;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.logger.Logger;

import java.util.Optional;

public class IncreaseQuantityCommand implements Command {

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
                                                        "IncreaseQuantityCommand::execute failed to read json file: "
                                                                        + FileUtils.getError());
                                        return false;
                                }

                                json = jsonOptional.get();

                                Optional<IncreaseQuantityRequest> requestOptional = JsonUtils.fromJson(json,
                                                IncreaseQuantityRequest.class);
                                if (requestOptional.isEmpty()) {
                                        Logger.error(
                                                        "IncreaseQuantityCommand::execute failed to parse json file");
                                        return false;
                                }
                                IncreaseQuantityRequest request = requestOptional.get();
                                request.setId(UID.getNextUID());
                                json = JsonUtils.toJson(request);
                        }
                        case Integer i when i > 1 -> {
                                if (p_Args.length < 3) {
                                        Logger.error(CommandConfig.c_NOT_ENOUGH_ARGS_MSG);
                                        return false;
                                }

                                IncreaseQuantityRequest request = getIncreaseRawRequest(p_Args);
                                json = JsonUtils.toJson(request);
                        }
                        default -> {
                                Logger.error(CommandConfig.c_NOT_ENOUGH_ARGS_MSG);
                                return false;
                        }
                }

                CommandNetworkHandler.handleMessage(json, "IncreaseQuantityCommand::execute");
                return false;
        }

        private static IncreaseQuantityRequest getIncreaseRawRequest(Object... p_Args) {
                assert (p_Args.length >= 3);

                String storeName = (String) p_Args[0];
                String productName = (String) p_Args[1];
                String quantityString = (String) p_Args[2];

                int quantity;
                try {
                        quantity = Integer.parseInt(quantityString);
                } catch (NumberFormatException e) {
                        Logger.error(
                                        "IncreaseQuantityCommand::getIncreaseRawRequest invalid number format for quantity");
                        return null;
                }

                return new IncreaseQuantityRequest(UserAgent.CLIENT, UID.getNextUID(),
                                Action.DECREASE_QUANTITY, storeName, productName, quantity);
        }

        @Override
        public String help() {
                return (CommandConfig.c_INCREASE_QUANTITY_COMMAND +
                                " {path to increase_quantity.json} -- see example/increase_quantity.json\n\t" +
                                CommandConfig.c_INCREASE_QUANTITY_COMMAND
                                + " {storeName} {productName} {increaseAmount}");
        }
}
