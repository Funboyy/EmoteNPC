package de.funboyy.labymod.emote.npc.listener;

import de.funboyy.labymod.emote.npc.user.User;
import de.funboyy.labymod.emote.npc.user.UserManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinQuitListener implements Listener {

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        UserManager.getInstance().register(event.getPlayer());
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        final User user = UserManager.getInstance().getUser(event.getPlayer());

        if (user != null) {
            UserManager.getInstance().unregister(user);
        }
    }

}
