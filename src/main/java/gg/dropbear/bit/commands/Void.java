package gg.dropbear.bit.commands;

import gg.dropbear.bit.Main;
import gg.dropbear.bit.modules.filehandler.ConfigReader;
import org.bukkit.command.CommandSender;

public class Void implements CMD {

    @Override
    public void getExtra(final ConfigReader configReader) {
    }

    @CAnnotation(others = false)
    @Override
    public Boolean perform(final Main db, final CommandSender commandSender, final String[] array) {
        return true;
    }
}
