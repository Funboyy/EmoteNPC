package de.funboyy.labymod.emote.npc.utils;

import io.netty.channel.Channel;
import java.lang.reflect.Field;
import java.util.Arrays;
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
        if (Versions.getInstance().hasNewProtocol()) {
            return getClassByName("net.minecraft." + name);
        }

        return getClassByName("net.minecraft.server." + Versions.getInstance().getVersion() + "." + name);
    }



    public Class<?> getBukkitClass(final String name) {
        return getClassByName("org.bukkit.craftbukkit." + Versions.getInstance().getVersion() + "." + name);
    }

    public Class<?> getClassByName(final String name) {
        try {
            return Class.forName(name);
        } catch (final ClassNotFoundException exception) {
            exception.printStackTrace();
        }

        return null;
    }

    public ItemStack addNBT(final ItemStack itemStack, final String key, final String value) {
        try {
            final NMSObject craftItemStack = new NMSObject(getBukkitClass("inventory.CraftItemStack"));
            final NMSObject nmsItemStack = new NMSObject(craftItemStack.getDeclaredMethod("asNMSCopy", ItemStack.class).invoke(itemStack));
            final NMSObject component = (boolean) nmsItemStack.getMethod(Versions.getInstance().getId() == Versions.v1_18_R1 ?
                    "r" : "hasTag").invoke() ? new NMSObject(nmsItemStack.getMethod(
                            (Versions.getInstance().getId() == Versions.v1_18_R1 ? "s" : "getTag"))
                    .invoke()) : new NMSObject(getClass("nbt.NBTTagCompound").newInstance());
            component.getMethod(Versions.getInstance().getId() == Versions.v1_18_R1 ?
                    "a" : "setString", String.class, String.class).invoke(key, value);

            return (ItemStack) craftItemStack.getDeclaredMethod("asBukkitCopy", getClass(
                    Versions.getInstance().hasNewProtocol() ? "world.item.ItemStack" : "ItemStack"))
                    .invoke(nmsItemStack.getObject());
        } catch (final IllegalAccessException | InstantiationException exception) {
            exception.printStackTrace();
        }

        return itemStack;
    }

    public String getNBT(final ItemStack itemStack, final String key) {
        final NMSObject craftItemStack = new NMSObject(getBukkitClass("inventory.CraftItemStack"));
        final NMSObject nmsItemStack = new NMSObject(craftItemStack.getDeclaredMethod("asNMSCopy", ItemStack.class).invoke(itemStack));

        if (!(boolean) nmsItemStack.getMethod(
                Versions.getInstance().getId() == Versions.v1_18_R1 ? "r" : "hasTag").invoke()) {
            return null;
        }

        final NMSObject component = new NMSObject(nmsItemStack.getMethod(
                Versions.getInstance().getId() == Versions.v1_18_R1 ? "s" : "getTag").invoke());
        return (String) component.getMethod(Versions.getInstance().getId() == Versions.v1_18_R1 ?
                "l" : "getString", String.class).invoke(key);
    }

    public Object getEntityPlayer(final Player player) {
        return new NMSObject(player).getMethod("getHandle").invoke();
    }

    public Channel getChannel(final Player player) {
        final NMSObject craftPlayer = new NMSObject(getEntityPlayer(player));
        final NMSObject playerConnection = new NMSObject(craftPlayer.getField(
                Versions.getInstance().hasNewProtocol() ? "b" : "playerConnection"));
        final Object networkManager = playerConnection.getField(
                Versions.getInstance().hasNewProtocol() ? "a" : "networkManager");

        if (Versions.getInstance().getId() == Versions.v1_8_R1) {
            return (Channel) getValue(networkManager, "i");
        }

        if (Versions.getInstance().getId() == Versions.v1_8_R2 || Versions.getInstance().hasNewProtocol()) {
            return (Channel) getValue(networkManager, "k");
        }

        return (Channel) getValue(networkManager, "channel");
    }

    public void sendPacket(final Player player, final Object object) {
        final NMSObject craftPlayer = new NMSObject(getEntityPlayer(player));
        final NMSObject playerConnection = new NMSObject(craftPlayer.getField(
                Versions.getInstance().hasNewProtocol() ? "b" : "playerConnection"));
        playerConnection.getMethod(Versions.getInstance().getId() == Versions.v1_18_R1 ? "a" : "sendPacket", getClass(
                Versions.getInstance().hasNewProtocol() ? "network.protocol.Packet" : "Packet")).invoke(object);
    }

    public Object getEnum(final Class<?> clazz, final String name) {
        final Object[] enums = clazz.getEnumConstants();
        return Arrays.stream(enums).filter(stream -> stream.toString().equals(name))
                .findAny().orElseGet(() -> enums[0]);
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
