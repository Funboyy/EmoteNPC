package de.funboyy.labymod.emote.npc.emote;

import de.funboyy.labymod.emote.npc.EmoteNPCPlugin;
import de.funboyy.labymod.emote.npc.config.Config;
import de.funboyy.labymod.emote.npc.utils.ItemBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class EmoteManager {

    private static EmoteManager instance;

    public static EmoteManager getInstance() {
        if (instance == null) {
            instance = new EmoteManager();
        }
        return instance;
    }

    @Getter private final Set<Emote> emotes;

    public EmoteManager() {
        this.emotes = new HashSet<>();
    }

    public void loadEmotes() {
        try {
            final InputStream inputStream = EmoteNPCPlugin.getInstance().getResource("emotes.txt");

            if (inputStream == null) {
                return;
            }

            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            for (String line; (line = reader.readLine()) != null;) {
                final Emote emote = getEmoteFromString(line);

                if (emote == null) {
                    continue;
                }

                this.emotes.add(emote);

                if (Config.getInstance().debug()) {
                    Bukkit.getLogger().info("[" + EmoteNPCPlugin.getInstance().getName() + "] " +
                            "Emote " + emote.getName() + " (#" + emote.getId() + ") registered.");
                }
            }
        } catch (final IOException exception) {
            exception.printStackTrace();
        }
    }

    public Emote getEmoteById(final int id) {
        return this.emotes.stream().filter(emote -> emote.getId() == id).findAny().orElse(null);
    }

    private Emote getEmoteFromString(final String string) {
        try {
            final int id = Integer.parseInt(string.split(" ")[0]);
            final String name = string.replaceFirst(id + " ", "");

            return new Emote(name, id);
        } catch (final NumberFormatException ignored) {
            return null;
        }
    }

}
