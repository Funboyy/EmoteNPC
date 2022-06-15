package de.funboyy.labymod.emote.npc.packet.nms;

import de.funboyy.labymod.emote.npc.EmoteNPCPlugin;
import de.funboyy.labymod.emote.npc.packet.mapping.ClassMapping;
import de.funboyy.labymod.emote.npc.packet.mapping.MethodMapping;
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

    private final ClassMapping manager;
    private final MethodMapping method;

    public NMSReflection() {
        this.manager = EmoteNPCPlugin.getInstance().getClassManager();
        this.method = EmoteNPCPlugin.getInstance().getMethodManager();
    }

    public ItemStack addNBT(final ItemStack itemStack, final String key, final String value) {
        try {
            final NMSObject craftItemStack = new NMSObject(this.manager.getCraftItemStack());
            final NMSObject nmsItemStack = new NMSObject(craftItemStack.getDeclaredMethod("asNMSCopy", ItemStack.class).invoke(itemStack));

            final NMSObject component = (boolean) nmsItemStack.getMethod(this.method.hasTag()).invoke() ?
                        new NMSObject(nmsItemStack.getMethod(this.method.getTag()).invoke()) :
                        new NMSObject(this.manager.getNBTTagCompound().newInstance());
            component.getMethod(this.method.setString(), String.class, String.class).invoke(key, value);

            return (ItemStack) craftItemStack.getDeclaredMethod("asBukkitCopy",
                    this.manager.getItemStack()).invoke(nmsItemStack.getObject());
        } catch (final IllegalAccessException | InstantiationException exception) {
            exception.printStackTrace();
        }

        return itemStack;
    }

    public String getNBT(final ItemStack itemStack, final String key) {
        final NMSObject craftItemStack = new NMSObject(this.manager.getCraftItemStack());
        final NMSObject nmsItemStack = new NMSObject(craftItemStack.getDeclaredMethod("asNMSCopy", ItemStack.class).invoke(itemStack));

        if (!(boolean) nmsItemStack.getMethod(this.method.hasTag()).invoke()) {
            return null;
        }

        final NMSObject component = new NMSObject(nmsItemStack.getMethod(this.method.getTag()).invoke());
        return (String) component.getMethod(this.method.getString(), String.class).invoke(key);
    }

    public Object getEntityPlayer(final Player player) {
        return new NMSObject(player).getMethod("getHandle").invoke();
    }

    public Channel getChannel(final Player player) {
        final NMSObject craftPlayer = new NMSObject(getEntityPlayer(player));
        final NMSObject playerConnection = new NMSObject(craftPlayer.getField(this.method.playerConnection()));
        final Object networkManager = playerConnection.getField(this.method.networkManager());

        return (Channel) getValue(networkManager, this.method.channel());
    }

    public void sendPacket(final Player player, final Object object) {
        final NMSObject craftPlayer = new NMSObject(getEntityPlayer(player));
        final NMSObject playerConnection = new NMSObject(craftPlayer.getField(this.method.playerConnection()));
        playerConnection.getMethod(this.method.sendPacket(), this.manager.getPacket()).invoke(object);
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
