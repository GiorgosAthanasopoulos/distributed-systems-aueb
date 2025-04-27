package giorgosathanasopoulos.com.github.distributed_systems_aueb.command;

public class HelpCommand implements Command {

    @Override
    public boolean execute(Object... p_Args) {
        System.out.println(help());
        return false;
    }

    @Override
    public String help() {
        StringBuilder sb = new StringBuilder();

        sb.append("\n");

        sb.append(new AddStoreCommand().help()).append("\n");
        sb.append(new FilterStoresCommand().help()).append("\n");
        sb.append(new ListStoresCommand().help()).append("\n");
        sb.append("\n");

        sb.append(new AddProductCommand().help()).append("\n");
        sb.append(new ListProductsCommand().help()).append("\n");
        sb.append(new RemoveProductCommand().help()).append("\n");
        sb.append(new IncreaseQuantityCommand().help()).append("\n");
        sb.append(new DecreaseQuantityCommand().help()).append("\n");
        sb.append("\n");

        sb.append(new ShowSalesFoodTypeCommand().help()).append("\n");
        sb.append(new ShowSalesStoreTypeCommand().help()).append("\n");
        sb.append("\n");

        sb.append(new ServerCommand().help()).append("\n");
        sb.append(new REPLCommand().help()).append("\n");
        sb.append(new ClientCommand().help()).append("\n");
        sb.append("\n");

        sb.append(CommandConfig.c_HELP_COMMAND + " -- shows available commands").append("\n");
        sb.append(new ClearConsoleCommand().help()).append("\n");
        sb.append(new QuitCommand().help()).append("\n");

        return sb.toString();
    }
}
