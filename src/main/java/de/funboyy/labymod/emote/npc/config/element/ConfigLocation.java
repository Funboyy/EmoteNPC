package de.funboyy.labymod.emote.npc.config.element;

import de.funboyy.labymod.emote.npc.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigLocation extends ConfigElement<Location> {

    public ConfigLocation(final String path) {
        super(value -> {
            final FileConfiguration config = Config.getFile();

            config.set(String.format(path, "world"), value.getWorld().getName());
            config.set(String.format(path, "x"), value.getX());
            config.set(String.format(path, "y"), value.getY());
            config.set(String.format(path, "z"), value.getZ());
            config.set(String.format(path, "yaw"), value.getYaw());
            config.set(String.format(path, "pitch"), value.getPitch());

            Config.save();
            Config.update();
        },
        () -> {
            final FileConfiguration config = Config.getFile();

            final World world = Bukkit.getWorld(config.getString(String.format(path, "world")));
            final double x = config.getDouble(String.format(path, "x"));
            final double y = config.getDouble(String.format(path, "y"));
            final double z = config.getDouble(String.format(path, "z"));
            final float yaw = (float) config.getDouble(String.format(path, "yaw"));
            final float pitch = (float) config.getDouble(String.format(path, "pitch"));

            return new Location(world == null ? Bukkit.getWorlds().get(0) : world, x, y, z, yaw, pitch);
        });
    }

}
