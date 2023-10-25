package de.funboyy.labymod.emote.npc.config.element;

import de.funboyy.labymod.emote.npc.config.Config;

public class ConfigBoolean extends ConfigElement<Boolean> {

    public ConfigBoolean(final String path) {
        super(value -> {}, () -> Config.getFile().getBoolean(path));
    }

}
