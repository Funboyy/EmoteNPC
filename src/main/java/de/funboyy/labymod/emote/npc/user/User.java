package de.funboyy.labymod.emote.npc.user;

import de.funboyy.labymod.emote.npc.EmoteNPCPlugin;
import de.funboyy.labymod.emote.npc.config.Config;
import de.funboyy.labymod.emote.npc.emote.Emote;
import de.funboyy.labymod.emote.npc.emote.EmoteManager;
import de.funboyy.labymod.emote.npc.packet.IEmoteNPC;
import de.funboyy.labymod.emote.npc.packet.IPacketReader;
import de.funboyy.labymod.emote.npc.utils.ItemBuilder;
import de.funboyy.labymod.emote.npc.utils.ProtocolUtils;
import de.funboyy.labymod.emote.npc.utils.Versions;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class User {

    @Getter private final Player player;
    @Getter private final IPacketReader reader;
    @Getter private final IEmoteNPC npc;

    @Getter @Setter private String version;
    @Getter @Setter private long delay = 0L;

    protected User(final Player player) throws NoSuchMethodException,
            InvocationTargetException, InstantiationException, IllegalAccessException {
        this.player = player;

        final Constructor<?> readerConstructor = Versions.getInstance().getPacketReader().getConstructor(Player.class);
        final Constructor<?> npcConstructor = Versions.getInstance().getEmoteNPC().getConstructor(Player.class);

        this.reader = (IPacketReader) readerConstructor.newInstance(this.player);
        this.reader.inject();

        this.npc = (IEmoteNPC) npcConstructor.newInstance(this.player);
    }

    @SuppressWarnings("unchecked")
    public void playEmote(final int emoteId) {
        if (!isPermitted()) {
            return;
        }

        final JSONArray array = new JSONArray();
        final JSONObject object = new JSONObject();
        object.put("uuid", this.npc.getUuid().toString());
        object.put("emote_id", emoteId);
        array.add(object);

        EmoteNPCPlugin.getInstance().getProtocol().sendMessage(getPlayer(), "emote_api", array.toJSONString());

        if (Config.getInstance().debug()) {
            if (emoteId == -1) {
                return;
            }

            final Emote emote = EmoteManager.getInstance().getEmoteById(emoteId);
            Bukkit.getLogger().info("[" + EmoteNPCPlugin.getInstance().getName() + "] " +
                    "Playing Emote " + emote.getName() + " (#" + emoteId + ") for " + this.player.getName());
        }
    }

    @SuppressWarnings("all")
    public boolean isPermitted() {
        if (this.version == null) {
            return false;
        }

        try {
            final int version = Integer.parseInt(this.version.replaceAll("\\.", ""));
            return version >= 380;
        } catch (final NumberFormatException ignored) {
            return false;
        }
    }

    @SuppressWarnings("all")
    public boolean isNearNPC() {
        if (!this.player.getLocation().getWorld().equals(Config.getInstance().getLocation().getWorld())) {
            return false;
        }

        return this.player.getLocation().distance(Config.getInstance().getLocation()) < 90;
    }

    public void openInventory(final int page) {
        final Inventory inventory = Bukkit.createInventory(null, 45, Config.getInstance().getInventory());
        final List<Emote> emotes = new ArrayList<>(EmoteManager.getInstance().getEmotes());
        final int pages = (emotes.size() / 21) + (emotes.size() % 21 > 0 ? 1 : 0);

        int temp = (21 * (page - 1));
        for (int i = 0; i < 45; i++) {
            if (i >= 10 && i <= 16 || i >= 19 && i <= 25 || i >= 28 && i <= 34) {
                if (emotes.size() > temp) {
                    inventory.setItem(i, new ItemBuilder(Material.PAPER)
                            .name("§7" + emotes.get(temp).getName())
                            .nbtTag("Event", "playEmote")
                            .nbtTag("EmoteID", String.valueOf(emotes.get(temp).getId()))
                            .build());
                    temp++;
                }
            } else if (i == 4) {
                inventory.setItem(i, new ItemBuilder(Material.EMERALD)
                        .name(Config.getInstance().getPage().replace("%page%", String.valueOf(page))
                                .replace("%max%", String.valueOf(pages)))
                        .build());
            } else if (i == 39 && page != 1) {
                inventory.setItem(i, new ItemBuilder(Versions.getInstance().getSkull())
                        .name(Config.getInstance().getLastPage())
                        .nbtTag("Event", "lastPage")
                        .nbtTag("Page", String.valueOf(page))
                        .owner("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTE4NWM5N2RiYjgzNTNkZTY1MjY5OGQyNGI2NDMyN2I3OTNhM2YzMmE5OGJlNjdiNzE5ZmJlZGFiMzVlIn19fQ==")
                        .build());
            } else if (i == 40) {
                inventory.setItem(i, new ItemBuilder(Material.BARRIER)
                        .name(Config.getInstance().getStopEmote())
                        .nbtTag("Event", "stopEmote")
                        .build());
            } else if (i == 41 && page < pages) {
                inventory.setItem(i, new ItemBuilder(Versions.getInstance().getSkull())
                        .name(Config.getInstance().getNextPage())
                        .nbtTag("Event", "nextPage")
                        .nbtTag("Page", String.valueOf(page))
                        .owner("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzFjMGVkZWRkNzExNWZjMWIyM2Q1MWNlOTY2MzU4YjI3MTk1ZGFmMjZlYmI2ZTQ1YTY2YzM0YzY5YzM0MDkxIn19fQ==")
                        .build());
            }
        }

        this.player.openInventory(inventory);
    }

}
