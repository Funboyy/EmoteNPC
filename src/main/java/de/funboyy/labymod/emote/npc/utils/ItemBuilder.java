package de.funboyy.labymod.emote.npc.utils;

import de.funboyy.version.helper.Version;
import de.funboyy.version.helper.custom.data.CustomData;
import de.funboyy.version.helper.custom.data.CustomItem;
import de.funboyy.version.helper.custom.data.CustomSkull;
import java.util.Arrays;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class ItemBuilder {

    private ItemStack item;
    private ItemMeta meta;

    public ItemBuilder(final Material material) {
        this.item = new ItemStack(material);
        this.meta = this.item.getItemMeta();
    }

    public ItemBuilder name(final String name) {
        this.meta.setDisplayName(name);
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public ItemBuilder lore(final String... lines) {
        this.meta.setLore(Arrays.asList(lines));
        return this;
    }

    public ItemBuilder amount(final int amount) {
        this.item.setAmount(amount);
        return this;
    }

    public ItemBuilder clickAction(final ClickAction action) {
        this.applyData(data -> data.setString(ClickAction.ACTION_KEY, action.name()));
        return this;
    }

    public ItemBuilder dataInt(final String key, final int value) {
        this.applyData(data -> data.setInt(key, value));
        return this;
    }

    private void applyData(final DataEntry entry) {
        this.item.setItemMeta(this.meta);

        final CustomItem item = CustomItem.fromItem(this.item);
        CustomData data = item.getData();

        if (data == null) {
            data = new CustomData();
        }

        entry.write(data);
        item.setData(data);

        this.item = item.apply(this.item);
        this.meta = this.item.getItemMeta();
    }

    // only Skull
    public ItemBuilder owner(final String value) {
        if (this.item.getType() != getSkull()) {
            return this;
        }

        if (Version.isOlderThanOrEqualTo(Version.v1_12_R1)) {
            this.item.setDurability((short) 3);
        }

        final CustomSkull skull = new CustomSkull((SkullMeta) this.meta);
        skull.setTexture(value);
        return this;
    }

    public ItemStack build() {
        this.item.setItemMeta(this.meta);
        return this.item;
    }

    public static Material getSkull() {
        return Version.isOlderThanOrEqualTo(Version.v1_12_R1) ? Material.valueOf("SKULL_ITEM") : Material.valueOf("PLAYER_HEAD");
    }

    private interface DataEntry {

        void write(final CustomData data);

    }

}
