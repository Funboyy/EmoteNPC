package de.funboyy.labymod.emote.npc.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.funboyy.labymod.emote.npc.emote.EmotePacket;
import de.funboyy.labymod.emote.npc.user.User;
import de.funboyy.version.helper.payload.Payload;
import de.funboyy.version.helper.payload.PayloadData;
import de.funboyy.version.helper.payload.PayloadKey;

public class Protocol {

    private static final Gson GSON = new GsonBuilder().create();

    public static final String LABYMOD_CHANNEL_LEGACY = "labymod3:main";
    public static final String LABYMOD_CHANNEL = "labymod:neo";

    public static void sendEmote(final User user, final EmotePacket packet) {
        final PayloadKey key = new PayloadKey(user.isLegacy() ? LABYMOD_CHANNEL_LEGACY : LABYMOD_CHANNEL);
        final PayloadData data = new PayloadData();

        if (user.isLegacy()) {
            data.writeString("emote_api");
            data.writeString(GSON.toJson(packet));
        } else {
            data.writeVarInt(16);
            data.writeList(packet.getEmotes(), emote -> {
                data.writeUUID(emote.getUniqueId());
                data.writeVarInt(emote.getEmoteId());
            });
        }

        final Payload payload = new Payload(key, data);
        payload.send(user.getPlayer());
    }

}
