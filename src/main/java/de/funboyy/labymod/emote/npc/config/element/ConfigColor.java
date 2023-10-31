package de.funboyy.labymod.emote.npc.config.element;

import de.funboyy.labymod.emote.npc.config.Config;
import net.md_5.bungee.api.ChatColor;

public class ConfigColor extends ConfigElement<ChatColor> {

    public ConfigColor(final String path) {
        super(path);
    }

    @Override
    public void set(final ChatColor color) {
        Config.getFile().set(super.path, color.toString());
    }

    @Override
    public ChatColor get() {
        final String value = Config.getFile().getString(super.path);

        if (value.charAt(0) != ChatColor.COLOR_CHAR || value.length() != 2) {
            return null;
        }

        return ChatColor.getByChar(value.toLowerCase().charAt(1));
    }

}
