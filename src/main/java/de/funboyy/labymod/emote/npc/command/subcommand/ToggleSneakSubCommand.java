package de.funboyy.labymod.emote.npc.command.subcommand;

import de.funboyy.labymod.emote.npc.config.Config;
import de.funboyy.version.helper.npc.Settings;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.CommandSender;

public class ToggleSneakSubCommand implements SubCommand {

    private static final List<String> COMMANDS = Arrays.asList("sneaking", "sneak");

    @Override
    public void onCommand(final CommandSender sender, final String[] args) {
        final Settings settings = Config.NPC_SETTINGS.get();

        Config.NPC_SETTINGS.set(new Settings(!settings.toggleSneak(), settings.lookClose()));
        sender.sendMessage(Config.COMMAND_TOGGLE_SNEAK.get());
    }

    @Override
    public boolean check(final String[] args) {
        if (args.length != 2) {
            return false;
        }

        if (!args[0].equalsIgnoreCase("toggle")) {
            return false;
        }

        return COMMANDS.contains(args[1].toLowerCase());
    }

    @Override
    public String getPermission() {
        return "emote.command.toggle";
    }

}
