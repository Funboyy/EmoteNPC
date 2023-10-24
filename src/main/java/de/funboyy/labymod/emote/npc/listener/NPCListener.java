package de.funboyy.labymod.emote.npc.listener;

import de.funboyy.labymod.emote.npc.EmoteNPCPlugin;
import de.funboyy.labymod.emote.npc.config.Config;
import de.funboyy.labymod.emote.npc.emote.EmoteManager;
import de.funboyy.labymod.emote.npc.user.User;
import de.funboyy.labymod.emote.npc.user.UserManager;
import de.funboyy.version.helper.Version;
import de.funboyy.version.helper.custom.data.CustomData;
import de.funboyy.version.helper.custom.data.CustomItem;
import de.funboyy.version.helper.npc.manager.event.NPCInteractEvent;
import de.funboyy.version.helper.npc.manager.event.NPCRemoveEvent;
import de.funboyy.version.helper.npc.manager.event.NPCSpawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class NPCListener implements Listener {

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
        final CustomItem customItem = CustomItem.fromItem(item);
        final CustomData data = customItem.getData();

        if (data == null || !data.hasKey("Event")) {
            return;
        }

        final String eventNBT = data.getString("Event");

        if (eventNBT.equals("playEmote")) {
            player.closeInventory();
            player.playSound(player.getLocation(), getSound(), 1, 1);

            if (!data.hasKey("EmoteID")) {
                return;
            }

            final int emoteId = data.getInt("EmoteID");

            user.playEmote(-1);
            Bukkit.getScheduler().runTaskLater(EmoteNPCPlugin.getInstance(), () -> {
                user.playEmote(emoteId);
                player.sendMessage(Config.getInstance().getPlayEmote().replace("%emote%",
                        EmoteManager.getInstance().getEmoteById(emoteId).getName()));
            }, 2);
            return;
        }

        if (eventNBT.equals("nextPage")) {
            player.playSound(player.getLocation(), getSound(), 1, 1);

            if (!data.hasKey("Page")) {
                return;
            }

            user.openInventory(data.getInt("Page") + 1);
            return;
        }

        if (eventNBT.equals("lastPage")) {
            player.playSound(player.getLocation(), getSound(), 1, 1);

            if (!data.hasKey("Page")) {
                return;
            }

            user.openInventory(data.getInt("Page") - 1);
            return;
        }

        if (eventNBT.equals("stopEmote")) {
            player.playSound(player.getLocation(), getSound(), 1, 1);
            user.playEmote(-1);
        }
    }

    @EventHandler
    public void onInteract(final NPCInteractEvent event) {
        final Player player = event.getPlayer();
        final User user = UserManager.getInstance().getUser(player);

        if (user == null) {
            return;
        }

        if (event.getAction() == NPCInteractEvent.Action.ATTACK) {
            user.playEmote(149);
            return;
        }

        if (!user.isPermitted()) {
            if (user.getDelay() > System.currentTimeMillis()) {
                return;
            }

            user.setDelay(System.currentTimeMillis() + 100);
            player.sendMessage(Config.getInstance().getLabyMod());
            return;
        }

        Bukkit.getScheduler().runTaskLater(EmoteNPCPlugin.getInstance(), () -> {
            if (player.getOpenInventory().getTitle().equals(Config.getInstance().getInventory())) {
                return;
            }

            UserManager.getInstance().getUser(player).openInventory(1);
            player.playSound(player.getLocation(), getSound(), 1f, 1f);
        }, 1);
    }

    @EventHandler
    public void onSpawn(final NPCSpawnEvent event) {
        if (Config.getInstance().debug()) {
            EmoteNPCPlugin.getInstance().getLogger().info("Spawned NPC for " + event.getPlayer().getName());
        }
    }

    @EventHandler
    public void onRemove(final NPCRemoveEvent event) {
        if (Config.getInstance().debug()) {
            EmoteNPCPlugin.getInstance().getLogger().info("Removed NPC for " + event.getPlayer().getName());
        }
    }

    private Sound getSound() {
        return Version.getVersionId() <= Version.v1_8_R3 ? Sound.valueOf("CLICK") : Sound.valueOf("UI_BUTTON_CLICK");
    }

}
