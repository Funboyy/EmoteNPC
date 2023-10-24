package de.funboyy.labymod.emote.npc.utils;

import de.funboyy.labymod.emote.npc.user.User;
import de.funboyy.version.helper.payload.Payload;
import de.funboyy.version.helper.payload.PayloadData;
import de.funboyy.version.helper.payload.PayloadKey;

public class Protocol {

    public static final String LABYMOD_CHANNEL_LEGACY = "labymod3:main";
    public static final String LABYMOD_CHANNEL = "labymod:neo";

    public static void sendMessage(final User user, final int id, final String messageContent) {
        final PayloadKey key = new PayloadKey(user.isLegacy() ? LABYMOD_CHANNEL_LEGACY : LABYMOD_CHANNEL);
        final PayloadData data = new PayloadData();
        data.writeInt(id);
        data.writeString(messageContent);

        final Payload payload = new Payload(key, data);
        payload.send(user.getPlayer());
    }

    public static void sendMessage(final User user, final String messageKey, final String messageContent) {
        final PayloadKey key = new PayloadKey(user.isLegacy() ? LABYMOD_CHANNEL_LEGACY : LABYMOD_CHANNEL);
        final PayloadData data = new PayloadData();
        data.writeString(messageKey);
        data.writeString(messageContent);

        final Payload payload = new Payload(key, data);
        payload.send(user.getPlayer());
    }

}
