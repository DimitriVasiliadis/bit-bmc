package gg.dropbear.bit.locale;

import gg.dropbear.bit.Main;
import gg.dropbear.bit.containers.SND;
import gg.dropbear.bit.containers.Users;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Language {

    private Main plugin;
    private HashMap<String, FileConfiguration> locales;
    static Pattern pattern;

    static {
        Language.pattern = Pattern.compile("(\\\\n)");
    }

    public Language(final Main plugin) {
        this.locales = new HashMap<String, FileConfiguration>();
        this.plugin = plugin;
    }

    public void reload() {
        try {
            this.locales.put(this.plugin.getConfigManager().Lang.toLowerCase(), new YmlMaker((JavaPlugin)this.plugin, "Translations" + File.separator + "Locale_" + this.plugin.getConfigManager().Lang + ".yml").getConfig());
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private FileConfiguration getEN() {
        if (this.locales.get("en") == null) {
            try {
                this.locales.put("en", new YmlMaker((JavaPlugin)this.plugin, "Translations" + File.separator + "Locale_EN.yml").getConfig());
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return this.locales.get("en");
    }

    public boolean containsKey(final String s) {
        return this.containsLKey(this.plugin.getConfigManager().Lang, s);
    }

    public boolean containsLKey(String s, final String s2) {
        s = ((s == null) ? this.plugin.getConfigManager().Lang.toLowerCase() : s.toLowerCase());
        final FileConfiguration locale = this.getLocale(s);
        return (locale != null && locale.contains(s2)) || this.getEN().contains(s2);
    }

    @Deprecated
    public String getMessage(final String s, final Object... array) {
        return this.getLMessage(this.plugin.getConfigManager().Lang, s, array);
    }

    public String getLMessage(String s, final String str, final Object... array) {
        s = ((s == null) ? this.plugin.getConfigManager().Lang.toLowerCase() : s.toLowerCase());
        final String string = "Missing locale for " + str + " ";
        String s2 = "";
        final FileConfiguration locale = this.getLocale(s);
        if (locale.isString(str)) {
            s2 = String.valueOf(s2) + locale.getString(str);
        }
        else if (locale.isList(str)) {
            for (final String str2 : locale.getStringList(str)) {
                if (!s2.isEmpty()) {
                    s2 = String.valueOf(s2) + "\n";
                }
                s2 = String.valueOf(s2) + str2;
            }
        }
        else {
            s2 = String.valueOf(s2) + (this.getEN().isString(str) ? this.getEN().getString(str) : string);
        }
        SND SND = null;
        Users Users = null;
        Location location = null;
        final ArrayList<SND> list = new ArrayList<SND>();
        for (final Object o : array) {
            if (o instanceof SND && SND == null) {
                SND = (SND)o;
                list.add((SND)o);
            }
            else if (o instanceof Users && Users == null) {
                Users = (Users)o;
                SND = new SND().setSender(Users).setTarget(Users);
                list.add((SND)o);
            }
            else if (o instanceof Location && location == null) {
                location = (Location)o;
                list.add((SND)o);
            }
            else if (o instanceof Player && SND == null) {
                SND = new SND().setTarget((Player)o).setTarget((Player)o);
            }
        }
        if (location != null) {
            s2 = this.replacePlayer(location, s2);
        }
        if (Users != null && Users.getPlayer(false) != null) {
            final Player player = Users.getPlayer(false);
            if (player != null) {
                s2 = this.replacePlayer("", player, null, s2);
                if (player.getLocation() != null) {
                    s2 = this.replacePlayer(player.getLocation(), s2);
                }
            }
        }
        if (array.length > 0) {
            for (int j = 0; j < array.length; ++j) {
                if (!list.contains(array[j])) {
                    if (array.length >= j + 2) {
                        Object text = array[j + 1];
                        if (text instanceof Boolean) {
                            text = (((Boolean) text) ? LC.info_variables_Enabled.getLocale(new Object[0]) : LC.info_variables_Disabled.getLocale(new Object[0]));
                        }
                        if (text instanceof LC) {
                            text = ((LC)text).getText();
                        }
                        s2 = outReplace(s2, array[j], text);
                    }
                    ++j;
                }
            }
        }
        if (SND != null) {
            s2 = this.updateSND(SND, s2);
        }
        return DBChatColor.translate(this.filterNewLine(s2.replace("!prefix!", LC.info_prefix.getLocale(new Object[0]))));
    }

    private FileConfiguration getLocale(final String s) {
        if (s.length() > 10) {
            return this.getEN();
        }
        if (this.locales.get(s) == null) {
            try {
                if (!new File(this.plugin.getDataFolder(), "Translations" + File.separator + "Locale_" + s.toUpperCase() + ".yml").isFile()) {
                    return this.getEN();
                }
                this.locales.put(s.toUpperCase(), new YmlMaker((JavaPlugin)this.plugin, "Translations" + File.separator + "Locale_" + s.toUpperCase() + ".yml").getConfig());
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return this.locales.getOrDefault(s, this.getEN());
    }

    public String replacePlayer(final String obj, final Location location, String s) {
        if (s == null || location == null) {
            return s;
        }
        s = replace(s, String.valueOf(obj) + "X", location.getBlockX());
        s = replace(s, String.valueOf(obj) + "Y", location.getBlockY());
        s = replace(s, String.valueOf(obj) + "Z", location.getBlockZ());
        s = replace(s, String.valueOf(obj) + "Yaw", (int)location.getYaw());
        s = replace(s, String.valueOf(obj) + "Pitch", (int)location.getPitch());
        if (location.getWorld() != null) {
            s = replace(s, String.valueOf(obj) + "WorldName", location.getWorld().getName());
            s = replace(s, String.valueOf(obj) + "World", location.getWorld().getName());
        }
        return s;
    }

    public String replacePlayer(final Location location, String s) {
        if (s == null || location == null) {
            return s;
        }
        s = replace(s, "x", location.getBlockX());
        s = replace(s, "y", location.getBlockY());
        s = replace(s, "z", location.getBlockZ());
        s = replace(s, "yaw", (int)location.getYaw());
        s = replace(s, "pitch", (int)location.getPitch());
        if (location.getWorld() != null) {
            s = replace(s, "worldName", location.getWorld().getName());
            s = replace(s, "world", location.getWorld().getName());
        }
        return s;
    }

    public String replaceUser(final String s, final Users Users, final String s2) {
        return this.replaceUser(s, Users, null, s2);
    }

    public String replaceUser(final String obj, final Users Users, final Player player, String s) {
        s = replace(s, "serverName", this.plugin.getBungeeCordManager().isBungeeCord() ? this.plugin.getBungeeCordManager().getThisServerName() : DBLib.getInstance().getReflectionManager().getServerName());
        if (s == null || Users == null) {
            return s;
        }
        if (Users.isOnline()) {
            return this.replacePlayer(obj, Users.getPlayer(false), player, s);
        }
        s = replace(s, String.valueOf(obj) + "offon", this.plugin.getOffOn(Users.getPlayer(false), player));
        s = replace(s, String.valueOf(obj) + "Name", Users.getName(false));

        if (obj.isEmpty()) {
            s = replace(s, "playerName", Users.getName(false));
        }
        if (Users.getLogOutLocation() != null) {
            s = this.replacePlayer(obj, Users.getLogOutLocation(), s);
        }
        return s;
    }

    private static String replace(final String s, final Object obj, Object obj2) {
        if (obj == null) {
            return s;
        }
        if (obj2 == null) {
            obj2 = "";
        }
        return s.replaceAll(String.valueOf("(?i)(\\[" + obj + "\\])"), Matcher.quoteReplacement(String.valueOf(obj2)));
    }

    public List<String> updateSND(final SND SND, final List<String> list) {
        for (int i = 0; i < list.size(); ++i) {
            list.set(i, this.updateSND(SND, list.get(i)));
        }
        return list;
    }

    public String updateSND(final SND SND, String s) {
        if (s == null) {
            return null;
        }
        if (!s.contains("[")) {
            s = this.filterNewLine(s);
            return s;
        }
        s = replace(s, "serverName", this.plugin.getBungeeCordManager().isBungeeCord() ? this.plugin.getBungeeCordManager().getThisServerName() : DBLib.getInstance().getReflectionManager().getServerName());
        if (SND.getConsoleSender() != null) {
            String s2 = SND.getConsoleSender().getName();
            if (s2.equalsIgnoreCase("Console") || s2.equalsIgnoreCase(this.plugin.getPlayerManager().getFakeUserName())) {
                s2 = LC.info_Console.getLocale(new Object[0]);
            }
            s = replace(s, "senderName", s2);
            s = replace(s, "senderDisplayName", s2);
            s = replace(s, "senderPrefix", "");
            s = replace(s, "senderSuffix", "");
        }
        if (SND.getPlayerSender() != null) {
            s = this.replacePlayer("sender", SND.getPlayerSender(), SND.getPlayerTarget(), s);
            if (SND.getPlayerSender().getLocation() != null) {
                s = this.replacePlayer(SND.getPlayerSender().getLocation(), s);
            }
        }
        if (SND.getSenderUser() != null) {
            s = this.replaceUser("sender", SND.getSenderUser(), s);
        }
        if (SND.getConsoleTarget() != null) {
            String s3 = SND.getConsoleTarget().getName();
            if (s3.equalsIgnoreCase("Console") || s3.equalsIgnoreCase(this.plugin.getPlayerManager().getFakeUserName())) {
                s3 = LC.info_Console.getLocale(new Object[0]);
            }
            s = replace(s, "targetName", s3);
            s = replace(s, "targetDisplayName", s3);
        }
        if (SND.getPlayerTarget() != null) {
            s = this.replacePlayer("", SND.getPlayerTarget(), SND.getPlayerSender(), s);
            if (SND.getPlayerTarget().getLocation() != null) {
                s = this.replacePlayer(SND.getPlayerTarget().getLocation(), s);
            }
        }
        if (SND.getTargetUser() != null) {
            s = this.replaceUser("", SND.getTargetUser(), SND.getPlayerSender(), s);
        }
        if (SND.getConsoleSource() != null) {
            String s4 = SND.getConsoleSource().getName();
            if (s4.equalsIgnoreCase("Console") || s4.equalsIgnoreCase(this.plugin.getPlayerManager().getFakeUserName())) {
                s4 = LC.info_Console.getLocale(new Object[0]);
            }
            s = replace(s, "sourceName", s4);
            s = replace(s, "sourceDisplayName", s4);
        }
        if (SND.getSenderName() != null) {
            String s5 = SND.getSenderName();
            if (s5.equalsIgnoreCase("Console") || s5.equalsIgnoreCase(this.plugin.getPlayerManager().getFakeUserName())) {
                s5 = LC.info_Console.getLocale(new Object[0]);
            }
            s = replace(s, "senderName", s5);
            s = replace(s, "senderDisplayName", s5);
            s = replace(s, "senderPrefix", "");
            s = replace(s, "senderSuffix", "");
        }
        if (SND.getTargetName() != null) {
            String s6 = SND.getTargetName();
            if (s6.equalsIgnoreCase("Console") || s6.equalsIgnoreCase(this.plugin.getPlayerManager().getFakeUserName())) {
                s6 = LC.info_Console.getLocale(new Object[0]);
            }
            s = replace(s, "Name", s6);
            s = replace(s, "DisplayName", s6);
            s = replace(s, "playerName", s6);
            s = replace(s, "playerDisplayName", s6);
        }
        if (SND.getPlayerSource() != null) {
            s = this.replacePlayer("source", SND.getPlayerSource(), SND.getPlayerTarget(), s);
            if (SND.getPlayerSource().getLocation() != null) {
                s = this.replacePlayer(SND.getPlayerSource().getLocation(), s);
            }
        }
        if (SND.getSourceUser() != null) {
            s = this.replaceUser("source", SND.getSourceUser(), SND.getPlayerSender(), s);
        }
        s = this.filterNewLine(s);
        return s;
    }

    public String filterNewLine(String replace) {
        final Matcher matcher = Language.pattern.matcher(replace);
        while (matcher.find()) {
            if (matcher.group(1) != null && !matcher.group(1).isEmpty()) {
                replace = replace.replace(matcher.group(1), "\n");
            }
        }
        return replace;
    }

    public boolean isList(final String s) {
        return this.isLList(this.plugin.getConfigManager().Lang, s);
    }

    public boolean isLList(String s, final String s2) {
        s = ((s == null) ? this.plugin.getConfigManager().Lang.toLowerCase() : s.toLowerCase());
        final FileConfiguration locale = this.getLocale(s);
        if (locale != null && locale.contains(s2)) {
            return locale.isList(s2);
        }
        return this.getEN().contains(s2) && this.getEN().isList(s2);
    }

    public List<String> getMessageList(final String s, final Object... array) {
        return this.getMessageLList(this.plugin.getConfigManager().Lang, s, array);
    }

    public List<String> getMessageLList(String s, final String str, final Object... array) {
        s = ((s == null) ? this.plugin.getConfigManager().Lang.toLowerCase() : s.toLowerCase());
        final String string = "Missing locale for " + str + " ";
        final FileConfiguration locale = this.getLocale(s);
        List<String> stringList;
        if (locale.isList(str)) {
            stringList = (List<String>)locale.getStringList(str);
        }
        else {
            stringList = (this.getEN().getStringList(str).isEmpty() ? Arrays.asList(string) : this.getEN().getStringList(str));
        }
        if (array != null && array.length > 0) {
            for (int i = 0; i < stringList.size(); ++i) {
                String replace = stringList.get(i);
                for (int j = 0; j < array.length; j += 2) {
                    replace = replace.replace(String.valueOf(array[j]), String.valueOf(array[j + 1]));
                }
                stringList.set(i, DBChatColor.translate(this.filterNewLine(replace)));
            }
        }
        int n = 0;
        final Iterator<String> iterator = stringList.iterator();
        while (iterator.hasNext()) {
            stringList.set(n, DBChatColor.translate(iterator.next().replace("!prefix!", LC.info_prefix.getLocale(new Object[0]))));
            ++n;
        }
        return stringList;
    }

}
