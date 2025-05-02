package giorgosathanasopoulos.com.github.distributed_systems_aueb.command;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.json.JsonUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.logger.Logger;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.BuyProductRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Message.UserAgent;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Request.Action;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.uid.UID;

public class BuyProductCommand implements Command {

    @Override
    public boolean execute(Object... p_Args) {
        String json;

        switch ((Integer) p_Args.length) {
            case Integer i when i > 1 -> {
                if (p_Args.length < 3) {
                    Logger.error(CommandConfig.c_NOT_ENOUGH_ARGS_MSG);
                    return false;
                }

                BuyProductRequest request = getRawBuyProductRequest(p_Args);
                if (request == null)
                    return false;

                json = JsonUtils.toJson(request);
            }
            default -> {
                Logger.error(CommandConfig.c_NOT_ENOUGH_ARGS_MSG);
                return false;
            }
        }

        CommandNetworkHandler.handleMessage(json, "BuyProductCommand::execute");
        return false;
    }

    private static BuyProductRequest getRawBuyProductRequest(Object... p_Args) {
        assert (p_Args.length >= 5);

        String storeName = (String) p_Args[0];
        String productName = (String) p_Args[1];
        String quantityString = (String) p_Args[2];

        int quantity;
        try {
            quantity = Integer.parseInt(quantityString);
        } catch (NumberFormatException e) {
            Logger.error(
                    "BuyProductCommand::getRawBuyProductRequest invalid number format for quantity");
            return null;
        }

        return new BuyProductRequest(UserAgent.CLIENT, UID.getNextUID(),
                Action.BUY_PRODUCT, storeName, productName, quantity);
    }

    @Override
    public String help() {
        return CommandConfig.c_BUY_PRODUCT_COMMAND + " {storeName} {productName} {quantity}";
    }

}
