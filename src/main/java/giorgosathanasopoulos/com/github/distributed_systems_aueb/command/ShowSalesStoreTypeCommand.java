package giorgosathanasopoulos.com.github.distributed_systems_aueb.command;

import java.io.File;
import java.util.Optional;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.file.FileUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.json.JsonUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Message.UserAgent;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.Request.Action;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.network.ShowSalesStoreTypeRequest;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.uid.UMID;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.logger.Logger;

public class ShowSalesStoreTypeCommand implements Command {

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
                                                                "ShowSalesStoreTypeCommand::execute failed to read json file: "
                                                                                + FileUtils.getError());
                                                return false;
                                        }

                                        json = jsonOptional.get();

                                        ShowSalesStoreTypeRequest request = JsonUtils.fromJson(json,
                                                        ShowSalesStoreTypeRequest.class);
                                        if (request == null) {
                                                Logger.error(
                                                                "ShowSalesStoreTypeCommand::execute failed to parse json: "
                                                                                + JsonUtils.getError());
                                                return false;
                                        }
                                        request.setId(UID.getNextUID());

                                        json = JsonUtils.toJson(request);
                                } else {
                                        ShowSalesStoreTypeRequest request = new ShowSalesStoreTypeRequest(
                                                        UserAgent.CLIENT,
                                                        UID.getNextUID(),
                                                        "", Action.REMOVE_PRODUCT, arg);
                                        json = JsonUtils.toJson(request);
                                }
                        }
                        default -> {
                                Logger.error(CommandConfig.c_NOT_ENOUGH_ARGS_MSG);
                                return false;
                        }
                }

                CommandNetworkHandler.handleMessage(json, "ShowSalesStoreType::execute");
                return false;
        }

        @Override
        public String help() {
                return (CommandConfig.c_SHOW_SALES_STORE_TYPE_COMMAND +
                                " {path to show_sales_store_type.json} -- see example/show_sales_store_type.json\n\t" +
                                CommandConfig.c_SHOW_SALES_STORE_TYPE_COMMAND + " {storeType}");
        }
}
