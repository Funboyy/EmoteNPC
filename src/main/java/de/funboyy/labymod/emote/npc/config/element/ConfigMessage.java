package de.funboyy.labymod.emote.npc.config.element;

import de.funboyy.labymod.emote.npc.config.Config;

public class ConfigMessage extends ConfigString {

    public ConfigMessage(final String path) {
        super(path);
    }

    public String get() {
        return format(super.get());
    }

    public static String format(final String value) {
        return value.replace("%prefix%", Config.PREFIX.get());
    }

}
