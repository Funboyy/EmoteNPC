package de.funboyy.labymod.emote.npc.config.element;

import de.funboyy.labymod.emote.npc.config.Config;
import net.md_5.bungee.api.ChatColor;

public class ConfigString extends ConfigElement<String> {

    public ConfigString(final String path) {
        super(value -> {}, () -> Config.getFile().getString(path));
    }

    public String get() {
        return format(super.get());
    }

    public static String format(final String value) {
        if (value == null) {
            return "";
        }

        return ChatColor.translateAlternateColorCodes('&', value);
    }

}
