package de.funboyy.labymod.emote.npc.emote;

import com.google.gson.annotations.SerializedName;
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

        @SerializedName("uuid")
        private final UUID uniqueId;
        @SerializedName("emote_id")
        private final int emoteId;

        public Emote(final UUID uniqueId, final int emoteId) {
            this.uniqueId = uniqueId;
            this.emoteId = emoteId;
        }

    }

}
