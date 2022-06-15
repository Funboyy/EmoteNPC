package de.funboyy.labymod.emote.npc.utils;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.Sound;

@SuppressWarnings("unused")
public class Version {

    public static final int v1_8_R1 = 1_8_1;
    public static final int v1_8_R2 = 1_8_2;
    public static final int v1_8_R3 = 1_8_3;
    public static final int v1_9_R1 = 1_9_1;
    public static final int v1_9_R2 = 1_9_2;
    public static final int v1_10_R1 = 1_10_1;
    public static final int v1_11_R1 = 1_11_1;
    public static final int v1_12_R1 = 1_12_1;
    public static final int v1_13_R1 = 1_13_1;
    public static final int v1_13_R2 = 1_13_2;
    public static final int v1_14_R1 = 1_14_1;
    public static final int v1_15_R1 = 1_15_1;
    public static final int v1_16_R1 = 1_16_1;
    public static final int v1_16_R2 = 1_16_2;
    public static final int v1_16_R3 = 1_16_3;
    public static final int v1_17_R1 = 1_17_1;
    public static final int v1_18_R1 = 1_18_1;
    public static final int v1_18_R2 = 1_18_2;
    public static final int v1_19_R1 = 1_19_1;

    private static Version instance;

    public static void init(final String version) throws RuntimeException {
        try {
            final int versionId = Integer.parseInt(version.replace("_", "")
                    .replace("R", "").substring(1));
            if (versionId < v1_8_R1 || versionId > v1_19_R1) {
                    throw new RuntimeException("Wrong version");
            }

            instance = new Version(version);
        } catch (final NumberFormatException ignored) {
            throw new RuntimeException("Wrong version");
        }
    }

    public static Version getInstance() {
        if (instance == null) {
            throw new RuntimeException("Version was not initialized");
        }
        return instance;
    }

    @Getter private final String version;
    @Getter private final int id;

    private Version(final String version) {
        this.version = version;
        this.id = Integer.parseInt(version.replace("_", "")
                .replace("R", "").substring(1));
    }

    public boolean hasNewProtocol() {
        return this.id >= v1_17_R1;
    }

    public Material getSkull() {
        return this.id <= v1_12_R1
                ? Material.valueOf("SKULL_ITEM") : Material.valueOf("PLAYER_HEAD");
    }

    public Sound getSound() {
        return this.id <= v1_8_R3 ? Sound.valueOf("CLICK") : Sound.valueOf("UI_BUTTON_CLICK");
    }

    public int getHealth() {
        if (this.id <= v1_9_R2) {
            return 6;
        }

        if (this.id <= v1_13_R2) {
            return 7;
        }

        if (this.id <= v1_16_R3) {
            return 8;
        }

        return 9;
    }

    public int getSkinOverlay() {
        if (this.id <= v1_8_R3) {
            return 10;
        }

        if (this.id <= v1_9_R2) {
            return 12;
        }

        if (this.id <= v1_13_R2) {
            return 13;
        }

        if (this.id <= v1_14_R1) {
            return 15;
        }

        if (this.id <= v1_16_R3) {
            return 16;
        }

        return 17;
    }

}
