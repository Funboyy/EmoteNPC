package de.funboyy.labymod.emote.npc.config;

import de.funboyy.labymod.emote.npc.EmoteNPCPlugin;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

public class Config {

    private static Config instance;

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    private String replace(final String message) {
        return ChatColor.translateAlternateColorCodes('&', message.replace("%prefix%", getPrefix()));
    }

    private String getPrefix() {
        return EmoteNPCPlugin.getInstance().getConfig().getString("prefix");
    }

    // Settings

    public String prefix() {
        return replace(EmoteNPCPlugin.getInstance().getConfig().getString("npc.name.prefix"));
    }

    public String name() {
        return replace(EmoteNPCPlugin.getInstance().getConfig().getString("npc.name.name"));
    }

    public String suffix() {
        return replace(EmoteNPCPlugin.getInstance().getConfig().getString("npc.name.suffix"));
    }

    public boolean lookClose() {
        return EmoteNPCPlugin.getInstance().getConfig().getBoolean("settings.look-close");
    }

    public boolean sneak() {
        return EmoteNPCPlugin.getInstance().getConfig().getBoolean("settings.sneak");
    }

    public Location getLocation() {
        final FileConfiguration config = EmoteNPCPlugin.getInstance().getConfig();
        final World world = Bukkit.getWorld(config.getString("npc.world"));
        final double x = config.getDouble("npc.x");
        final double y = config.getDouble("npc.y");
        final double z = config.getDouble("npc.z");
        final float yaw = (float) config.getDouble("npc.yaw");
        final float pitch = (float) config.getDouble("npc.pitch");

        return new Location(world, x, y, z, yaw, pitch);
    }

    // Inventory

    public String getInventory() {
        return replace(EmoteNPCPlugin.getInstance().getConfig().getString("inventory.title"));
    }

    public String getPage() {
        return replace(EmoteNPCPlugin.getInstance().getConfig().getString("inventory.pages"));
    }

    public String getNextPage() {
        return replace(EmoteNPCPlugin.getInstance().getConfig().getString("inventory.next-page"));
    }

    public String getLastPage() {
        return replace(EmoteNPCPlugin.getInstance().getConfig().getString("inventory.last-page"));
    }

    public String getStopEmote() {
        return replace(EmoteNPCPlugin.getInstance().getConfig().getString("inventory.stop-emote"));
    }

    // Messages

    public String getPlayEmote() {
        return replace(EmoteNPCPlugin.getInstance().getConfig().getString("messages.play-emote"));
    }

    public String getReload() {
        return replace(EmoteNPCPlugin.getInstance().getConfig().getString("messages.reload"));
    }

    public String getSetLocation() {
        return replace(EmoteNPCPlugin.getInstance().getConfig().getString("messages.set.location"));
    }

    public String getToggleLookCloseEnabled() {
        return replace(EmoteNPCPlugin.getInstance().getConfig().getString("messages.toggle.look-close.enabled"));
    }

    public String getToggleLookCloseDisabled() {
        return replace(EmoteNPCPlugin.getInstance().getConfig().getString("messages.toggle.look-close.disabled"));
    }

    public String getToggleSneakEnabled() {
        return replace(EmoteNPCPlugin.getInstance().getConfig().getString("messages.toggle.sneak.enabled"));
    }

    public String getToggleSneakDisabled() {
        return replace(EmoteNPCPlugin.getInstance().getConfig().getString("messages.toggle.sneak.disabled"));
    }

    public String getLabyMod() {
        return replace(EmoteNPCPlugin.getInstance().getConfig().getString("messages.error.labymod"));
    }

    public String getOnlyPlayer() {
        return replace(EmoteNPCPlugin.getInstance().getConfig().getString("messages.error.only-player"));
    }

    public String getPermission() {
        return replace(EmoteNPCPlugin.getInstance().getConfig().getString("messages.error.permission"));
    }

    public String getHelpSetLocation() {
        return replace(EmoteNPCPlugin.getInstance().getConfig().getString("messages.help.set.location"));
    }

    public String getHelpToggleLookClose() {
        return replace(EmoteNPCPlugin.getInstance().getConfig().getString("messages.help.toggle.look-close"));
    }

    public String getHelpToggleSneak() {
        return replace(EmoteNPCPlugin.getInstance().getConfig().getString("messages.help.toggle.sneak"));
    }

    public String getHelpReload() {
        return replace(EmoteNPCPlugin.getInstance().getConfig().getString("messages.help.reload"));
    }

    // Debug

    public boolean debug() {
        return EmoteNPCPlugin.getInstance().getConfig().getBoolean("debug");
    }

}
