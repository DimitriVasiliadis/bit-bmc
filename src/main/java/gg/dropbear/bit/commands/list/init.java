package gg.dropbear.bit.commands.list;

import gg.dropbear.bit.Main;
import gg.dropbear.bit.commands.CAnnotation;
import gg.dropbear.bit.commands.CMD;
import gg.dropbear.bit.containers.Users;
import gg.dropbear.bit.modules.filehandler.ConfigReader;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class init implements CMD {

    @Override
    public void getExtra(final ConfigReader configReader) {
        configReader.addComment("afkTitle", new String[] { "This only determines message in title message and not message shown in chat. For chat message check config.yml file" });
        configReader.get("afkTitle", "&eYou are now AFK");
        configReader.get("afkSubTitle", (List) Arrays.asList("&2Don't eat to much cookies!", "&2Nice day for fishing", "&2Afk, again?", "&2Where are you?"));
    }

    @CAnnotation(info = "&eToggle afk mode. Reason could be provided", args = "(-p:playerName) (reason) (-s)", tab = { "playername", "-s" }, modules = { "afk" }, others = true)
    @Override
    public Boolean perform(final Main db, final CommandSender commandSender, final String[] array) {
        String substring = null;
        String s = "";
        boolean b = false;
        for (final String str : array) {
            final String lowerCase;
            switch (lowerCase = str.toLowerCase()) {
                case "-s": {
                    if (PermissionsManager.DBPerm.command_silent.hasPermission(commandSender)) {
                        b = true;
                    }
                    break;
                }
                default:
                    break;
            }
            if (str.startsWith("-p:")) {
                substring = str.substring("-p:".length());
            }
            else {
                if (!s.isEmpty()) {
                    s = String.valueOf(s) + " ";
                }
                s = String.valueOf(s) + str;
            }
        }
        final Users user = db.getUser(commandSender, substring, this);

        if (user == null) {
            return null;
        }


        return true;
    }
}
