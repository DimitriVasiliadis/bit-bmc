package gg.dropbear.bit;

import gg.dropbear.bit.containers.Region;
import gg.dropbear.bit.containers.Users;
import gg.dropbear.bit.locale.Language;
import gg.dropbear.bit.modules.database.DBClassLoader;
import gg.dropbear.bit.modules.database.DBManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.module.Configuration;
import java.util.UUID;

public class Main extends JavaPlugin {
    protected static Main instance;

    private boolean fullyLoaded;

    protected Language languageManager;
    protected Config configManager;

    protected UserManager userManager;
    protected PlayerManager playerManager;
    private DBManager dbManager;

    private DBClassLoader classLoader;

    @Override
    public void onLoad() {
        ConfigurationSerialization.registerClass(Region.class);
    }

    @Override
    public void onEnable() {
        Main.instance = this;
    }

    @Override
    public void onDisable() {

    }

    public static Main getInstance() {
        return instance;
    }

    public boolean isFullyLoaded() {
        return this.fullyLoaded;
    }


    public Player getPlayer(String lowerCase) {
        final Player player = Bukkit.getPlayer(lowerCase);
        if (player != null) {
            return player;
        }
        lowerCase = lowerCase.toLowerCase();

        final UUID whoUsesName = this.getPlayer(lowerCase).getUniqueId();
        if (whoUsesName != null) {
            final Users user = this.getUserManager().getUser(whoUsesName);
            if (user != null && user.isOnline()) {
                return user.getPlayer();
            }
        }
        return null;
    }

    public Users getUser(final CommandSender commandSender, final String s, final Object o, final boolean b, final boolean b2) {
        return this.getUser(commandSender, s, o.getClass().getSimpleName(), b, b2);
    }

    public Users getUser(final CommandSender commandSender, final String s, final Object o) {
        return this.getUser(commandSender, s, o.getClass().getSimpleName(), true, true);
    }

    public Language getLM() {
        if (this.languageManager == null) {
            (this.languageManager = new Language(this)).reload();
        }
        return this.languageManager;
    }

    public Config getConfigManager() {
        if (this.configManager == null) {
            this.configManager = new Config(this);
        }
        return this.configManager;
    }

    public UserManager getUserManager() {
        if (this.userManager == null) {
            this.userManager = new UserManager(this);
        }
        return this.userManager;
    }

    public PlayerManager getPlayerManager() {
        if (this.playerManager == null) {
            this.playerManager = new PlayerManager(this);
        }
        return this.playerManager;
    }

    public DBManager getDbManager() {
        if (this.dbManager == null) {
            this.dbManager = new DBManager(this);
        }
        return this.dbManager;
    }

    public DBClassLoader getDBClassloader() {
        return this.classLoader;
    }

    public void consoleMessage(String stripColor) {
        final ConsoleCommandSender consoleSender = Bukkit.getConsoleSender();
        if (Config.monochromeConsole) {
            stripColor = DBChatColor.stripColor(stripColor);
            if (this.fullyLoaded) {
                consoleSender.sendMessage(String.valueOf(DBChatColor.stripColor(this.prefix)) + ((stripColor == null) ? null : stripColor));
            }
            else {
                consoleSender.sendMessage("  " + stripColor);
            }
            return;
        }
        if (this.fullyLoaded) {
            consoleSender.sendMessage(String.valueOf(this.prefix) + ((stripColor == null) ? null : DBChatColor.translate(stripColor)));
        }
        else {
            consoleSender.sendMessage(DBChatColor.translate("  &3" + stripColor));
        }
    }
}