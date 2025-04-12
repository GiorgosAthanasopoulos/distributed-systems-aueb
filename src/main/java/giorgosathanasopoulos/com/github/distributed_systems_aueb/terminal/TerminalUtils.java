package giorgosathanasopoulos.com.github.distributed_systems_aueb.terminal;

public class TerminalUtils {
    public static void clearConsole() {
        switch (getOs()) {
            case WINDOWS -> clearConsoleGeneric();
            case LINUX -> clearConsoleGeneric();
            case OTHER -> clearConsoleGeneric();
        }
    }

    private static void clearConsoleGeneric() {
        System.out.println("\033[H\033[2J");
        System.out.flush();
    }

    // private static void clearConsoleWindows() {
    // try {
    // Runtime.getRuntime().exec(new String[] { "cls" });
    // } catch (IOException e) {
    // Logger.error("CommandUtils::clearConsoleWindows an error occurred when trying
    // to clear console: "
    // + e.getLocalizedMessage());
    // }
    // }

    // private static void clearConsoleLinux() {
    // try {
    // Runtime.getRuntime().exec(new String[] { "sh", "-c", "clear" });
    // } catch (IOException e) {
    // Logger.error("CommandUtils::clearConsoleLinux an error occurred when trying
    // to clear console: "
    // + e.getLocalizedMessage());
    // }
    // }

    public static OS getOs() {
        String os = System.getProperty("os.name");
        switch (os) {
            case "Windows" -> {
                return OS.WINDOWS;
            }
            case "Linux" -> {
                return OS.LINUX;
            }
            default -> {
                return OS.OTHER; // most likely than not
            }
        }
    }

    public enum OS {
        WINDOWS,
        LINUX,
        OTHER
    }
}
