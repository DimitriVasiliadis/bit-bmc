package gg.dropbear.bit.commands;
import gg.dropbear.bit.Main;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Command {

    private CMD cmdClass;
    private String name;
    private CAnnotation annotation;
    private Boolean enabled;
    private Boolean baseEnabled;

    public Command(final CMD cmdClass, final String name, final CAnnotation annotation) {
        this.enabled = null;
        this.baseEnabled = true;
        this.cmdClass = cmdClass;
        this.name = name;
    }

    public CMD getCmdClass() {
        return this.cmdClass;
    }

    public Command setCmdClass(final CMD cmdClass) {
        this.cmdClass = cmdClass;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public Command setName(final String name) {
        this.name = name;
        return this;
    }

    public CAnnotation getAnnotation() {
        return this.annotation;
    }

    public void setAnnotation(final CAnnotation anotation) {
        this.annotation = anotation;
    }

    public String getTranslatedArgs() {
        String message = "";
        final String string = "command." + this.cmdClass.getClass().getSimpleName() + ".help.args";
        if (Main.getInstance().getLM().containsKey(string) && !Main.getInstance().getLM().getMessage(string, new Object[0]).isEmpty()) {
            message = Main.getInstance().getLM().getMessage(string, new Object[0]);
        }
        return message;
    }

    public Boolean getEnabled() {
        return this.enabled;
    }

    public void setEnabled(final Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getBaseEnabled() {
        return this.baseEnabled;
    }

    public void setBaseEnabled(final Boolean baseEnabled) {
        this.baseEnabled = baseEnabled;
    }

}
