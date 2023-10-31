package de.funboyy.labymod.emote.npc.config.element;

import de.funboyy.labymod.emote.npc.config.Config;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigMessageList extends ConfigElement<List<String>> {

    public ConfigMessageList(final String path) {
        super(path);
    }

    @Override
    public List<String> get() {
        return Config.getFile().getStringList(super.path).stream().map(value ->
                ConfigMessage.format(ConfigString.format(value))).collect(Collectors.toList());
    }

}
