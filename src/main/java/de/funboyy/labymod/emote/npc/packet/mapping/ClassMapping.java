package de.funboyy.labymod.emote.npc.packet.mapping;

import de.funboyy.labymod.emote.npc.utils.Version;

public class ClassMapping {

    private final Version version;
    private final boolean newProtocol;
    
    public ClassMapping() {
        this.version = Version.getInstance();
        this.newProtocol = this.version.hasNewProtocol();
    }

    private Class<?> getBukkitClass(final String name) {
        return getClassByName("org.bukkit.craftbukkit." + Version.getInstance().getVersion() + "." + name);
    }

    private Class<?> getOldClass(final String name) {
        return this.getClassByName("net.minecraft.server." + Version.getInstance().getVersion() + "." + name);
    }

    private Class<?> getNewClass(final String name) {
        return this.getClassByName("net.minecraft." + name);
    }
    
    private Class<?> getClassByName(final String name) {
        try {
            return Class.forName(name);
        } catch (final ClassNotFoundException exception) {
            exception.printStackTrace();
        }

        return null;
    }


    public Class<?> getCraftItemStack() {
        return this.getBukkitClass("inventory.CraftItemStack");
    }

    public Class<?> getCraftChatMessage() {
        return this.getBukkitClass("util.CraftChatMessage");
    }
    

    public Class<?> getNBTTagCompound() {
        if (this.newProtocol) {
            return this.getNewClass("nbt.NBTTagCompound");
        }

        return this.getOldClass("NBTTagCompound");
    }

    public Class<?> getItemStack() {
        if (this.newProtocol) {
            return this.getNewClass("world.item.ItemStack");
        }

        return this.getOldClass("ItemStack");
    }

    public Class<?> getPacket() {
        if (this.newProtocol) {
            return this.getNewClass("network.protocol.Packet");
        }

        return this.getOldClass("Packet");
    }

    public Class<?> getScoreboard() {
        if (this.newProtocol) {
            return this.getNewClass("world.scores.Scoreboard");
        }

        return this.getOldClass("Scoreboard");
    }

    public Class<?> getScoreboardTeam() {
        if (this.newProtocol) {
            return this.getNewClass("world.scores.ScoreboardTeam");
        }

        return this.getOldClass("ScoreboardTeam");
    }

    public Class<?> getIChatBaseComponent() {
        if (this.newProtocol) {
            return this.getNewClass("network.chat.IChatBaseComponent");
        }

        return this.getOldClass("IChatBaseComponent");
    }

    public Class<?> getPacketPlayOutScoreboardTeam() {
        if (this.newProtocol) {
            return this.getNewClass("network.protocol.game.PacketPlayOutScoreboardTeam");
        }

        return this.getOldClass("PacketPlayOutScoreboardTeam");
    }

    public Class<?> getPacketPlayOutNamedEntitySpawn() {
        if (this.newProtocol) {
            return this.getNewClass("network.protocol.game.PacketPlayOutNamedEntitySpawn");
        }

        return this.getOldClass("PacketPlayOutNamedEntitySpawn");
    }

    public Class<?> getEntityHuman() {
        if (this.newProtocol) {
            return this.getNewClass("world.entity.player.EntityHuman");
        }

        return this.getOldClass("EntityHuman");
    }

    public Class<?> getWorld() {
        if (this.newProtocol) {
            return this.getNewClass("world.level.World");
        }

        return this.getOldClass("World");
    }

    public Class<?> getPlayerInteractManager() {
        if (this.newProtocol) {
            return this.getNewClass("server.level.InteractManager");
        }

        return this.getOldClass("PlayerInteractManager");
    }

    public Class<?> getMinecraftServer() {
        if (this.newProtocol) {
            return this.getNewClass("server.MinecraftServer");
        }

        return this.getOldClass("MinecraftServer");
    }

    public Class<?> getEntityPlayer() {
        if (this.newProtocol) {
            return this.getNewClass("server.level.EntityPlayer");
        }

        return this.getOldClass("EntityPlayer");
    }

    public Class<?> getEntity() {
        if (this.newProtocol) {
            return this.getNewClass("world.entity.Entity");
        }

        return this.getOldClass("Entity");
    }

    public Class<?> getDataWatcher() {
        if (this.newProtocol) {
            return this.getNewClass("network.syncher.DataWatcher");
        }

        return this.getOldClass("DataWatcher");
    }

    public Class<?> getPacketPlayOutAnimation() {
        if (this.newProtocol) {
            return this.getNewClass("network.protocol.game.PacketPlayOutAnimation");
        }

        return this.getOldClass("PacketPlayOutAnimation");
    }

    public Class<?> getPacketPlayOutEntityDestroy() {
        if (this.newProtocol) {
            return this.getNewClass("network.protocol.game.PacketPlayOutEntityDestroy");
        }

        return this.getOldClass("PacketPlayOutEntityDestroy");
    }

