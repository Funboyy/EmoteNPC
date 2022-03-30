package de.funboyy.labymod.emote.npc.listener;

import de.funboyy.labymod.emote.npc.user.User;
import de.funboyy.labymod.emote.npc.user.UserManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinQuitListener implements Listener {

    @EventHandler
    public void handleJoin(final PlayerJoinEvent event) {
        final User user = UserManager.getInstance().register(event.getPlayer());

        if (!user.isNearNPC()) {
            return;
        }

        user.getNpc().spawn();
    }

    @EventHandler
    public void handleQuit(final PlayerQuitEvent event) {
        UserManager.getInstance().unregister(event.getPlayer());
    }
}
