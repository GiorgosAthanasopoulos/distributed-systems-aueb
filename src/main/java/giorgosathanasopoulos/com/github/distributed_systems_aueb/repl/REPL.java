package giorgosathanasopoulos.com.github.distributed_systems_aueb.repl;

import java.util.Scanner;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.logger.Logger;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.command.CommandConfig;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.command.CommandProcessor;

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

        System.out.print("> ");
        Scanner sc = new Scanner(System.in);

        while (true) {
            if (!sc.hasNextLine())
                continue;

            String input = sc.nextLine().trim();
            boolean shouldExit = c_CommandProcessor.process(input.split(" "));
            if (shouldExit)
                break;

            System.out.println("> ");
        }

        sc.close();
    }
}
