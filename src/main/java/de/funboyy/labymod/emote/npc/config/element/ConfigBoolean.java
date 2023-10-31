package de.funboyy.labymod.emote.npc.config.element;

import de.funboyy.labymod.emote.npc.config.Config;

public class ConfigBoolean extends ConfigElement<Boolean> {

    public ConfigBoolean(final String path) {
        super(path);
    }

    @Override
    public Boolean get() {
        return Config.getFile().getBoolean(super.path);
    }

}
