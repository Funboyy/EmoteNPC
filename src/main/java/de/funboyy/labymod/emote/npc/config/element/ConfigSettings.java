package de.funboyy.labymod.emote.npc.config.element;

import de.funboyy.labymod.emote.npc.config.Config;
import de.funboyy.version.helper.npc.Settings;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigSettings extends ConfigElement<Settings> {

    public ConfigSettings(final String path) {
        super(path);
    }

    @Override
    public void set(final Settings settings) {
        final FileConfiguration config = Config.getFile();

        config.set(String.format(super.path, "toggleSneak"), settings.toggleSneak());
        config.set(String.format(super.path, "lookClose"), settings.lookClose());

        super.save();
    }

    @Override
    public Settings get() {
        final FileConfiguration config = Config.getFile();

        return new Settings(config.getBoolean(String.format(super.path, "toggleSneak")),
                config.getBoolean(String.format(super.path, "lookClose")));
    }

}
