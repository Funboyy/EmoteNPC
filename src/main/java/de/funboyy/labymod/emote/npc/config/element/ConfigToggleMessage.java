package de.funboyy.labymod.emote.npc.config.element;

import de.funboyy.labymod.emote.npc.config.Config;
import java.util.function.Supplier;

public class ConfigToggleMessage extends ConfigElement<String> {

    private final Supplier<Boolean> supplier;

    public ConfigToggleMessage(final String path, final Supplier<Boolean> supplier) {
        super(path);
        this.supplier = supplier;
    }

    @Override
    public void set(final String message) {
        Config.getFile().set(String.format(super.path, this.supplier.get() ? "enabled" : "disabled"), message);

        super.save();
    }

    @Override
    public String get() {
        final String message = Config.getFile().getString(String.format(super.path, this.supplier.get() ? "enabled" : "disabled"));

        return ConfigMessage.format(ConfigString.format(message));
    }

}
