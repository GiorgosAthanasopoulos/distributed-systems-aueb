package giorgosathanasopoulos.com.github.distributed_systems_aueb;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.cli.CLI;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.logger.Logger;

public class Main {

    public static void main(String[] args) {
        Logger.info(
                "When providing raw parameters (cli/repl) names must not have whitespace (not even with \") - they are treated as different arguments (our parser's limitation)!");
        CLI.run(args);
    }
}
