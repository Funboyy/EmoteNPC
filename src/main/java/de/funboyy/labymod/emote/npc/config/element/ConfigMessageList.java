package de.funboyy.labymod.emote.npc.config.element;

import de.funboyy.labymod.emote.npc.config.Config;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigMessageList extends ConfigElement<List<String>> {

    public ConfigMessageList(final String path) {
        super(value -> {}, () -> Config.getFile().getStringList(path).stream().map(value ->
                ConfigMessage.format(ConfigString.format(value))).collect(Collectors.toList()));
    }

}
