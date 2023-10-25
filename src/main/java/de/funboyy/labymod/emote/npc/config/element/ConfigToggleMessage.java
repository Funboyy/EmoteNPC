package de.funboyy.labymod.emote.npc.config.element;

import de.funboyy.labymod.emote.npc.config.Config;

public class ConfigToggleMessage extends ConfigElement<String> {

    public ConfigToggleMessage(final String path, final Getter<Boolean> toggleValue) {
        super(value -> {}, () -> {
            if (toggleValue.get()) {
                return Config.getFile().getString(String.format(path, "enabled"));
            }

            return Config.getFile().getString(String.format(path, "disabled"));
        });
    }

    public String get() {
        return ConfigMessage.format(ConfigString.format(super.get()));
    }

}
