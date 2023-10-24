package de.funboyy.labymod.emote.npc.emote;

import com.google.gson.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;

@Getter
public class EmotePacket {

    private final List<Emote> emotes;

    public EmotePacket() {
        this.emotes = new ArrayList<>();
    }

    public void addEmote(final Emote emote) {
        this.emotes.add(emote);
    }

    @Getter
    public static class Emote {

        private final UUID npcUniqueId;
        private final int emoteId;

        public Emote(final UUID npcUniqueId, final int emoteId) {
            this.npcUniqueId = npcUniqueId;
            this.emoteId = emoteId;
        }

        public static JsonSerializer<Emote> getLegacySerializer() {
            return (emote, type, context) -> {
                final JsonObject object = new JsonObject();
                object.addProperty("uuid", emote.getNpcUniqueId().toString());
                object.addProperty("emote_id", emote.getEmoteId());

                return object;
            };
        }

    }

    public static JsonSerializer<EmotePacket> getLegacySerializer() {
        return (packet, type, context) -> {
            final JsonArray array = new JsonArray();
            packet.getEmotes().forEach(emote -> array.add(context.serialize(emote, Emote.class)));

            return array;
        };
    }

}
