package de.funboyy.labymod.emote.npc.packet;

import java.util.UUID;

public interface IEmoteNPC {

    Integer getEntityId();

    UUID getUuid();

    boolean isSpawned();

    void spawn();

    void remove();

    void headRotation(final float yaw, final float pitch);

    void sneak(final boolean sneaking);

}
