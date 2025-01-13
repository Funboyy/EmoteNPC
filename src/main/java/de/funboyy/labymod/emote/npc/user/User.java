package de.funboyy.labymod.emote.npc.user;

import de.funboyy.labymod.emote.npc.EmoteNPCPlugin;
import de.funboyy.labymod.emote.npc.config.Config;
import de.funboyy.labymod.emote.npc.emote.Emote;
import de.funboyy.labymod.emote.npc.emote.EmoteManager;
import de.funboyy.labymod.emote.npc.emote.EmotePacket;
import de.funboyy.labymod.emote.npc.utils.ClickAction;
import de.funboyy.labymod.emote.npc.utils.ItemBuilder;
import de.funboyy.labymod.emote.npc.utils.Protocol;
import de.funboyy.version.helper.npc.NPC;
import de.funboyy.version.helper.npc.Skin;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

@Getter
public class User {

    private static final String PREVIOUS_PAGE_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTE4NWM5N2RiYjgzNTNkZTY1MjY5OGQyNGI2NDMyN2I3OTNhM2YzMmE5OGJlNjdiNzE5ZmJlZGFiMzVlIn19fQ==";
    private static final String NEXT_PAGE_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzFjMGVkZWRkNzExNWZjMWIyM2Q1MWNlOTY2MzU4YjI3MTk1ZGFmMjZlYmI2ZTQ1YTY2YzM0YzY5YzM0MDkxIn19fQ==";

    private final Player player;
    private final NPC npc;

    @Setter private String version;
    @Setter private boolean legacy;
    @Setter private long delay = 0L;

    protected User(final Player player) {
        this.player = player;

        this.npc = new NPC(EmoteNPCPlugin.getInstance(), this.player, Config.NPC_SETTINGS.get(), Config.NPC_NAME.get(),
                Config.NPC_LOCATION.get(), Skin.fromPlayer(this.player));
        this.npc.getTeam().setPrefix(Config.SCOREBOARD_PREFIX.get());
        this.npc.getTeam().setSuffix(Config.SCOREBOARD_SUFFIX.get());

        final ChatColor color = Config.SCOREBOARD_COLOR.get();
        if (color != null) {
            this.npc.getTeam().setColor(color);
        }

        EmoteNPCPlugin.getInstance().getManager().registerNPC(this.npc);
    }

    public void playEmote(final int emoteId) {
        if (!this.isPermitted()) {
            return;
        }

        final EmotePacket packet = new EmotePacket();
        packet.addEmote(new EmotePacket.Emote(this.npc.getUniqueId(), emoteId));

        Protocol.sendEmote(this, packet);

        if (Config.DEBUG.get()) {
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
        final Inventory inventory = Bukkit.createInventory(null, 45, Config.INVENTORY_TITLE.get());
        final List<Emote> emotes = EmoteManager.getInstance().getEmotes().stream().filter(emote -> !emote.isDraft()).collect(Collectors.toList());
        final int pages = (emotes.size() / 21) + (emotes.size() % 21 > 0 ? 1 : 0);

        inventory.setItem(4, new ItemBuilder(Material.EMERALD)
                .name(Config.ITEM_PAGES.get().replace("%page%", String.valueOf(page)).replace("%max%", String.valueOf(pages)))
                .amount(page)
                .build());

        final ItemBuilder stopBuilder = new ItemBuilder(Material.BARRIER)
                .name(Config.ITEM_STOP_EMOTE.get())
                .clickAction(ClickAction.STOP_EMOTE);

        if (!this.isLegacy()) {
            stopBuilder.lore(Config.ITEM_LABY_3.get());
        }

        inventory.setItem(40, stopBuilder.build());

        if (page != 1) {
            inventory.setItem(39, new ItemBuilder(ItemBuilder.getSkull())
                    .name(Config.ITEM_PREVIOUS_PAGE.get())
                    .clickAction(ClickAction.PREVIOUS_PAGE)
                    .dataInt(ClickAction.PAGE_KEY, page)
                    .owner(PREVIOUS_PAGE_TEXTURE)
                    .build());
        }

        if (page < pages) {
            inventory.setItem(41, new ItemBuilder(ItemBuilder.getSkull())
                    .name(Config.ITEM_NEXT_PAGE.get())
                    .clickAction(ClickAction.NEXT_PAGE)
                    .dataInt(ClickAction.PAGE_KEY, page)
                    .owner(NEXT_PAGE_TEXTURE)
                    .build());
        }

        int slot = 10;
        final int startIndex = 21 * (page - 1);

        for (int i = 0; i < 21; i++) {
            final int emoteIndex = i + startIndex;

            // check if we didn't reach the end of the list
            if (emotes.size() <= emoteIndex) {
                break;
            }

            final Emote emote = emotes.get(emoteIndex);
            final ItemBuilder builder = new ItemBuilder(Material.PAPER)
                    .name("§7" + emote.getName())
                    .clickAction(ClickAction.PLAY_EMOTE)
                    .dataInt(ClickAction.EMOTE_KEY, emote.getId());

            if (emote.getId() > 219 && this.isLegacy()) {
                builder.lore(Config.ITEM_LABY_4.get());
            }

            inventory.setItem(slot, builder.build());

            // do line break when at the end of the line
            if (i % 7 == 6) {
                slot += 3;
                continue;
            }

            slot++;
        }

        this.player.openInventory(inventory);
    }

}
