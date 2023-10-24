package de.funboyy.labymod.emote.npc.config;

import de.funboyy.labymod.emote.npc.EmoteNPCPlugin;
import de.funboyy.version.helper.npc.Setting;
import lombok.AllArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

@AllArgsConstructor
public class Config {

    private static Config instance;

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config(EmoteNPCPlugin.getInstance().getConfig());
        }
        return instance;
    }
    
    private FileConfiguration config;
    
    public void reload() {
        this.config = EmoteNPCPlugin.getInstance().getConfig();
    }

    private String replace(final String message) {
        if (message == null) {
            return "";
        }

        return ChatColor.translateAlternateColorCodes('&', message.replace("%prefix%", getPrefix()));
    }

    private String getPrefix() {
        return this.config.getString("prefix");
    }

    // Settings

    public String prefix() {
        return replace(this.config.getString("npc.name.prefix"));
    }

    public String name() {
        return replace(this.config.getString("npc.name.name"));
    }

    public ChatColor nameColor() {
        final String value = replace(this.config.getString("npc.name.nameColor"));

        if (value.charAt(0) != ChatColor.COLOR_CHAR || value.length() != 2) {
            return null;
        }

        return ChatColor.getByChar(value.toLowerCase().charAt(1));
    }

    public String suffix() {
        return replace(this.config.getString("npc.name.suffix"));
    }
    
    public Setting setting() {
        return new Setting(this.config.getBoolean("settings.sneak"), this.config.getBoolean("settings.look-close"));
    }

    public Location getLocation() {
        final String name = this.config.getString("npc.world");
        final World world = name == null? Bukkit.getWorlds().get(0) : Bukkit.getWorld(name);
        final double x = this.config.getDouble("npc.x");
        final double y = this.config.getDouble("npc.y");
        final double z = this.config.getDouble("npc.z");
        final float yaw = (float) this.config.getDouble("npc.yaw");
        final float pitch = (float) this.config.getDouble("npc.pitch");

        return new Location(world, x, y, z, yaw, pitch);
    }

    public String getLabyMod4Only() {
        return replace(this.config.getString("npc.labymod4-only"));
    }

    // Inventory

    public String getInventory() {
        return replace(this.config.getString("inventory.title"));
    }

    public String getPage() {
        return replace(this.config.getString("inventory.pages"));
    }

    public String getNextPage() {
        return replace(this.config.getString("inventory.next-page"));
    }

    public String getLastPage() {
        return replace(this.config.getString("inventory.last-page"));
    }

    public String getStopEmote() {
        return replace(this.config.getString("inventory.stop-emote"));
    }

    // Messages

    public String getPlayEmote() {
        return replace(this.config.getString("messages.play-emote"));
    }

    public String getReload() {
        return replace(this.config.getString("messages.reload"));
    }

    public String getSetLocation() {
        return replace(this.config.getString("messages.set.location"));
    }

    public String getToggleLookCloseEnabled() {
        return replace(this.config.getString("messages.toggle.look-close.enabled"));
    }

    public String getToggleLookCloseDisabled() {
        return replace(this.config.getString("messages.toggle.look-close.disabled"));
    }

    public String getToggleSneakEnabled() {
        return replace(this.config.getString("messages.toggle.sneak.enabled"));
    }

    public String getToggleSneakDisabled() {
        return replace(this.config.getString("messages.toggle.sneak.disabled"));
    }

    public String getLabyMod() {
        return replace(this.config.getString("messages.error.labymod"));
    }

    public String getOnlyPlayer() {
        return replace(this.config.getString("messages.error.only-player"));
    }

    public String getPermission() {
        return replace(this.config.getString("messages.error.permission"));
    }

    public String getHelpSetLocation() {
        return replace(this.config.getString("messages.help.set.location"));
    }

    public String getHelpToggleLookClose() {
        return replace(this.config.getString("messages.help.toggle.look-close"));
    }

    public String getHelpToggleSneak() {
        return replace(this.config.getString("messages.help.toggle.sneak"));
    }

    public String getHelpReload() {
        return replace(this.config.getString("messages.help.reload"));
    }

    // Debug

    public boolean debug() {
        return this.config.getBoolean("debug");
    }

}
