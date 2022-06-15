package de.funboyy.labymod.emote.npc.packet.mapping;

import de.funboyy.labymod.emote.npc.utils.Version;

public class MethodMapping {

    private final Version version;

    public MethodMapping() {
        this.version = Version.getInstance();
    }

    public String hasTag() {
        switch (this.version.getId()) {
            case Version.v1_18_R1:
                return "r";
            case Version.v1_18_R2:
                return "s";
            case Version.v1_19_R1:
                return "t";
            default:
                return "hasTag";
        }
    }

    public String getTag() {
        switch (this.version.getId()) {
            case Version.v1_18_R1:
                return "s";
            case Version.v1_18_R2:
                return "t";
            case Version.v1_19_R1:
                return "u";
            default:
                return "getTag";
        }
    }

    public String setString() {
        if (this.version.getId() >= Version.v1_18_R1) {
            return "a";
        }

        return "setString";
    }

    public String getString() {
        if (this.version.getId() >= Version.v1_18_R1) {
            return "l";
        }

        return "getString";
    }

    public String playerConnection() {
        if (this.version.hasNewProtocol()) {
            return "b";
        }

        return "playerConnection";
    }

    public String networkManager() {
        if (this.version.getId() == Version.v1_19_R1) {
            return "b";
        }

        if (this.version.hasNewProtocol()) {
            return "a";
        }

        return "networkManager";
    }

    public String channel() {
        switch (this.version.getId()) {
            case Version.v1_8_R1:
                return "i";
            case Version.v1_8_R2:
            case Version.v1_17_R1:
            case Version.v1_18_R1:
                return "k";
            case Version.v1_18_R2:
            case Version.v1_19_R1:
                return "m";
            default:
                return "channel";
        }
    }

    public String sendPacket() {
        if (this.version.getId() >= Version.v1_18_R1) {
            return "a";
        }

        return "sendPacket";
    }

    public String setPrefix() {
        if (this.version.getId() >= Version.v1_18_R1) {
            return "b";
        }

        return "setPrefix";
    }

    public String setSuffix() {
        if (this.version.getId() >= Version.v1_18_R1) {
            return "c";
        }

        return "setSuffix";
    }

    public String getPlayerNameSet() {
        if (this.version.getId() >= Version.v1_18_R1) {
            return "g";
        }

        return "getPlayerNameSet";
    }

    public String getId() {
        if (this.version.getId() >= Version.v1_18_R1) {
            return "ae";
        }

        return "getId";
    }

    public String getDataWatcher() {
        if (this.version.getId() >= Version.v1_18_R1) {
            return "ai";
        }

        return "getDataWatcher";
    }

    public String set() {
        if (this.version.getId() >= Version.v1_18_R1) {
            return "b";
        }

        return "set";
    }

    public String getProfile() {
        switch (this.version.getId()) {
            case Version.v1_19_R1:
                return "fz";
            case Version.v1_18_R2:
                return "fq";
            case Version.v1_18_R1:
                return "fp";
            default:
                return "getProfile";
        }
    }

}
