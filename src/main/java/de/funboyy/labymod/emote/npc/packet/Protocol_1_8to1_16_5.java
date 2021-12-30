package de.funboyy.labymod.emote.npc.packet;

import de.funboyy.labymod.emote.npc.utils.NMSObject;
import de.funboyy.labymod.emote.npc.utils.NMSReflection;
import de.funboyy.labymod.emote.npc.utils.ProtocolUtils;
import de.funboyy.labymod.emote.npc.utils.Versions;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.bukkit.entity.Player;

public class Protocol_1_8to1_16_5 implements IProtocol {

    @Override
    public void sendBrand(final Player player) {
        final Object minecraftServer = new NMSObject(NMSReflection.getInstance().getClass("MinecraftServer"))
                .getDeclaredMethod("getServer").invoke();
        final String serverModName = (String) new NMSObject(minecraftServer).getMethod("getServerModName").invoke();

        send(player, Versions.getInstance().getId() <= Versions.v1_12_R1 ?
                new String[]{ "MC|Brand" } : new String[]{ "minecraft", "brand" }, serverModName);
    }

    @Override
    public void sendMessage(final Player player, final String key, final String messageContent) {
        final byte[] bytes = ProtocolUtils.getBytesToSend(key, messageContent);

        send(player, Versions.getInstance().getId() <= Versions.v1_12_R1 ?
                new String[]{ "labymod3:main" } : new String[]{ "labymod3", "main" }, Unpooled.wrappedBuffer(bytes));
    }

    @Override
    public void send(final Player player, final String[] key, final Object object) {
        try {
            final Constructor<?> serializerConstructor = NMSReflection.getInstance()
                    .getClass("PacketDataSerializer").getConstructor(ByteBuf.class);
            final ByteBuf byteBuf = object instanceof ByteBuf ? (ByteBuf) object : Unpooled.buffer();
            final NMSObject serializerObject = new NMSObject(serializerConstructor.newInstance(byteBuf));
            final Object serializer;

            if (!(object instanceof ByteBuf)) {
                serializer = serializerObject.getMethod("a", object.getClass()).invoke(object);
            } else {
                serializer = serializerObject.getObject();
            }

            if (Versions.getInstance().getId() <= Versions.v1_12_R1) {
                final Constructor<?> payloadPacketConstructor = NMSReflection.getInstance()
                        .getClass("PacketPlayOutCustomPayload").getConstructor(String.class, serializer.getClass());
                final Object payloadPacket = payloadPacketConstructor.newInstance(key[0], serializer);

                NMSReflection.getInstance().sendPacket(player, payloadPacket);

                return;
            }

            final Constructor<?> minecraftKeyConstructor = NMSReflection.getInstance()
                    .getClass("MinecraftKey").getConstructor(String.class, String.class);
            final Object minecraftKey = minecraftKeyConstructor.newInstance(key[0], key[1]);

            final Constructor<?> payloadPacketConstructor = NMSReflection.getInstance()
                    .getClass("PacketPlayOutCustomPayload").getConstructor(minecraftKey.getClass(), serializer.getClass());
            final Object payloadPacket = payloadPacketConstructor.newInstance(minecraftKey, serializer);

            NMSReflection.getInstance().sendPacket(player, payloadPacket);

        } catch (final NoSuchMethodException | InstantiationException | IllegalAccessException
                | InvocationTargetException exception) {
            exception.printStackTrace();
        }
    }

}
