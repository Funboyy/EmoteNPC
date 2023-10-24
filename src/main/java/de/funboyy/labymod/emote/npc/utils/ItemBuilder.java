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

    public ItemBuilder nbtTag(final String key, final String value) {
        this.item.setItemMeta(this.meta);

        final CustomItem item = CustomItem.fromItem(this.item);
        CustomData data = item.getData();

        if (data == null) {
            data = new CustomData();
        }

        data.setString(key, value);
        item.setData(data);

        this.item = item.apply(this.item);
        this.meta = this.item.getItemMeta();
        return this;
    }

    public ItemBuilder nbtTag(final String key, final int value) {
        this.item.setItemMeta(this.meta);

        final CustomItem item = CustomItem.fromItem(this.item);
        CustomData data = item.getData();

        if (data == null) {
            data = new CustomData();
        }

        data.setInt(key, value);
        item.setData(data);

        this.item = item.apply(this.item);
        this.meta = this.item.getItemMeta();
        return this;
    }

    // only Skull
    public ItemBuilder owner(final String value) {
        if (this.item.getType() != getSkull()) {
            return this;
        }

        if (Version.getVersionId() <= Version.v1_12_R1) {
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
        return Version.getVersionId() <= Version.v1_12_R1 ? Material.valueOf("SKULL_ITEM") : Material.valueOf("PLAYER_HEAD");
    }

}
