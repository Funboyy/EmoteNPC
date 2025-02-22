package de.funboyy.labymod.emote.npc.listener;

import de.funboyy.labymod.emote.npc.EmoteNPCPlugin;
import de.funboyy.labymod.emote.npc.config.Config;
import de.funboyy.labymod.emote.npc.emote.EmoteManager;
import de.funboyy.labymod.emote.npc.user.User;
import de.funboyy.labymod.emote.npc.user.UserManager;
import de.funboyy.labymod.emote.npc.utils.ClickAction;
import de.funboyy.version.helper.Version;
import de.funboyy.version.helper.custom.data.CustomData;
import de.funboyy.version.helper.custom.data.CustomItem;
import de.funboyy.version.helper.npc.manager.event.NPCInteractEvent;
import de.funboyy.version.helper.npc.manager.event.NPCRemoveEvent;
import de.funboyy.version.helper.npc.manager.event.NPCSpawnEvent;
import java.time.Instant;
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
        if (event.getClickedInventory() == null) {
            return;
        }

        if (!event.getView().getTitle().equals(Config.INVENTORY_TITLE.get())) {
            return;
        }

        final Player player = (Player) event.getWhoClicked();
        final User user = UserManager.getInstance().getUser(player);

        if (user == null) {
            return;
        }

        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        final ItemStack item = event.getCurrentItem();
        final CustomItem customItem = CustomItem.fromItem(item);
        final CustomData data = customItem.getData();

        if (data == null || !data.hasKey(ClickAction.ACTION_KEY)) {
            return;
        }

        final ClickAction action = ClickAction.getByString(data.getString(ClickAction.ACTION_KEY));

        if (action == ClickAction.PLAY_EMOTE) {
            if (!data.hasKey(ClickAction.EMOTE_KEY)) {
                return;
            }

            final int emoteId = data.getInt(ClickAction.EMOTE_KEY);

            player.playSound(player.getLocation(), this.getSound(), 1, 1);
            player.closeInventory();
            user.playEmote(-1);

            Bukkit.getScheduler().runTaskLater(EmoteNPCPlugin.getInstance(), () -> {
                user.playEmote(emoteId);
                player.sendMessage(Config.MESSAGE_PLAY_EMOTE.get().replace("%emote%",
                        EmoteManager.getInstance().getEmoteById(emoteId).getName()));
            }, 2);
            return;
        }

        if (action == ClickAction.STOP_EMOTE) {
            player.playSound(player.getLocation(), this.getSound(), 1, 1);
            user.playEmote(-1);
            return;
        }

        if (action == ClickAction.PREVIOUS_PAGE) {
            if (!data.hasKey(ClickAction.PAGE_KEY)) {
                return;
            }

            player.playSound(player.getLocation(), this.getSound(), 1, 1);
            user.openInventory(data.getInt(ClickAction.PAGE_KEY) - 1);
            return;
        }

        if (action == ClickAction.NEXT_PAGE) {
            if (!data.hasKey(ClickAction.PAGE_KEY)) {
                return;
            }

            player.playSound(player.getLocation(), this.getSound(), 1, 1);
            user.openInventory(data.getInt(ClickAction.PAGE_KEY) + 1);
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
            if (user.getDelay() > Instant.now().toEpochMilli()) {
                return;
            }

            user.setDelay(Instant.now().toEpochMilli() + 100);
            player.sendMessage(Config.MESSAGE_REQUIRES_LABYMOD.get());
            return;
        }

        Bukkit.getScheduler().runTaskLater(EmoteNPCPlugin.getInstance(), () -> {
            if (player.getOpenInventory().getTitle().equals(Config.INVENTORY_TITLE.get())) {
                return;
            }

            UserManager.getInstance().getUser(player).openInventory(1);
            player.playSound(player.getLocation(), this.getSound(), 1f, 1f);
        }, 1);
    }

    @EventHandler
    public void onSpawn(final NPCSpawnEvent event) {
        if (Config.DEBUG.get()) {
            EmoteNPCPlugin.getInstance().getLogger().info("Spawned NPC for " + event.getPlayer().getName());
        }
    }

    @EventHandler
    public void onRemove(final NPCRemoveEvent event) {
        if (Config.DEBUG.get()) {
            EmoteNPCPlugin.getInstance().getLogger().info("Removed NPC for " + event.getPlayer().getName());
        }
    }

    private Sound getSound() {
        return Version.isOlderThanOrEqualTo(Version.v1_8_R3) ? Sound.valueOf("CLICK") : Sound.valueOf("UI_BUTTON_CLICK");
    }

}
