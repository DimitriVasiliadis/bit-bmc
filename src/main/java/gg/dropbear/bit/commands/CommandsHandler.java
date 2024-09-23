package gg.dropbear.bit.commands;

import gg.dropbear.bit.Main;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

public class CommandsHandler implements CommandExecutor {

    public static final String label = "db";
    private static String packagePath;
    public static int stage;
    public static String msg;
    private Map<String, Command> commands;
    private List<String> disabledBase;
    private boolean testServer;
    protected Main plugin;

    public boolean onCommand(CommandSender commandSender, final org.bukkit.command.Command command, final String s, String[] addLast) {
        return false;
    }
}
