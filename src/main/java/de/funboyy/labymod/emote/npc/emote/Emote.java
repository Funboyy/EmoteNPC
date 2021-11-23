package de.funboyy.labymod.emote.npc.emote;

import lombok.Getter;

public class Emote {

    @Getter private final String name;
    @Getter private final int id;

    protected Emote(final String name, final int id) {
        this.name = name;
        this.id = id;
    }

}
