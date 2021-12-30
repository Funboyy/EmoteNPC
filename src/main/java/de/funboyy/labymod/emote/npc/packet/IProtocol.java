package de.funboyy.labymod.emote.npc.packet;

import org.bukkit.entity.Player;

public interface IProtocol {

    void sendBrand(final Player player);

    void sendMessage(final Player player, final String key, final String messageContent);

    void send(final Player player, final String[] key, final Object object);

}
