package de.funboyy.labymod.emote.npc.utils;

import io.netty.channel.Channel;
import java.lang.reflect.Field;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class NMSReflection {

    private static NMSReflection instance;

    public static NMSReflection getInstance() {
        if (instance == null) {
            instance = new NMSReflection();
        }

        return instance;
    }

    public Class<?> getClass(final String name) {
        final String nmsName = "net.minecraft.server." + Versions.getInstance().getVersion() + "." + name;

        try {
            return Class.forName(nmsName);
        } catch (final ClassNotFoundException exception) {
            exception.printStackTrace();
        }

        return null;
    }

    public Class<?> getBukkitClass(final String name) {
        final String nmsName = "org.bukkit.craftbukkit." + Versions.getInstance().getVersion() + "." + name;

        try {
            return Class.forName(nmsName);
        } catch (final ClassNotFoundException exception) {
            exception.printStackTrace();
        }

        return null;
    }

    public ItemStack addNBT(final ItemStack itemStack, final String key, final String value) {
        try {
            final NMSObject craftItemStack = new NMSObject(getBukkitClass("inventory.CraftItemStack"));
            final NMSObject nmsItemStack = new NMSObject(craftItemStack.getDeclaredMethod("asNMSCopy", ItemStack.class).invoke(itemStack));
            final NMSObject component = (boolean) nmsItemStack.getMethod("hasTag").invoke()
                    ? new NMSObject(nmsItemStack.getMethod("getTag").invoke()) : new NMSObject(getClass("NBTTagCompound").newInstance());
            component.getMethod("setString", String.class, String.class).invoke(key, value);

            return (ItemStack) craftItemStack.getDeclaredMethod("asBukkitCopy", getClass("ItemStack")).invoke(nmsItemStack.getObject());
        } catch (final IllegalAccessException | InstantiationException exception) {
            exception.printStackTrace();
        }

        return itemStack;
    }

    public String getNBT(final ItemStack itemStack, final String key) {
        final NMSObject craftItemStack = new NMSObject(getBukkitClass("inventory.CraftItemStack"));
        final NMSObject nmsItemStack = new NMSObject(craftItemStack.getDeclaredMethod("asNMSCopy", ItemStack.class).invoke(itemStack));

        if (!(boolean) nmsItemStack.getMethod("hasTag").invoke()) {
            return null;
        }

        final NMSObject component = new NMSObject(nmsItemStack.getMethod("getTag").invoke());
        return (String) component.getMethod("getString", String.class).invoke(key);
    }

    public Object getEntityPlayer(final Player player) {
        return new NMSObject(player).getMethod("getHandle").invoke();
    }

    public Channel getChannel(final Player player) {
        final NMSObject craftPlayer = new NMSObject(getEntityPlayer(player));
        final NMSObject playerConnection = new NMSObject(craftPlayer.getField("playerConnection"));
        final Object networkManager = playerConnection.getField("networkManager");

        if (Versions.getInstance().getVersionId() == 181) {
            return (Channel) getValue(networkManager, "i");
        }

        if (Versions.getInstance().getVersionId() == 182) {
            return (Channel) getValue(networkManager, "k");
        }

        return (Channel) getValue(networkManager, "channel");
    }

    public void sendPacket(final Player player, final Object object) {
        final NMSObject craftPlayer = new NMSObject(getEntityPlayer(player));
        final NMSObject playerConnection = new NMSObject(craftPlayer.getField("playerConnection"));
        playerConnection.getMethod("sendPacket", getClass("Packet")).invoke(object);
    }

    public void setValue(final Object object, final String name, final Object value) {
        try {
            final Field field = object.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(object, value);
        } catch (final NoSuchFieldException | IllegalAccessException exception) {
            exception.printStackTrace();
        }
    }

    public Object getValue(final Object object, final String name) {
        try {
            final Field field = object.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(object);
        } catch (final NoSuchFieldException | IllegalAccessException exception) {
            exception.printStackTrace();
            return null;
        }
    }

}
