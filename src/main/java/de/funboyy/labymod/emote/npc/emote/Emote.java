package de.funboyy.labymod.emote.npc.emote;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class Emote {

    private final String name;
    private final int id;
    private final boolean draft;

}
