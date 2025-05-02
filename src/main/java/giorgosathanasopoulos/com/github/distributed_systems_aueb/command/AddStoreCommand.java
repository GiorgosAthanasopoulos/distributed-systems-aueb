package giorgosathanasopoulos.com.github.distributed_systems_aueb.command;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.file.FileUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.json.JsonUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.AddStoreRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Message.UserAgent;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.uid.UID;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Request.Action;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.logger.Logger;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.model.Store;

import java.util.Optional;

public class AddStoreCommand implements Command {

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
                            "AddStoreCommand::execute failed to read json file: " + FileUtils.getError());
                    return false;
                }

                json = jsonOptional.get();

                Optional<AddStoreRequest> requestOptional = JsonUtils.fromJson(json, AddStoreRequest.class);
                if (requestOptional.isEmpty()) {
                    Logger.error("AddStoreCommand::execute failed to parse json file");
                    return false;
                }
                AddStoreRequest request = requestOptional.get();
                request.setId(UID.getNextUID());

                json = JsonUtils.toJson(request);

            }
            case Integer i when i > 1 -> {
                if (p_Args.length < 7) {
                    Logger.error(CommandConfig.c_NOT_ENOUGH_ARGS_MSG);
                    return false;
                }

                AddStoreRequest request = getRawAddStoreRequest(p_Args);
                if (request == null)
                    return false;

                json = JsonUtils.toJson(request);
            }
            default -> {
                Logger.error(CommandConfig.c_NOT_ENOUGH_ARGS_MSG);
                return false;
            }
        }

        CommandNetworkHandler.handleMessage(json, "AddStoreCommand::execute");
        return false;
    }

    private AddStoreRequest getRawAddStoreRequest(Object... p_Args) {
        assert (p_Args.length >= 7);

        String storeName = (String) p_Args[0];
        String latitudeString = (String) p_Args[1];
        String longitudeString = (String) p_Args[2];
        String foodCategory = (String) p_Args[3];
        String starsString = (String) p_Args[4];
        String noOfVotesString = (String) p_Args[5];
        String storeLogo = (String) p_Args[6];

        try {
            double latitude = Double.parseDouble(latitudeString);
            double longitude = Double.parseDouble(longitudeString);
            int stars = Integer.parseInt(starsString);
            int noOfVotes = Integer.parseInt(noOfVotesString);

            return new AddStoreRequest(UserAgent.CLIENT, UID.getNextUID(), Action.ADD_STORE,
                    new Store(storeName, latitude, longitude, foodCategory, stars, noOfVotes, storeLogo));
        } catch (NumberFormatException e) {
            Logger
                    .error("AddStoreCommand::getRawAddStoreRequest invalid number format: " + e.getLocalizedMessage());
            return null;
        }

    }

    @Override
    public String help() {
        return CommandConfig.c_ADD_STORE_COMMAND + " {path to store.json} -- see example/store.json\n\t" +
                CommandConfig.c_ADD_STORE_COMMAND
                + " {storeName} {latitude} {longitude} {foodCategory} {stars} {noOfVotes} {storeLogo}";
    }
}
