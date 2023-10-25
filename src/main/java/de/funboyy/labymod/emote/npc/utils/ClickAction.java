package de.funboyy.labymod.emote.npc.utils;

import java.util.Arrays;

public enum ClickAction {

    PLAY_EMOTE,
    STOP_EMOTE,
    PREVIOUS_PAGE,
    NEXT_PAGE;

    public static final String ACTION_KEY = "ClickAction";
    public static final String EMOTE_KEY = "Emote";
    public static final String PAGE_KEY = "Page";

    public static ClickAction getByString(final String name) {
        return Arrays.stream(values()).filter(action -> action.name().equals(name)).findFirst().orElse(null);
    }

}
