package gg.dropbear.bit.locale;

import gg.dropbear.bit.Main;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class YmlMaker {

    Main Plugin;
    public String fileName;
    private JavaPlugin plugin;
    public File ConfigFile;
    private FileConfiguration Configuration;

    public YmlMaker(final JavaPlugin plugin, final String fileName) {
        if (plugin == null) {
            throw new IllegalArgumentException("plugin cannot be null");
        }
        this.plugin = plugin;
        this.fileName = fileName;
        final File dataFolder = plugin.getDataFolder();
        if (dataFolder == null) {
            throw new IllegalStateException();
        }
        this.ConfigFile = new File(dataFolder.toString() + File.separatorChar + this.fileName);
    }

    public void reloadConfig() {
        this.Configuration = (FileConfiguration) YamlConfiguration.loadConfiguration(this.ConfigFile);
        final InputStream defConfigStream = this.plugin.getResource(this.fileName);
        if (defConfigStream != null) {
            final YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(this.ConfigFile);
            this.Configuration.setDefaults((org.bukkit.configuration.Configuration)defConfig);
        }
        if (defConfigStream != null) {
            try {
                defConfigStream.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public FileConfiguration getConfig() {
        if (this.Configuration == null) {
            this.reloadConfig();
        }
        return this.Configuration;
    }

}
