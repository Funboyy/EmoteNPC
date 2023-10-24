package de.funboyy.labymod.emote.npc.command;

import de.funboyy.labymod.emote.npc.EmoteNPCPlugin;
import de.funboyy.labymod.emote.npc.config.Config;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class EmoteCommand implements CommandExecutor {

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!sender.hasPermission("emote.command")) {
            sender.sendMessage(Config.getInstance().getPermission());
            return true;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("rl") || args[0].equalsIgnoreCase("reload")) {
                reload(sender);
                return true;
            }
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("set")) {
                if (args[1].equalsIgnoreCase("loc") || args[1].equalsIgnoreCase("location")) {
                    setLocation(sender);
                    return true;
                }
            }

            if (args[0].equalsIgnoreCase("toggle")) {
                final String setting = args[1].toLowerCase();

                if (setting.equals("lookclose")) {
                    toggleLookClose(sender);
                    return true;
                }

                if (setting.equals("sneak") || setting.equals("sneaking")) {
                    toggleSneaking(sender);
                    return true;
                }
            }
        }

        sender.sendMessage(Config.getInstance().getHelpSetLocation());
        sender.sendMessage(Config.getInstance().getHelpToggleLookClose());
        sender.sendMessage(Config.getInstance().getHelpToggleSneak());
        sender.sendMessage(Config.getInstance().getHelpReload());
        return true;
    }

    private void reload(final CommandSender sender) {
        if (!sender.hasPermission("emote.command.reload")) {
            sender.sendMessage(Config.getInstance().getPermission());
            return;
        }

        EmoteNPCPlugin.getInstance().updateConfig();
        sender.sendMessage(Config.getInstance().getReload());
    }

    private void setLocation(final CommandSender sender) {
        if (!sender.hasPermission("emote.command.set")) {
            sender.sendMessage(Config.getInstance().getPermission());
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(Config.getInstance().getOnlyPlayer());
            return;
        }

        final Player player = (Player) sender;

        final FileConfiguration config = EmoteNPCPlugin.getInstance().getConfig();
        config.set("npc.world", player.getWorld().getName());
        config.set("npc.x", player.getLocation().getX());
        config.set("npc.y", player.getLocation().getY());
        config.set("npc.z", player.getLocation().getZ());
        config.set("npc.yaw", player.getLocation().getYaw());
        config.set("npc.pitch", player.getLocation().getPitch());

        EmoteNPCPlugin.getInstance().saveConfig();
        EmoteNPCPlugin.getInstance().updateSetting();

        player.sendMessage(Config.getInstance().getSetLocation());
    }

    private void toggleLookClose(final CommandSender sender) {
        if (!sender.hasPermission("emote.command.toggle")) {
            sender.sendMessage(Config.getInstance().getPermission());
            return;
        }

        final boolean lookClose = Config.getInstance().setting().lookClose();

        EmoteNPCPlugin.getInstance().getConfig().set("settings.look-close", !lookClose);
        EmoteNPCPlugin.getInstance().saveConfig();
        EmoteNPCPlugin.getInstance().updateSetting();

        sender.sendMessage(!lookClose ?
                Config.getInstance().getToggleLookCloseEnabled() : Config.getInstance().getToggleLookCloseDisabled());
    }

    private void toggleSneaking(final CommandSender sender) {
        if (!sender.hasPermission("emote.command.toggle")) {
            sender.sendMessage(Config.getInstance().getPermission());
            return;
        }

        final boolean sneaking = Config.getInstance().setting().toggleSneak();

        EmoteNPCPlugin.getInstance().getConfig().set("settings.sneak", !sneaking);
        EmoteNPCPlugin.getInstance().saveConfig();
        EmoteNPCPlugin.getInstance().updateSetting();

        sender.sendMessage(!sneaking ?
                Config.getInstance().getToggleSneakEnabled() : Config.getInstance().getToggleSneakDisabled());
    }
}
