package gg.dropbear.bit.commands;

import gg.dropbear.bit.Main;
import gg.dropbear.bit.modules.filehandler.ConfigReader;
import org.bukkit.command.CommandSender;

public interface CMD {

    Boolean perform(final Main p0, final CommandSender p1, final String[] p2);

    void getExtra(final ConfigReader p0);
}
