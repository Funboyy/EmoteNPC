package de.funboyy.labymod.emote.npc.packet;

import de.funboyy.labymod.emote.npc.EmoteNPCPlugin;
import de.funboyy.labymod.emote.npc.packet.mapping.ClassMapping;
import de.funboyy.labymod.emote.npc.packet.nms.NMSObject;
import de.funboyy.labymod.emote.npc.packet.nms.NMSReflection;
import de.funboyy.labymod.emote.npc.utils.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.bukkit.entity.Player;

public class Protocol {

    private final ClassMapping manager;
    private final boolean olderVersion;

    public Protocol() {
        this.manager = EmoteNPCPlugin.getInstance().getClassManager();
        this.olderVersion = Version.getInstance().getId() <= Version.v1_12_R1;
    }

    public void sendBrand(final Player player) {
        final Object minecraftServer = new NMSObject(this.manager.getMinecraftServer())
                .getDeclaredMethod("getServer").invoke();
        final String serverModName = (String) new NMSObject(minecraftServer).getMethod("getServerModName").invoke();

        send(player, this.olderVersion ? new String[]{ "MC|Brand" } : new String[]{ "minecraft", "brand" }, serverModName);
    }

    public void sendMessage(final Player player, final String key, final String messageContent) {
        final byte[] bytes = ProtocolUtils.getBytesToSend(key, messageContent);

        send(player, this.olderVersion ? new String[]{ "labymod3:main" } : new String[]{ "labymod3", "main" }, Unpooled.wrappedBuffer(bytes));
    }

    public void send(final Player player, final String[] key, final Object object) {
        try {
            final Constructor<?> serializerConstructor = this.manager.getPacketDataSerializer().getConstructor(ByteBuf.class);
            final ByteBuf byteBuf = object instanceof ByteBuf ? (ByteBuf) object : Unpooled.buffer();
            final NMSObject serializerObject = new NMSObject(serializerConstructor.newInstance(byteBuf));
            final Object serializer;

            if (!(object instanceof ByteBuf)) {
                serializer = serializerObject.getMethod("a", object.getClass()).invoke(object);
            } else {
                serializer = serializerObject.getObject();
            }

            if (this.olderVersion) {
                final Constructor<?> payloadPacketConstructor = this.manager.getPacketPlayOutCustomPayload()
                        .getConstructor(String.class, serializer.getClass());
                final Object payloadPacket = payloadPacketConstructor.newInstance(key[0], serializer);

                NMSReflection.getInstance().sendPacket(player, payloadPacket);
                return;
            }

            final Constructor<?> minecraftKeyConstructor = this.manager.getMinecraftKey().getConstructor(String.class, String.class);
            final Object minecraftKey = minecraftKeyConstructor.newInstance(key[0], key[1]);

            final Constructor<?> payloadPacketConstructor = this.manager.getPacketPlayOutCustomPayload()
                    .getConstructor(minecraftKey.getClass(), serializer.getClass());
            final Object payloadPacket = payloadPacketConstructor.newInstance(minecraftKey, serializer);

            NMSReflection.getInstance().sendPacket(player, payloadPacket);

        } catch (final NoSuchMethodException | InstantiationException | IllegalAccessException
                | InvocationTargetException exception) {
            exception.printStackTrace();
        }
    }

}
