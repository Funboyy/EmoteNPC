package de.funboyy.labymod.emote.npc.emote;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.funboyy.labymod.emote.npc.EmoteNPCPlugin;
import de.funboyy.labymod.emote.npc.config.Config;
import de.funboyy.labymod.emote.npc.utils.Protocol;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class EmoteManager {

    private static EmoteManager instance;

    public static EmoteManager getInstance() {
        if (instance == null) {
            instance = new EmoteManager();
        }
        return instance;
    }

    private final List<Emote> emotes;

    public EmoteManager() {
        this.emotes = new ArrayList<>();
    }

    public void loadEmotes() {
        InputStream inputStream;

        try {
            inputStream = new URL("https://neo.labymod.net/emotes/index.json").openStream();
        } catch (final IOException ignored) {
            EmoteNPCPlugin.getInstance().getLogger().warning("Cannot load remote emote list. Using local emote list (may be outdated)");
            inputStream = EmoteNPCPlugin.getInstance().getResource("emotes.json");
        }

        if (inputStream == null) {
            return;
        }

        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        final JsonElement element = Protocol.GSON.fromJson(reader, JsonElement.class);

        if (!element.isJsonObject()) {
            return;
        }

        final JsonObject object = element.getAsJsonObject();

        if (!object.has("emotes") || !object.get("emotes").isJsonObject()) {
            return;
        }

        final JsonObject emotes = object.get("emotes").getAsJsonObject();

        this.emotes.addAll(emotes.entrySet().stream()
                .map(Map.Entry::getValue)
                .filter(JsonElement::isJsonObject)
                .map(JsonElement::getAsJsonObject)
                .map(emote -> Protocol.GSON.fromJson(emote, Emote.class))
                .filter(emote -> emote.getName() != null)
                .collect(Collectors.toList()));

        if (Config.DEBUG.get()) {
            EmoteNPCPlugin.getInstance().getLogger().info(this.emotes.stream()
                    .filter(emote -> !emote.isDraft()).count() + " Emotes were registered");
        }
    }

    public Emote getEmoteById(final int id) {
        return this.emotes.stream().filter(emote -> emote.getId() == id).findAny().orElse(null);
    }

}
