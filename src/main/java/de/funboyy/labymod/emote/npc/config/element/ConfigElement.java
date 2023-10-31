package de.funboyy.labymod.emote.npc.config.element;

import de.funboyy.labymod.emote.npc.config.Config;

public abstract class ConfigElement<V> {

    protected final String path;

    public ConfigElement(final String path) {
        this.path = path;
    }

    public void set(final V value) {
        Config.getFile().set(this.path, value);

        save();
    }

    public abstract V get();

    protected void save() {
        Config.save();
        Config.update();
    }

}
