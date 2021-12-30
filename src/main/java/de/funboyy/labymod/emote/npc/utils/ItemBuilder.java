package de.funboyy.labymod.emote.npc.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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

    public ItemBuilder nbtTag(final String key, final String value) {
        this.item.setItemMeta(this.meta);
        this.item = NMSReflection.getInstance().addNBT(this.item, key, value);
        this.meta = this.item.getItemMeta();
        return this;
    }

    // only Skull
    public ItemBuilder owner(final String value) {
        if (this.item.getType() != Versions.getInstance().getSkull()) {
            return this;
        }

        if (Versions.getInstance().getId() <= Versions.v1_12_R1) {
            this.item.setDurability((short) 3);
        }

        final GameProfile gameProfile = new GameProfile(UUID.randomUUID(), null);
        gameProfile.getProperties().put("textures", new Property("textures", value));
        NMSReflection.getInstance().setValue(this.meta, "profile", gameProfile);
        return this;
    }

    public ItemStack build() {
        this.item.setItemMeta(this.meta);
        return this.item;
    }
}
