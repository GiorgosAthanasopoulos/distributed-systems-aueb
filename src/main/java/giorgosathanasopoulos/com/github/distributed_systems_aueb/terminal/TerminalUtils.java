package giorgosathanasopoulos.com.github.distributed_systems_aueb.terminal;

public class TerminalUtils {
    @SuppressWarnings("unused")
    private static final WindowsTerminalUtils c_Win = new WindowsTerminalUtils();
    @SuppressWarnings("unused")
    private static final UnixTerminalUtils c_Unix = new UnixTerminalUtils();
    private static final GenericTerminalUtils c_Generic = new GenericTerminalUtils();

    public static void clearConsole() {
        switch (getOs()) {
            case WINDOWS -> c_Generic.clearConsole();
            case UNIX -> c_Generic.clearConsole();
            case OTHER -> c_Generic.clearConsole();
        }
    }

    public static OS getOs() {
        String os = System.getProperty("os.name");
        switch (os) {
            case "Windows" -> {
                return OS.WINDOWS;
            }
            case "Linux" -> {
                return OS.UNIX;
            }
            default -> {
                return OS.OTHER;
            }
        }
    }

    public enum OS {
        WINDOWS,
        UNIX,
        OTHER
    }
}
