package de.funboyy.labymod.emote.npc.utils;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.Sound;

public class Versions {

    private static Versions instance;

    public static void init(final String version) throws RuntimeException {
        try {
            final int versionId = Integer.parseInt(version.replace("_", "")
                    .replace("R", "").substring(1));
            if (versionId < 181 || versionId > 1165) {
                throw new RuntimeException("Wrong version");
            }

            instance = new Versions(version);
        } catch (final NumberFormatException ignored) {
            throw new RuntimeException("Wrong version");
        }
    }

    public static Versions getInstance() {
        if (instance == null) {
            throw new RuntimeException("Version was not initialized");
        }
        return instance;
    }

    @Getter private final String version;
    @Getter private final int versionId;

    private Versions(final String version) {
        this.version = version;
        this.versionId = Integer.parseInt(version.replace("_", "")
                .replace("R", "").substring(1));
    }

    public boolean isMinecraft18() {
        return this.versionId >= 181 && this.versionId <= 183;
    }

    public boolean isMinecraft19() {
        return this.versionId == 191 || this.versionId == 192;
    }

    public Material getSkull() {
        return Versions.getInstance().getVersionId() <= 1121
                ? Material.valueOf("SKULL_ITEM") : Material.valueOf("PLAYER_HEAD");
    }

    public Sound getSound() {
        return isMinecraft18() ? Sound.valueOf("CLICK") : Sound.valueOf("UI_BUTTON_CLICK");
    }

    public int getHealth() {
        if (isMinecraft18() || isMinecraft19()) {
            return 6;
        }

        if (this.versionId <= 1132) {
            return 7;
        }

        return 8;
    }

    public int getSkinOverlay() {
        if (isMinecraft18()) {
            return 10;
        }

        if (isMinecraft19()) {
            return 12;
        }

        if (this.versionId <= 1132) {
            return 13;
        }

        if (this.versionId <= 1141) {
            return 15;
        }

        return 16;
    }

}
