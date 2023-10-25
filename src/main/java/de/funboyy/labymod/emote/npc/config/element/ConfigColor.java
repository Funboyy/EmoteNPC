package de.funboyy.labymod.emote.npc.config.element;

import de.funboyy.labymod.emote.npc.config.Config;
import net.md_5.bungee.api.ChatColor;

public class ConfigColor extends ConfigElement<ChatColor> {

    public ConfigColor(final String path) {
        super(value -> {}, () -> {
            final String value = Config.getFile().getString(path);

            if (value.charAt(0) != ChatColor.COLOR_CHAR || value.length() != 2) {
                return null;
            }

            return ChatColor.getByChar(value.toLowerCase().charAt(1));
        });
    }

}
