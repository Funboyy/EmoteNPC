package de.funboyy.labymod.emote.npc.config.element;

import de.funboyy.labymod.emote.npc.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigLocation extends ConfigElement<Location> {

    public ConfigLocation(final String path) {
        super(path);
    }

    @Override
    public void set(final Location location) {
        final FileConfiguration config = Config.getFile();

        config.set(String.format(super.path, "world"), location.getWorld().getName());
        config.set(String.format(super.path, "x"), location.getX());
        config.set(String.format(super.path, "y"), location.getY());
        config.set(String.format(super.path, "z"), location.getZ());
        config.set(String.format(super.path, "yaw"), location.getYaw());
        config.set(String.format(super.path, "pitch"), location.getPitch());

        super.save();
    }

    @Override
    public Location get() {
        final FileConfiguration config = Config.getFile();

        final World world = Bukkit.getWorld(config.getString(String.format(super.path, "world")));
        final double x = config.getDouble(String.format(super.path, "x"));
        final double y = config.getDouble(String.format(super.path, "y"));
        final double z = config.getDouble(String.format(super.path, "z"));
        final float yaw = (float) config.getDouble(String.format(super.path, "yaw"));
        final float pitch = (float) config.getDouble(String.format(super.path, "pitch"));

        return new Location(world == null ? Bukkit.getWorlds().get(0) : world, x, y, z, yaw, pitch);
    }

}
