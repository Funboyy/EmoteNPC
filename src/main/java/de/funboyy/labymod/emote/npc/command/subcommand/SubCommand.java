package de.funboyy.labymod.emote.npc.command.subcommand;

import org.bukkit.command.CommandSender;

public interface SubCommand {

    void onCommand(final CommandSender sender, final String[] args);

    boolean check(final String[] args);

    String getPermission();

}
