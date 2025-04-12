package giorgosathanasopoulos.com.github.distributed_systems_aueb.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.file.FileUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.json.JsonUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.logger.Logger;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.model.Filters;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.FilterStoresRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Message.UserAgent;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Request.Action;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.uid.UID;

public class FilterStoresCommand implements Command {

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
                            "FilterStoresCommand::execute failed to read json file: " + FileUtils.getError());
                    return false;
                }

                json = jsonOptional.get();

                FilterStoresRequest request = JsonUtils.fromJson(json, FilterStoresRequest.class);
                if (request == null) {
                    Logger.error("FilterStoresRequest::execute failed to parse json file");
                    return false;
                }
                request.setId(UID.getNextUID());
                json = JsonUtils.toJson(request);
            }
            case Integer i when i > 1 -> {
                if (p_Args.length < 6) {
                    Logger.error(
                            CommandConfig.c_NOT_ENOUGH_ARGS_MSG);
                    return false;
                }

                FilterStoresRequest request = getRawFilterRequest(p_Args);
                if (request == null)
                    return false;

                json = JsonUtils.toJson(request);
            }
            default -> {
                Logger.error(CommandConfig.c_NOT_ENOUGH_ARGS_MSG);
                return false;
            }
        }

        CommandNetworkHandler.handleMessage(json, "FilterStoresRequest::execute");
        return false;
    }

    private static FilterStoresRequest getRawFilterRequest(Object... p_Args) {
        assert (p_Args.length >= 5);

        String latitudeString = (String) p_Args[0];
        String longitudeString = (String) p_Args[1];
        String radiusKmString = (String) p_Args[2];
        String foodTypeString = (String) p_Args[3];
        String starsString = (String) p_Args[4];
        String priceString = (String) p_Args[5];

        double latitude;
        double longitude;
        double radiusKm;
        try {
            latitude = Double.parseDouble(latitudeString);
            longitude = Double.parseDouble(longitudeString);
            radiusKm = Double.parseDouble(radiusKmString);
        } catch (NumberFormatException e) {
            Logger.error(
                    "FilterStoresCommand::getRawFilterRequest invalid number format: " + e.getLocalizedMessage());
            return null;
        }

        List<String> foodTypes = new ArrayList<>();
        for (String foodType : foodTypeString.split(",")) {
            foodTypes.add(foodType);
        }
        List<Integer> prices = new ArrayList<>();
        for (String price : priceString.split(",")) {
            prices.add(price.equals("$$$") ? 3 : price.equals("$$") ? 2 : 1);
        }
        List<Integer> stars = new ArrayList<>();
        for (String star : starsString.split(",")) {
            try {
                stars.add(Integer.parseInt(star));
            } catch (NumberFormatException e) {
                Logger.error(
                        "FilterStoresCommand::getRawFilterRequest invalid number format: " + e.getLocalizedMessage());
                return null;
            }
        }

        return new FilterStoresRequest(UserAgent.CLIENT, UID.getNextUID(), "", Action.FILTER_STORES,
                new Filters(latitude,
                        longitude, radiusKm, foodTypes, stars, prices));

    }

    @Override
    public String help() {
        return CommandConfig.c_FILTER_STORES_COMMAND
                + " {path to filter_stores.json} -- see example/filter_stores.json\n\t" +
                CommandConfig.c_FILTER_STORES_COMMAND
                + " {latitude} {longitude} {radiusKm} {foodType} {stars} {price} (foodType, stars, price can be comma separated with multiple values but NO WHITESPACE!)";
    }

}
