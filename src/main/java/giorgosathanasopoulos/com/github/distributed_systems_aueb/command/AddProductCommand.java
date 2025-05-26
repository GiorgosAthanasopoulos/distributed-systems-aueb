package giorgosathanasopoulos.com.github.distributed_systems_aueb.command;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.file.FileUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.json.JsonUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.model.Product;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.AddProductRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Message.UserAgent;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Request.Action;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.uid.UID;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.logger.Logger;

import java.util.Optional;

public class AddProductCommand implements Command {

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
                            "AddProductCommand::execute failed to read json file: " + FileUtils.getError());
                    return false;
                }

                json = jsonOptional.get();

                Optional<AddProductRequest> requestOptional = JsonUtils.fromJson(json, AddProductRequest.class);
                if (requestOptional.isEmpty()) {
                    Logger.error("AddProductCommand::execute failed to parse json file");
                    return false;
                }
                AddProductRequest request = requestOptional.get();
                request.setId(UID.getNextUID());
                json = JsonUtils.toJson(request);
            }
            case Integer i when i > 1 -> {
                if (p_Args.length < 5) {
                    Logger.error(
                            CommandConfig.c_NOT_ENOUGH_ARGS_MSG);
                    return false;
                }

                AddProductRequest request = getRawProductRequest(p_Args);
                if (request == null)
                    return false;

                json = JsonUtils.toJson(request);
            }
            default -> {
                Logger.error(CommandConfig.c_NOT_ENOUGH_ARGS_MSG);
                return false;
            }
        }

        CommandNetworkHandler.handleMessage(json, "AddProductCommand::execute");
        return false;
    }

    private static AddProductRequest getRawProductRequest(Object... p_Args) {
        assert (p_Args.length >= 5);

        String storeName = (String) p_Args[0];
        String productName = (String) p_Args[1];
        String productType = (String) p_Args[2];
        String availabilityString = (String) p_Args[3];
        String priceString = (String) p_Args[4];

        int availability;
        double price;
        try {
            availability = Integer.parseInt(availabilityString);
            price = Double.parseDouble(priceString);
        } catch (NumberFormatException e) {
            Logger.error(
                    "AddProductCommand::getRawProductRequest invalid number format for availability (and/or) price");
            return null;
        }

        return new AddProductRequest(UserAgent.CLIENT, UID.getNextUID(),
                Action.ADD_PRODUCT, storeName, new Product(storeName, productName, productType, availability, price));
    }

    @Override
    public String help() {
        return CommandConfig.c_ADD_PRODUCT_COMMAND + " {path to add_product.json} -- see example/product.json\n\t" +
                CommandConfig.c_ADD_PRODUCT_COMMAND + " {storeName} {productName} {productType} {availability} {price}";
    }
}
