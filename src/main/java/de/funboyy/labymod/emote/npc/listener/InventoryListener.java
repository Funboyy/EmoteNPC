package de.funboyy.labymod.emote.npc.listener;

import de.funboyy.labymod.emote.npc.EmoteNPCPlugin;
import de.funboyy.labymod.emote.npc.config.Config;
import de.funboyy.labymod.emote.npc.emote.EmoteManager;
import de.funboyy.labymod.emote.npc.user.User;
import de.funboyy.labymod.emote.npc.user.UserManager;
import de.funboyy.labymod.emote.npc.utils.NMSReflection;
import de.funboyy.labymod.emote.npc.utils.Versions;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryListener implements Listener {

    @EventHandler
    public void onClick(final InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        final User user = UserManager.getInstance().getUser(player);

        if (user == null) {
            return;
        }

        if (event.getClickedInventory() == null) {
            return;
        }

        if (!event.getView().getTitle().equals(Config.getInstance().getInventory())) {
            return;
        }

        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        final ItemStack item = event.getCurrentItem();
        final String eventNBT = NMSReflection.getInstance().getNBT(item, "Event");

        if (eventNBT == null) {
            return;
        }

        if (eventNBT.equals("playEmote")) {
            player.closeInventory();
            player.playSound(player.getLocation(), Versions.getInstance().getSound(), 1, 1);

            final String emote = NMSReflection.getInstance().getNBT(item, "EmoteID");
            if (emote == null) {
                return;
            }

            final int emoteId = Integer.parseInt(emote);

            user.playEmote(-1);
            Bukkit.getScheduler().runTaskLater(EmoteNPCPlugin.getInstance(), () -> {
                user.playEmote(emoteId);
                player.sendMessage(Config.getInstance().getPlayEmote().replace("%emote%",
                        EmoteManager.getInstance().getEmoteById(emoteId).getName()));
            }, 2);

            return;
        }

        if (eventNBT.equals("nextPage")) {
            player.playSound(player.getLocation(), Versions.getInstance().getSound(), 1, 1);

            final String page = NMSReflection.getInstance().getNBT(item, "Page");
            if (page == null) {
                return;
            }

            user.openInventory(Integer.parseInt(page) + 1);

            return;
        }

        if (eventNBT.equals("lastPage")) {
            player.playSound(player.getLocation(), Versions.getInstance().getSound(), 1, 1);

            final String page = NMSReflection.getInstance().getNBT(item, "Page");
            if (page == null) {
                return;
            }

            user.openInventory(Integer.parseInt(page) - 1);

            return;
        }

        if (eventNBT.equals("stopEmote")) {
            player.playSound(player.getLocation(), Versions.getInstance().getSound(), 1, 1);
            user.playEmote(-1);
        }
    }

}
