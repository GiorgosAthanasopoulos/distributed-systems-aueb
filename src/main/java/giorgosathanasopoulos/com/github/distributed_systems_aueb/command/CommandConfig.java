package giorgosathanasopoulos.com.github.distributed_systems_aueb.command;

public class CommandConfig {

    public static final String c_ADD_STORE_COMMAND = "--add-store";
    public static final String c_FILTER_STORES_COMMAND = "--filter-stores";
    public static final String c_LIST_STORES_COMMAND = "--list-stores";

    public static final String c_ADD_PRODUCT_COMMAND = "--add-product";
    public static final String c_LIST_PRODUCTS_COMMAND = "--list-products";
    public static final String c_REMOVE_PRODUCT_COMMAND = "--remove-product";
    public static final String c_INCREASE_QUANTITY_COMMAND = "--increase-quantity";
    public static final String c_DECREASE_QUANTITY_COMMAND = "--decrease-quantity";

    public static final String c_SHOW_SALES_STORE_TYPE_COMMAND = "--show-sales-store-type";
    public static final String c_SHOW_SALES_FOOD_TYPE_COMMAND = "--show-sales-food-type";

    public static final String c_REPL_COMMAND = "--repl";
    public static final String c_SERVER_COMMAND = "--server";

    public static final String c_HELP_COMMAND = "--help";
    public static final String c_CLEAR_CONSOLE_COMMAND = "--clear";
    public static final String c_CLEAR_CONSOLE_COMMAND_2 = "-c";
    public static final String c_QUIT_COMMAND = "--quit";

    public static final String c_NOT_ENOUGH_ARGS_MSG = "Not enough arguments for request. Refer to " + c_HELP_COMMAND
            + " for help.";

    public static final String c_WINDOWS_OS_NAME = "Windows";
    public static final String c_LINUX_OS_NAME = "Linux";
}
