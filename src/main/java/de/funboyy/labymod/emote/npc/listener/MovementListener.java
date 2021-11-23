package de.funboyy.labymod.emote.npc.listener;

import de.funboyy.labymod.emote.npc.EmoteNPCPlugin;
import de.funboyy.labymod.emote.npc.config.Config;
import de.funboyy.labymod.emote.npc.user.User;
import de.funboyy.labymod.emote.npc.user.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class MovementListener implements Listener {

    @EventHandler
    public void onChange(final PlayerChangedWorldEvent event) {
        final User user = UserManager.getInstance().getUser(event.getPlayer());

        if (user == null) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(EmoteNPCPlugin.getInstance(), ()-> {
            if (!user.isNearNPC()) {
                if (!user.getNpc().isSpawned()) {
                    return;
                }

                user.getNpc().remove();
                return;
            }

            if (!user.getNpc().isSpawned()) {
                user.getNpc().spawn();
            }
        }, 1);
    }

    @EventHandler
    public void onMove(final PlayerMoveEvent event) {
        final User user = UserManager.getInstance().getUser(event.getPlayer());

        if (user == null) {
            return;
        }

        if (!user.isNearNPC()) {
            if (!user.getNpc().isSpawned()) {
                return;
            }

            user.getNpc().remove();
            return;
        }

        if (!user.getNpc().isSpawned()) {
            user.getNpc().spawn();
        }

        if (!Config.getInstance().lookClose()) {
            return;
        }

        final Location location = user.getPlayer().getLocation();
        final Location npcLocation = location.setDirection(location.subtract(Config.getInstance().getLocation()).toVector());
        user.getNpc().headRotation(npcLocation.getYaw(), npcLocation.getPitch());
    }

    @EventHandler
    public void onSneak(final PlayerToggleSneakEvent event) {
        final User user = UserManager.getInstance().getUser(event.getPlayer());

        if (user == null) {
            return;
        }

        if (!user.isNearNPC()) {
            return;
        }

        if (!Config.getInstance().sneak()) {
            return;
        }

        user.getNpc().sneak(event.isSneaking());
    }

}
