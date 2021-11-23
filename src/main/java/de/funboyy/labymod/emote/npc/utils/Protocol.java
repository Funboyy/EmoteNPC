package de.funboyy.labymod.emote.npc.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import org.bukkit.entity.Player;

public class Protocol {

    public static void sendMessage(final Player player, final String key, final String messageContent) {
        try {
            final byte[] bytes = getBytesToSend(key, messageContent);

            final Constructor<?> serializerConstructor = NMSReflection.getInstance()
                    .getClass("PacketDataSerializer").getConstructor(ByteBuf.class);
            final Object serializer = serializerConstructor.newInstance(Unpooled.wrappedBuffer(bytes));

            if (Versions.getInstance().getVersionId() <= 1121) {
                final Constructor<?> payloadPacketConstructor = NMSReflection.getInstance()
                        .getClass("PacketPlayOutCustomPayload").getConstructor(String.class, serializer.getClass());
                final Object payloadPacket = payloadPacketConstructor.newInstance("labymod3:main", serializer);

                NMSReflection.getInstance().sendPacket(player, payloadPacket);

                return;
            }

            final Constructor<?> minecraftKeyConstructor = NMSReflection.getInstance()
                    .getClass("MinecraftKey").getConstructor(String.class, String.class);
            final Object minecraftKey = minecraftKeyConstructor.newInstance("labymod3", "main");

            final Constructor<?> payloadPacketConstructor = NMSReflection.getInstance()
                    .getClass("PacketPlayOutCustomPayload").getConstructor(minecraftKey.getClass(), serializer.getClass());
            final Object payloadPacket = payloadPacketConstructor.newInstance(minecraftKey, serializer);

            NMSReflection.getInstance().sendPacket(player, payloadPacket);

        } catch (final NoSuchMethodException | InstantiationException | IllegalAccessException
                | InvocationTargetException exception) {
            exception.printStackTrace();
        }
    }

    private static byte[] getBytesToSend(final String messageKey, final String messageContents) {
        final ByteBuf byteBuf = Unpooled.buffer();
        writeString(byteBuf, messageKey);
        writeString(byteBuf, messageContents);

        final byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        byteBuf.release();

        return bytes;
    }

    private static void writeVarIntToBuffer(final ByteBuf byteBuf, int input) {
        while ((input & -128) != 0) {
            byteBuf.writeByte(input & 127 | 128);
            input >>>= 7;
        }

        byteBuf.writeByte(input);
    }

    public static void writeString(final ByteBuf byteBuf, final String string) {
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);

        if (bytes.length > Short.MAX_VALUE) {
            throw new EncoderException("String too big (was " + string.length() + " bytes encoded, max " + Short.MAX_VALUE + ")");
        } else {
            writeVarIntToBuffer(byteBuf, bytes.length);
            byteBuf.writeBytes(bytes);
        }
    }

    private static int readVarIntFromBuffer(final ByteBuf byteBuf) {
        int i = 0;
        int j = 0;

        byte bytes;
        do {
            bytes = byteBuf.readByte();
            i |= (bytes & 127) << j++ * 7;
            if ( j > 5 ) {
                throw new RuntimeException("VarInt too big");
            }
        } while ((bytes & 128) == 128);

        return i;
    }

    public static String readString(final ByteBuf byteBuf, final int maxLength) {
        final int i = readVarIntFromBuffer(byteBuf);

        if (i > maxLength * 4) {
            throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + i + " > " + maxLength * 4 + ")");
        } else if (i < 0) {
            throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
        } else {
            final byte[] bytes = new byte[i];
            byteBuf.readBytes(bytes);

            final String string = new String(bytes, StandardCharsets.UTF_8);
            if (string.length() > maxLength) {
                throw new DecoderException("The received string length is longer than maximum allowed (" + i + " > " + maxLength + ")");
            } else {
                return string;
            }
        }
    }
}
