package de.funboyy.labymod.emote.npc.packet;

import de.funboyy.labymod.emote.npc.utils.NMSObject;
import de.funboyy.labymod.emote.npc.utils.NMSReflection;
import de.funboyy.labymod.emote.npc.utils.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.bukkit.entity.Player;

public class Protocol1_17to1_18_1 implements IProtocol {

    @Override
    public void sendBrand(final Player player) {
        final Object minecraftServer = new NMSObject(NMSReflection.getInstance().getClass("server.MinecraftServer"))
                .getDeclaredMethod("getServer").invoke();
        final String serverModName = (String) new NMSObject(minecraftServer).getMethod("getServerModName").invoke();

        send(player, new String[]{ "minecraft", "brand" }, serverModName);
    }

    @Override
    public void sendMessage(final Player player, final String key, final String messageContent) {
        final byte[] bytes = ProtocolUtils.getBytesToSend(key, messageContent);

        send(player, new String[]{ "labymod3", "main" }, Unpooled.wrappedBuffer(bytes));
    }

    @Override
    public void send(final Player player, final String[] key, final Object object) {
        try {
            final Constructor<?> serializerConstructor = NMSReflection.getInstance()
                    .getClass("network.PacketDataSerializer").getConstructor(ByteBuf.class);
            final ByteBuf byteBuf = object instanceof ByteBuf ? (ByteBuf) object : Unpooled.buffer();
            final NMSObject serializerObject = new NMSObject(serializerConstructor.newInstance(byteBuf));
            final Object serializer;

            if (!(object instanceof ByteBuf)) {
                serializer = serializerObject.getMethod("a", object.getClass()).invoke(object);
            } else {
                serializer = serializerObject.getObject();
            }

            final Constructor<?> minecraftKeyConstructor = NMSReflection.getInstance()
                    .getClass("resources.MinecraftKey").getConstructor(String.class, String.class);
            final Object minecraftKey = minecraftKeyConstructor.newInstance(key[0], key[1]);

            final Constructor<?> payloadPacketConstructor = NMSReflection.getInstance()
                    .getClass("network.protocol.game.PacketPlayOutCustomPayload")
                    .getConstructor(minecraftKey.getClass(), serializer.getClass());
            final Object payloadPacket = payloadPacketConstructor.newInstance(minecraftKey, serializer);

            NMSReflection.getInstance().sendPacket(player, payloadPacket);

        } catch (final NoSuchMethodException | InstantiationException | IllegalAccessException
                | InvocationTargetException exception) {
            exception.printStackTrace();
        }
    }

}
