package giorgosathanasopoulos.com.github.distributed_systems_aueb.command;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.logger.Logger;
import java.util.HashMap;
import java.util.Map;

public class CommandProcessor {

        private final Map<String, Command> c_Commands = new HashMap<>();

        public CommandProcessor() {
                c_Commands.put(CommandConfig.c_ADD_STORE_COMMAND, new AddStoreCommand());
                c_Commands.put(CommandConfig.c_FILTER_STORES_COMMAND, new FilterStoresCommand());
                c_Commands.put(CommandConfig.c_LIST_STORES_COMMAND, new ListStoresCommand());

                c_Commands.put(
                                CommandConfig.c_ADD_PRODUCT_COMMAND,
                                new AddProductCommand());
                c_Commands.put(
                                CommandConfig.c_LIST_PRODUCTS_COMMAND,
                                new ListProductsCommand());
                c_Commands.put(
                                CommandConfig.c_REMOVE_PRODUCT_COMMAND,
                                new RemoveProductCommand());
                c_Commands.put(
                                CommandConfig.c_INCREASE_QUANTITY_COMMAND,
                                new IncreaseQuantityCommand());
                c_Commands.put(
                                CommandConfig.c_DECREASE_QUANTITY_COMMAND,
                                new DecreaseQuantityCommand());

                c_Commands.put(
                                CommandConfig.c_SHOW_SALES_STORE_TYPE_COMMAND,
                                new ShowSalesStoreTypeCommand());
                c_Commands.put(
                                CommandConfig.c_SHOW_SALES_FOOD_TYPE_COMMAND,
                                new ShowSalesFoodTypeCommand());

                c_Commands.put(CommandConfig.c_REPL_COMMAND, new REPLCommand());
                c_Commands.put(CommandConfig.c_SERVER_COMMAND, new ServerCommand());

                c_Commands.put(CommandConfig.c_HELP_COMMAND, new HelpCommand());
                c_Commands.put(CommandConfig.c_CLEAR_CONSOLE_COMMAND, new ClearConsoleCommand());
                c_Commands.put(CommandConfig.c_QUIT_COMMAND, new QuitCommand());
        }

        public boolean process(String[] p_Input) {
                String helpCommand = "Type '" + CommandConfig.c_HELP_COMMAND + "' for a list of commands.";

                if (p_Input.length == 0) {
                        Logger.error("Missing command. " + helpCommand);
                        return false;
                }

                Command command = c_Commands.get(p_Input[0]);

                if (command != null) {
                        Object[] args = new Object[p_Input.length - 1];
                        if (p_Input.length > 1)
                                System.arraycopy(p_Input, 1, args, 0, p_Input.length - 1);
                        return command.execute(args);
                } else {
                        Logger.error("Unknown command. " + helpCommand);
                        return false;
                }
        }
}
