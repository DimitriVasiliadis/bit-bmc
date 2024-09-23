package gg.dropbear.bit.containers;

import gg.dropbear.bit.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;

public class SND {

    private Player sender;
    private String senderName;
    private Users senderUser;
    private ConsoleCommandSender console;
    private Player target;
    private String targetName;
    private Users targetUser;
    private ConsoleCommandSender targetConsole;
    private Player source;
    private Users sourceUser;
    private ConsoleCommandSender sourceConsole;

    public SND() {
    }

    public SND(final Player sender, final Player target) {
        this.sender = sender;
        if (sender != null) {
            this.senderName = sender.getName();
        }
        if ((this.target = target) != null) {
            this.targetName = target.getName();
        }
    }

    public SND setSender(final Player sender) {
        this.sender = sender;
        if (sender != null) {
            this.senderName = sender.getName();
            if (this.senderUser == null) {
                this.senderUser = Main.getInstance().getUserManager().getUser(sender);
            }
        }
        return this;
    }

    public SND setSender(final Users senderUser) {
        this.senderUser = senderUser;
        if (this.senderUser != null) {
            this.senderName = this.senderUser.getName();
        }
        return this;
    }

    public SND setSender(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            this.sender = (Player)commandSender;
            this.senderName = commandSender.getName();
        }
        else if (commandSender instanceof ConsoleCommandSender) {
            this.console = (ConsoleCommandSender)commandSender;
        }
        else if (commandSender instanceof BlockCommandSender) {
            this.console = Bukkit.getConsoleSender();
        }
        else if (commandSender instanceof RemoteConsoleCommandSender) {
            this.console = Bukkit.getConsoleSender();
        }
        return this;
    }

    public SND setSender(final ConsoleCommandSender console) {
        this.console = console;
        return this;
    }

    public SND setTarget(final Player target) {
        this.target = target;
        if (this.target != null) {
            if (this.targetUser == null) {
                this.targetUser = Main.getInstance().getUserManager().getUser(target);
            }
            this.targetName = this.target.getName();
        }
        return this;
    }

    public SND setTarget(final Users targetUser) {
        this.targetUser = targetUser;
        return this;
    }

    public SND setTarget(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            this.target = (Player)commandSender;
            if (this.targetUser == null) {
                this.targetUser = Main.getInstance().getUserManager().getUser(this.target);
                this.targetName = this.target.getName();
            }
        }
        else if (commandSender instanceof ConsoleCommandSender) {
            this.targetConsole = (ConsoleCommandSender)commandSender;
        }
        else if (commandSender instanceof BlockCommandSender) {
            this.targetConsole = Bukkit.getConsoleSender();
        }
        else if (commandSender instanceof RemoteConsoleCommandSender) {
            this.targetConsole = Bukkit.getConsoleSender();
        }
        return this;
    }

    public SND setTarget(final ConsoleCommandSender targetConsole) {
        this.targetConsole = targetConsole;
        return this;
    }

    public ConsoleCommandSender getConsoleSender() {
        return this.console;
    }

    public Player getPlayerSender() {
        return this.sender;
    }

    public Users getSenderUser() {
        return this.senderUser;
    }

    public ConsoleCommandSender getConsoleTarget() {
        return this.targetConsole;
    }

    public Player getPlayerTarget() {
        return this.target;
    }

    public Users getTargetUser() {
        return this.targetUser;
    }

    public ConsoleCommandSender getConsoleSource() {
        return this.sourceConsole;
    }

    public String getSenderName() {
        return this.senderName;
    }

    public String getTargetName() {
        return this.targetName;
    }

    public Player getPlayerSource() {
        return this.source;
    }

    public Users getSourceUser() {
        return this.sourceUser;
    }



}