    public Class<?> getPacketPlayOutPlayerInfo() {
        if (this.newProtocol) {
            return this.getNewClass("network.protocol.game.PacketPlayOutPlayerInfo");
        }

        return this.getOldClass("PacketPlayOutPlayerInfo");
    }

    public Class<?> getEnumGamemode() {
        if (this.newProtocol) {
            return this.getNewClass("world.level.EnumGamemode");
        }

        final int versionId = this.version.getId();
        if (versionId >= Version.v1_8_R2 && versionId <= Version.v1_9_R2) {
            return this.getOldClass("WorldSettings$EnumGamemode");
        }

        return this.getOldClass("EnumGamemode");
    }

    public Class<?> getEnumPlayerInfoAction() {
        if (this.newProtocol) {
            return this.getNewClass("network.protocol.game.PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
        }

        if (this.version.getId() == Version.v1_8_R1) {
            return this.getOldClass("EnumPlayerInfoAction");
        }

        return this.getOldClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
    }

    public Class<?> getPlayerInfoData() {
        if (this.newProtocol) {
            return this.getNewClass("network.protocol.game.PacketPlayOutPlayerInfo$PlayerInfoData");
        }

        if (this.version.getId() == Version.v1_8_R1) {
            return this.getOldClass("PlayerInfoData");
        }

        return this.getOldClass("PacketPlayOutPlayerInfo$PlayerInfoData");
    }

    public Class<?> getPacketPlayOutEntityHeadRotation() {
        if (this.newProtocol) {
            return this.getNewClass("network.protocol.game.PacketPlayOutEntityHeadRotation");
        }

        return this.getOldClass("PacketPlayOutEntityHeadRotation");
    }

    public Class<?> getPacketPlayOutEntityLook() {
        if (this.newProtocol) {
            return this.getNewClass("network.protocol.game.PacketPlayOutEntity$PacketPlayOutEntityLook");
        }

        if (this.version.getId() == Version.v1_8_R1) {
            return this.getOldClass("PacketPlayOutEntityLook"); 
        }
        
        return this.getOldClass("PacketPlayOutEntity$PacketPlayOutEntityLook");
    }

    public Class<?> getDataWatcherObject() {
        if (this.newProtocol) {
            return this.getNewClass("network.syncher.DataWatcherObject");
        }

        return this.getOldClass("DataWatcherObject");
    }

    public Class<?> getDataWatcherSerializer() {
        if (this.newProtocol) {
            return this.getNewClass("network.syncher.DataWatcherSerializer");
        }

        return this.getOldClass("DataWatcherSerializer");
    }

    public Class<?> getDataWatcherRegistry() {
        if (this.newProtocol) {
            return this.getNewClass("network.syncher.DataWatcherRegistry");
        }

        return this.getOldClass("DataWatcherRegistry");
    }

    public Class<?> getEntityPose() {
        if (this.newProtocol) {
            return this.getNewClass("world.entity.EntityPose");
        }

        return this.getOldClass("EntityPose");
    }

    public Class<?> getPacketPlayOutEntityMetadata() {
        if (this.newProtocol) {
            return this.getNewClass("network.protocol.game.PacketPlayOutEntityMetadata");
        }

        return this.getOldClass("PacketPlayOutEntityMetadata");
    }

    public Class<?> getMathHelper() {
        if (this.newProtocol) {
            return this.getNewClass("util.MathHelper");
        }

        return this.getOldClass("MathHelper");
    }

    public Class<?> getPacketDataSerializer() {
        if (this.newProtocol) {
            return this.getNewClass("network.PacketDataSerializer");
        }

        return this.getOldClass("PacketDataSerializer");
    }

    public Class<?> getPacketPlayOutCustomPayload() {
        if (this.newProtocol) {
            return this.getNewClass("network.protocol.game.PacketPlayOutCustomPayload");
        }

        return this.getOldClass("PacketPlayOutCustomPayload");
    }

    public Class<?> getMinecraftKey() {
        if (this.newProtocol) {
            return this.getNewClass("resources.MinecraftKey");
        }

        return this.getOldClass("MinecraftKey");
    }

    public Class<?> getEnumEntityUseAction() {
        if (this.newProtocol) {
            return this.getNewClass("network.protocol.game.PacketPlayInUseEntity$b");
        }

        return this.getOldClass("PacketPlayInUseEntity.EnumEntityUseAction");
    }

    public Class<?> getProfilePublicKey() {
        if (this.version.getId() == Version.v1_19_R1) {
            return this.getNewClass("world.entity.player.ProfilePublicKey");
        }

        throw new RuntimeException("This class does not exist in this version. Was the wrong version detected?");
    }

    public Class<?> getProfilePublicKeyA() {
        if (this.version.getId() == Version.v1_19_R1) {
            return this.getNewClass("world.entity.player.ProfilePublicKey$a");
        }

        throw new RuntimeException("This class does not exist in this version. Was the wrong version detected?");
    }
    
}
