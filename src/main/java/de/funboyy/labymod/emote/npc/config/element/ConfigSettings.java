package de.funboyy.labymod.emote.npc.config.element;

import de.funboyy.labymod.emote.npc.config.Config;
import de.funboyy.version.helper.npc.Settings;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigSettings extends ConfigElement<Settings> {

    public ConfigSettings(final String path) {
        super(value -> {
            final FileConfiguration config = Config.getFile();

            config.set(String.format(path, "toggleSneak"), value.toggleSneak());
            config.set(String.format(path, "lookClose"), value.lookClose());

            Config.save();
            Config.update();
        },
        () -> {
            final FileConfiguration config = Config.getFile();

            return new Settings(config.getBoolean(String.format(path, "toggleSneak")), config.getBoolean(String.format(path, "lookClose")));
        });
    }

}
