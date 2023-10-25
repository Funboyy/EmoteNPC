package de.funboyy.labymod.emote.npc.command.subcommand;

import de.funboyy.labymod.emote.npc.config.Config;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.CommandSender;

public class ReloadSubCommand implements SubCommand {

    private static final List<String> COMMANDS = Arrays.asList("reload", "rl");

    @Override
    public void onCommand(final CommandSender sender, final String[] args) {
        Config.load();
        Config.update();
        sender.sendMessage(Config.COMMAND_RELOAD.get());
    }

    @Override
    public boolean check(final String[] args) {
        if (args.length != 1) {
            return false;
        }

        return COMMANDS.contains(args[0].toLowerCase());
    }

    @Override
    public String getPermission() {
        return "emote.command.reload";
    }

}
