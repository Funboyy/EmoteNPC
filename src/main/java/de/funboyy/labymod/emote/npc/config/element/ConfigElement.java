package de.funboyy.labymod.emote.npc.config.element;

public class ConfigElement<V> {

    private final Setter<V> setter;
    private final Getter<V> getter;

    public ConfigElement(final Setter<V> setter, final Getter<V> getter) {
        this.setter = setter;
        this.getter = getter;
    }

    public void set(final V value) {
        this.setter.set(value);
    }

    public V get() {
        return this.getter.get();
    }

    public interface Setter<V> {

        void set(final V value);

    }

    public interface Getter<V> {

        V get();

    }

}
