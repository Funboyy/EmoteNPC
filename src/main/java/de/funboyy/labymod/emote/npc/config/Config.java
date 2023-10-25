package de.funboyy.labymod.emote.npc.config;

import de.funboyy.labymod.emote.npc.EmoteNPCPlugin;
import de.funboyy.labymod.emote.npc.config.element.*;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class Config {

    public static final ConfigString PREFIX = new ConfigString("prefix");

    public static final ConfigString NPC_NAME = new ConfigString("npc.name");
    public static final ConfigLocation NPC_LOCATION = new ConfigLocation("npc.location.%s");
    public static final ConfigSettings NPC_SETTINGS = new ConfigSettings("npc.settings.%s");

    public static final ConfigString SCOREBOARD_PREFIX = new ConfigString("npc.scoreboard.prefix");
    public static final ConfigColor SCOREBOARD_COLOR = new ConfigColor("npc.scoreboard.color");
    public static final ConfigString SCOREBOARD_SUFFIX = new ConfigString("npc.scoreboard.suffix");

    public static final ConfigString INVENTORY_TITLE = new ConfigString("inventory.title");
    public static final ConfigString ITEM_PAGES = new ConfigString("inventory.item.pages");
    public static final ConfigString ITEM_STOP_EMOTE = new ConfigString("inventory.item.stopEmote");
    public static final ConfigString ITEM_PREVIOUS_PAGE = new ConfigString("inventory.item.previousPage");
    public static final ConfigString ITEM_NEXT_PAGE = new ConfigString("inventory.item.nextPage");
    public static final ConfigString ITEM_WARNING = new ConfigString("inventory.item.labyMod4Only");

    public static final ConfigMessage COMMAND_PERMISSION = new ConfigMessage("command.permission");
    public static final ConfigMessage COMMAND_ONLY_PLAYER = new ConfigMessage("command.onlyPlayer");
    public static final ConfigMessage COMMAND_RELOAD = new ConfigMessage("command.reload");
    public static final ConfigMessage COMMAND_SET_LOCATION = new ConfigMessage("command.set.location");
    public static final ConfigToggleMessage COMMAND_TOGGLE_SNEAK = new ConfigToggleMessage("command.toggle.sneak.%s",
            () -> NPC_SETTINGS.get().toggleSneak());
    public static final ConfigToggleMessage COMMAND_TOGGLE_LOOK_CLOSE = new ConfigToggleMessage("command.toggle.lookClose.%s",
            () -> NPC_SETTINGS.get().lookClose());
    public static final ConfigMessageList COMMAND_HELP = new ConfigMessageList("command.help");

    public static final ConfigMessage MESSAGE_PLAY_EMOTE = new ConfigMessage("message.playEmote");
    public static final ConfigMessage MESSAGE_REQUIRES_LABYMOD = new ConfigMessage("message.requiresLabyMod");

    public static final ConfigBoolean DEBUG = new ConfigBoolean("debug");

    public static FileConfiguration getFile() {
        return EmoteNPCPlugin.getInstance().getConfig();
    }

    public static void load() {
        final Plugin plugin = EmoteNPCPlugin.getInstance();

        plugin.saveDefaultConfig();

        plugin.getConfig().options().copyDefaults(true);
        plugin.reloadConfig();
    }

    public static void update() {
        EmoteNPCPlugin.getInstance().getManager().getNpcs().forEach(npc -> {
            npc.getTeam().setPrefix(SCOREBOARD_PREFIX.get());
            npc.getTeam().setSuffix(SCOREBOARD_SUFFIX.get());

            final ChatColor color = SCOREBOARD_COLOR.get();
            if (color != null) {
                npc.getTeam().setColor(color);
            }

            npc.setSettings(NPC_SETTINGS.get());
            npc.setLocation(NPC_LOCATION.get());
            npc.setName(NPC_NAME.get());
            npc.respawn();
        });
    }

    public static void save() {
        EmoteNPCPlugin.getInstance().saveConfig();
    }

}
