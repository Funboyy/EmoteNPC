package de.funboyy.labymod.emote.npc.command;

import de.funboyy.labymod.emote.npc.command.subcommand.*;
import de.funboyy.labymod.emote.npc.config.Config;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class EmoteCommand implements CommandExecutor {

    private final List<SubCommand> subCommands;

    public EmoteCommand() {
        this.subCommands = Arrays.asList(
                new ReloadSubCommand(),
                new SetLocationSubCommand(),
                new ToggleSneakSubCommand(),
                new ToggleLookCloseSubCommand()
        );
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!sender.hasPermission("emote.command")) {
            sender.sendMessage(Config.COMMAND_PERMISSION.get());
            return true;
        }

        final Optional<SubCommand> optional = this.subCommands.stream().filter(subCommand -> subCommand.check(args)).findFirst();

        if (!optional.isPresent()) {
            Config.COMMAND_HELP.get().forEach(sender::sendMessage);
            return true;
        }

        if (!sender.hasPermission(optional.get().getPermission())) {
            sender.sendMessage(Config.COMMAND_PERMISSION.get());
            return true;
        }

        optional.get().onCommand(sender, args);
        return true;
    }

}
