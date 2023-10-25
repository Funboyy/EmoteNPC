package de.funboyy.labymod.emote.npc.command.subcommand;

import de.funboyy.labymod.emote.npc.config.Config;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetLocationSubCommand implements SubCommand {

    private static final List<String> COMMANDS = Arrays.asList("location", "loc");

    @Override
    public void onCommand(final CommandSender sender, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Config.COMMAND_ONLY_PLAYER.get());
            return;
        }

        final Player player = (Player) sender;

        Config.NPC_LOCATION.set(player.getLocation());
        player.sendMessage(Config.COMMAND_SET_LOCATION.get());
    }

    @Override
    public boolean check(final String[] args) {
        if (args.length != 2) {
            return false;
        }

        if (!args[0].equalsIgnoreCase("set")) {
            return false;
        }

        return COMMANDS.contains(args[1].toLowerCase());
    }

    @Override
    public String getPermission() {
        return "emote.command.set";
    }

}
