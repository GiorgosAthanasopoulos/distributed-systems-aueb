package giorgosathanasopoulos.com.github.distributed_systems_aueb.repl;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.command.CommandProcessor;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.command.CommandConfig;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.logger.Logger;
import java.util.Scanner;

public class REPL {

    private final CommandProcessor c_CommandProcessor = new CommandProcessor();
    public static boolean s_IsREPL = false;

    public void run() {
        if (s_IsREPL) {
            Logger.error("REPL::run already running in REPL mode!");
            return;
        }

        s_IsREPL = true;
        Logger.info(
                "Welcome to the Manager REPL! Type '" +
                        CommandConfig.c_HELP_COMMAND +
                        "' for commands.");

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine().trim();

                if (input.isEmpty())
                    continue;

                boolean shouldExit = c_CommandProcessor.process(input.split(" "));
                if (shouldExit)
                    break;
            }
        }
    }
}
