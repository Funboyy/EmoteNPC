package de.funboyy.labymod.emote.npc.user;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.funboyy.labymod.emote.npc.EmoteNPCPlugin;
import de.funboyy.labymod.emote.npc.config.Config;
import de.funboyy.labymod.emote.npc.emote.Emote;
import de.funboyy.labymod.emote.npc.emote.EmoteManager;
import de.funboyy.labymod.emote.npc.emote.EmotePacket;
import de.funboyy.labymod.emote.npc.utils.ItemBuilder;
import de.funboyy.labymod.emote.npc.utils.Protocol;
import de.funboyy.version.helper.npc.NPC;
import de.funboyy.version.helper.npc.Skin;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

@Getter
public class User {

    private static final Gson GSON = new GsonBuilder().create();
    private static final Gson GSON_LEGACY = new GsonBuilder().registerTypeAdapter(EmotePacket.class, EmotePacket.getLegacySerializer())
            .registerTypeAdapter(EmotePacket.Emote.class, EmotePacket.Emote.getLegacySerializer()).create();

    private final Player player;
    private final NPC npc;

    @Setter private String version;
    @Setter private boolean legacy;
    @Setter private long delay = 0L;

    protected User(final Player player) {
        this.player = player;

        this.npc = new NPC(EmoteNPCPlugin.getInstance(), this.player, Config.getInstance().setting(), Config.getInstance().name(),
                Config.getInstance().getLocation(), Skin.fromPlayer(this.player));
        this.npc.getTeam().setPrefix(Config.getInstance().prefix());
        this.npc.getTeam().setSuffix(Config.getInstance().suffix());

        if (Config.getInstance().nameColor() != null) {
            this.npc.getTeam().setColor(Config.getInstance().nameColor());
        }

        EmoteNPCPlugin.getInstance().getManager().registerNPC(this.npc);
    }

    public void playEmote(final int emoteId) {
        if (!isPermitted()) {
            return;
        }

        final EmotePacket packet = new EmotePacket();
        packet.addEmote(new EmotePacket.Emote(this.npc.getUniqueId(), emoteId));

        if (this.legacy) {
            Protocol.sendMessage(this, "emote_api", GSON_LEGACY.toJson(packet, EmotePacket.class));
        } else {
            Protocol.sendMessage(this, 18, GSON.toJson(packet, EmotePacket.class));
        }

        if (Config.getInstance().debug()) {
            if (emoteId == -1) {
                return;
            }

            final Emote emote = EmoteManager.getInstance().getEmoteById(emoteId);
            EmoteNPCPlugin.getInstance().getLogger().info(
                    "Playing Emote " + emote.getName() + " (#" + emoteId + ") for " + this.player.getName());
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isPermitted() {
        return this.version != null;
    }

    public void openInventory(final int page) {
        final Inventory inventory = Bukkit.createInventory(null, 45, Config.getInstance().getInventory());
        final List<Emote> emotes = EmoteManager.getInstance().getEmotes().stream().filter(emote -> !emote.isDraft()).collect(Collectors.toList());
        final int pages = (emotes.size() / 21) + (emotes.size() % 21 > 0 ? 1 : 0);

        int temp = (21 * (page - 1));
        for (int i = 0; i < 45; i++) {
            if (i >= 10 && i <= 16 || i >= 19 && i <= 25 || i >= 28 && i <= 34) {
                if (emotes.size() <= temp) {
                    continue;
                }

                final ItemBuilder builder = new ItemBuilder(Material.PAPER)
                        .name("§7" + emotes.get(temp).getName())
                        .nbtTag("Event", "playEmote")
                        .nbtTag("EmoteID", emotes.get(temp).getId());

                if (emotes.get(temp).getId() > 219) {
                    builder.lore(Config.getInstance().getLabyMod4Only());
                }

                inventory.setItem(i, builder.build());
                temp++;
                continue;
            }

            if (i == 4) {
                inventory.setItem(i, new ItemBuilder(Material.EMERALD)
                        .name(Config.getInstance().getPage().replace("%page%", String.valueOf(page))
                                .replace("%max%", String.valueOf(pages)))
                        .amount(page)
                        .build());
                continue;
            }

            if (i == 39 && page != 1) {
                inventory.setItem(i, new ItemBuilder(ItemBuilder.getSkull())
                        .name(Config.getInstance().getLastPage())
                        .nbtTag("Event", "lastPage")
                        .nbtTag("Page", page)
                        .owner("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTE4NWM5N2RiYjgzNTNkZTY1MjY5OGQyNGI2NDMyN2I3OTNhM2YzMmE5OGJlNjdiNzE5ZmJlZGFiMzVlIn19fQ==")
                        .build());
                continue;
            }

            if (i == 40) {
                inventory.setItem(i, new ItemBuilder(Material.BARRIER)
                        .name(Config.getInstance().getStopEmote())
                        .nbtTag("Event", "stopEmote")
                        .build());
                continue;
            }

            if (i == 41 && page < pages) {
                inventory.setItem(i, new ItemBuilder(ItemBuilder.getSkull())
                        .name(Config.getInstance().getNextPage())
                        .nbtTag("Event", "nextPage")
                        .nbtTag("Page", page)
                        .owner("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzFjMGVkZWRkNzExNWZjMWIyM2Q1MWNlOTY2MzU4YjI3MTk1ZGFmMjZlYmI2ZTQ1YTY2YzM0YzY5YzM0MDkxIn19fQ==")
                        .build());
            }
        }

        this.player.openInventory(inventory);
    }

}
