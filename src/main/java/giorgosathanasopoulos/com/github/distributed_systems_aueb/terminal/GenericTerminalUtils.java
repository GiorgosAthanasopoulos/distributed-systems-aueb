package giorgosathanasopoulos.com.github.distributed_systems_aueb.terminal;

public class GenericTerminalUtils implements IPlatformTerminalUtils {

    @Override
    public void clearConsole() {
        System.out.println("\033[H\033[2J");
        System.out.flush();
    }

}
