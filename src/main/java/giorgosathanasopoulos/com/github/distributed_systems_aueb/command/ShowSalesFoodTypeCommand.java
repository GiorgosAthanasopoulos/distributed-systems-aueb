package giorgosathanasopoulos.com.github.distributed_systems_aueb.command;

import java.io.File;
import java.util.Optional;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.logger.Logger;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.file.FileUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.json.JsonUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Message.UserAgent;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Request.Action;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.ShowSalesFoodTypeRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.uid.UID;

public class ShowSalesFoodTypeCommand implements Command {

        @Override
        public boolean execute(Object... p_Args) {
                String json;

                switch ((Integer) p_Args.length) {
                        case Integer i when i == 1 -> {
                                String arg = (String) p_Args[0];

                                if (new File(arg).exists()) {
                                        Optional<String> jsonOptional = FileUtils.readFile(arg, false);
                                        if (jsonOptional.isEmpty()) {
                                                Logger.error(
                                                                "ShowSalesFoodType::execute failed to read json file: "
                                                                                + FileUtils.getError());
                                                return false;
                                        }

                                        json = jsonOptional.get();

                                        Optional<ShowSalesFoodTypeRequest> requestOptional = JsonUtils.fromJson(json,
                                                        ShowSalesFoodTypeRequest.class);
                                        if (requestOptional.isEmpty()) {
                                                Logger.error(
                                                                "ShowSalesFoodTypeCommand::execute failed to parse json: "
                                                                                + JsonUtils.getError());
                                                return false;
                                        }
                                        ShowSalesFoodTypeRequest request = requestOptional.get();
                                        request.setId(UID.getNextUID());

                                        json = JsonUtils.toJson(request);
                                } else {
                                        ShowSalesFoodTypeRequest request = new ShowSalesFoodTypeRequest(
                                                        UserAgent.CLIENT,
                                                        UID.getNextUID(),
                                                        Action.SHOW_SALES_FOOD_TYPE, arg);
                                        json = JsonUtils.toJson(request);
                                }
                        }
                        default -> {
                                Logger.error(CommandConfig.c_NOT_ENOUGH_ARGS_MSG);
                                return false;
                        }
                }

                CommandNetworkHandler.handleMessage(json, "ShowSalesFoodType::execute");
                return false;
        }

        @Override
        public String help() {
                return (CommandConfig.c_SHOW_SALES_FOOD_TYPE_COMMAND +
                                " {path to show_sales_food_type.json} -- see example/show_sales_food_type.json\n\t" +
                                CommandConfig.c_SHOW_SALES_FOOD_TYPE_COMMAND + " {foodType}");
        }
}
