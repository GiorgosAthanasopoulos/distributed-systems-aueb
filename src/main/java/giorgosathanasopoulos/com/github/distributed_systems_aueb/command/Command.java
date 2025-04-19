package giorgosathanasopoulos.com.github.distributed_systems_aueb.command;

public interface Command {
    boolean execute(Object... p_Args);

    String help();
}
