package de.funboyy.labymod.emote.npc.packet;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import de.funboyy.labymod.emote.npc.EmoteNPCPlugin;
import de.funboyy.labymod.emote.npc.config.Config;
import de.funboyy.labymod.emote.npc.utils.NMSObject;
import de.funboyy.labymod.emote.npc.utils.NMSReflection;
import de.funboyy.labymod.emote.npc.utils.Versions;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class EmoteNPC_1_17to1_18_1 implements IEmoteNPC {

    private final Player player;
    @Getter private Integer entityId;
    @Getter private UUID uuid;
    private GameProfile gameProfile;
    private String value = "";
    private String signature = "";

    private NMSObject team;
    private NMSObject entityPlayer;

    private String name = "";

    @Getter private boolean spawned = false;

    public EmoteNPC_1_17to1_18_1(final Player player) {
        this.player = player;
    }

    private void setName() {
        try {
            final String prefix = Config.getInstance().prefix().length() > 16 ?
                    Config.getInstance().prefix().substring(0, 16) : Config.getInstance().prefix();
            this.name = Config.getInstance().name().length() > 16 ?
                    Config.getInstance().name().substring(0, 16) : Config.getInstance().name();
            final String suffix = Config.getInstance().suffix().length() > 16 ?
                    Config.getInstance().suffix().substring(0, 16) : Config.getInstance().suffix();

            final Object scoreboard = NMSReflection.getInstance().getClass("world.scores.Scoreboard").newInstance();
            final Class<?> scoreboardTeam = NMSReflection.getInstance().getClass("world.scores.ScoreboardTeam");
            final Constructor<?> teamConstructor = scoreboardTeam.getConstructor(scoreboard.getClass(), String.class);
            this.team = new NMSObject(teamConstructor.newInstance(scoreboard, "000-NPC"));

            final Class<?> chatBaseComponentClass = NMSReflection.getInstance().getClass("network.chat.IChatBaseComponent");
            final Class<?> craftChatMessageClass = NMSReflection.getInstance().getBukkitClass("util.CraftChatMessage");

            final Object prefixObject = ((Object[]) new NMSObject(craftChatMessageClass)
                    .getDeclaredMethod("fromString", String.class).invoke(prefix))[0];
            final Object suffixObject = ((Object[]) new NMSObject(craftChatMessageClass)
                    .getDeclaredMethod("fromString", String.class).invoke(suffix))[0];

            this.team.getMethod(Versions.getInstance().getId() == Versions.v1_17_R1 ?
                    "setPrefix" : "b", chatBaseComponentClass).invoke(prefixObject);
            this.team.getMethod(Versions.getInstance().getId() == Versions.v1_17_R1 ?
                    "setSuffix" : "c", chatBaseComponentClass).invoke(suffixObject);

            @SuppressWarnings("unchecked")
            final Collection<String> playerNameSet = (Collection<String>) this.team.getMethod(
                    Versions.getInstance().getId() == Versions.v1_17_R1 ? "getPlayerNameSet" : "g").invoke();
            playerNameSet.add(this.name);

            final NMSObject packetObject = new NMSObject(NMSReflection.getInstance().getClass("network.protocol.game.PacketPlayOutScoreboardTeam"));
            final Object packet = packetObject.getDeclaredMethod("a", scoreboardTeam, boolean.class).invoke(this.team.getObject(), false);
            NMSReflection.getInstance().sendPacket(this.player, packet);
        } catch (final InstantiationException | IllegalAccessException | NoSuchMethodException
                | InvocationTargetException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void spawn() {
        try {
            this.spawned = true;

            setName();
            setSkin();

            final Location location = Config.getInstance().getLocation();
            this.uuid = new UUID(new Random().nextLong(), 0);
            this.gameProfile = new GameProfile(this.uuid, this.name);
            if (!this.value.equals("") && !this.signature.equals("")) {
                this.gameProfile.getProperties().put("textures", new Property("textures", this.value, this.signature));
            }

            if (location.getWorld() == null) {
                Bukkit.getLogger().warning("[" + EmoteNPCPlugin.getInstance().getName() + "] " +
                        "Can not spawn NPC because the specified world was not found");
                return;
            }

            final Object minecraftServer = new NMSObject(Bukkit.getServer()).getMethod("getServer").invoke();
            final Object worldServer = new NMSObject(location.getWorld()).getMethod("getHandle").invoke();
            
            final Class<?> minecraftServerClass = NMSReflection.getInstance().getClass("server.MinecraftServer");
            final Class<?> entityPlayerClass = NMSReflection.getInstance().getClass("server.level.EntityPlayer");
            final Constructor<?> entityPlayerConstructor = entityPlayerClass.getConstructor(
                    minecraftServerClass, worldServer.getClass(), GameProfile.class);
            this.entityPlayer = new NMSObject(entityPlayerConstructor.newInstance(
                    minecraftServer, worldServer, this.gameProfile));

            this.entityId = (int) this.entityPlayer.getMethod(
                    Versions.getInstance().getId() == Versions.v1_17_R1 ? "getId" : "ae").invoke();

            final Class<?> entitySpawnClass = NMSReflection.getInstance().getClass("network.protocol.game.PacketPlayOutNamedEntitySpawn");
            final Constructor<?> entitySpawnConstructor = entitySpawnClass.getConstructor(
                    NMSReflection.getInstance().getClass("world.entity.player.EntityHuman"));
            final Object packetPlayOutNamedEntitySpawn = entitySpawnConstructor.newInstance(this.entityPlayer.getObject());

            NMSReflection.getInstance().setValue(packetPlayOutNamedEntitySpawn, "a", this.entityId);
            NMSReflection.getInstance().setValue(packetPlayOutNamedEntitySpawn, "b", this.gameProfile.getId());
            NMSReflection.getInstance().setValue(packetPlayOutNamedEntitySpawn, "c", location.getX());
            NMSReflection.getInstance().setValue(packetPlayOutNamedEntitySpawn, "d", location.getY());
            NMSReflection.getInstance().setValue(packetPlayOutNamedEntitySpawn, "e", location.getZ());
            NMSReflection.getInstance().setValue(packetPlayOutNamedEntitySpawn, "f", getFixRotation(location.getYaw()));
            NMSReflection.getInstance().setValue(packetPlayOutNamedEntitySpawn, "g", getFixRotation(location.getPitch()));

            handlePlayerList("ADD_PLAYER");

            NMSReflection.getInstance().sendPacket(this.player, packetPlayOutNamedEntitySpawn);

            sneak(false);
            headRotation(location.getYaw(), location.getPitch());

            if (Config.getInstance().lookClose()) {
                final Location playerLocation = this.player.getLocation();
                final Location npcLocation = playerLocation.setDirection(playerLocation.subtract(location).toVector());
                headRotation(npcLocation.getYaw(), npcLocation.getPitch());
            }

            if (Config.getInstance().sneak()) {
                sneak(this.player.isSneaking());
            }

            final Class<?> entityClass = NMSReflection.getInstance().getClass("world.entity.Entity");
            final Class<?> animationPacketClass = NMSReflection.getInstance().getClass("network.protocol.game.PacketPlayOutAnimation");
            final Constructor<?> animationPacketConstructor = animationPacketClass.getConstructor(entityClass, int.class);
            final Object animationPacket = animationPacketConstructor.newInstance(this.entityPlayer.getObject(), 0);

            NMSReflection.getInstance().sendPacket(this.player, animationPacket);

            Bukkit.getScheduler().runTaskLater(EmoteNPCPlugin.getInstance(), () -> handlePlayerList("REMOVE_PLAYER"), 10);

            if (Config.getInstance().debug()) {
                Bukkit.getLogger().info("[" + EmoteNPCPlugin.getInstance().getName() + "] " +
                        "Spawned NPC for " + this.player.getName());
            }
        } catch (final InstantiationException | IllegalAccessException | NoSuchMethodException
                | InvocationTargetException exception) {
            exception.printStackTrace();
        }
    }

    @SuppressWarnings("all")
    @Override
    public void remove() {
        try {
            if (this.entityId == null || !this.spawned) {
                return;
            }

            this.spawned = false;

            final Class<?> destroyPacket = NMSReflection.getInstance().getClass("network.protocol.game.PacketPlayOutEntityDestroy");
            final Constructor<?> destroyPacketConstructor = destroyPacket.getConstructor(int[].class);
            NMSReflection.getInstance().sendPacket(this.player, destroyPacketConstructor.newInstance(new int[]{ this.entityId }));

            final NMSObject teamPacket = new NMSObject(NMSReflection.getInstance()
                    .getClass("network.protocol.game.PacketPlayOutScoreboardTeam"));
            final Object packet = teamPacket.getDeclaredMethod("a", this.team.getObject().getClass()).invoke(this.team.getObject());
            NMSReflection.getInstance().sendPacket(this.player, packet);

            if (Config.getInstance().debug()) {
                Bukkit.getLogger().info("[" + EmoteNPCPlugin.getInstance().getName() + "] " +
                        "Removed NPC for " + this.player.getName());
            }
        } catch (final NoSuchMethodException | InstantiationException | IllegalAccessException
                | InvocationTargetException exception) {
            exception.printStackTrace();
        }
    }

    @SuppressWarnings("all")
    private void handlePlayerList(final String action) {
        try {
            final Class<?> playerInfoClass = NMSReflection.getInstance().getClass("network.protocol.game.PacketPlayOutPlayerInfo");
            final Class<?> chatBaseComponentClass = NMSReflection.getInstance().getClass("network.chat.IChatBaseComponent");
            final Class<?> enumGameModeClass = NMSReflection.getInstance().getClass("world.level.EnumGamemode");
            final Class<?> craftChatMessageClass = NMSReflection.getInstance().getBukkitClass("util.CraftChatMessage");
            final Class<?> enumPlayerInfoActionClass = NMSReflection.getInstance().getClass("network.protocol.game.PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
            final Class<?> playerInfoDataClass = NMSReflection.getInstance().getClass("network.protocol.game.PacketPlayOutPlayerInfo$PlayerInfoData");
            final Class<?> entityPlayerClass = NMSReflection.getInstance().getClass("server.level.EntityPlayer");

            final Object enumGameMode = NMSReflection.getInstance().getEnum(enumGameModeClass, "SURVIVAL");
            final Object craftChatMessage = ((Object[]) new NMSObject(craftChatMessageClass)
                    .getDeclaredMethod("fromString", String.class).invoke(""))[0];
            final Object enumPlayerInfoAction = NMSReflection.getInstance().getEnum(enumPlayerInfoActionClass, action);

            final Object[] entityPlayerArray = (Object[]) Array.newInstance(entityPlayerClass, 1);
            entityPlayerArray[0] = this.entityPlayer.getObject();

            final Constructor<?> playerInfoConstructor = playerInfoClass.getConstructor(enumPlayerInfoActionClass,
                    entityPlayerArray.getClass());
            final Object playerInfoPacket = playerInfoConstructor.newInstance(
                    enumPlayerInfoAction, entityPlayerArray);

            final Constructor<?> playerInfoDataConstructor = playerInfoDataClass.getConstructor(
                    GameProfile.class, int.class, enumGameModeClass, chatBaseComponentClass);
            final Object playerInfoData = playerInfoDataConstructor.newInstance(
                    this.gameProfile, 1, enumGameMode, craftChatMessage);

            final List<Object> players = new ArrayList<>();
            players.add(playerInfoData);

            NMSReflection.getInstance().setValue(playerInfoPacket, "b", players);

            NMSReflection.getInstance().sendPacket(this.player, playerInfoPacket);
        } catch (final InstantiationException | IllegalAccessException | NoSuchMethodException
                | InvocationTargetException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void headRotation(final float yaw, final float pitch) {
        try {

            final Class<?> headRotationPacketClass = NMSReflection.getInstance().getClass("network.protocol.game.PacketPlayOutEntityHeadRotation");
            final Constructor<?> headRotationPacketConstructor = headRotationPacketClass.getConstructor(
                    NMSReflection.getInstance().getClass("world.entity.Entity"), byte.class);
            final Object headRotationPacket = headRotationPacketConstructor.newInstance(
                    this.entityPlayer.getObject(), getFixRotation(yaw));

            final Class<?> lookPacketClass = NMSReflection.getInstance().getClass("network.protocol.game.PacketPlayOutEntity$PacketPlayOutEntityLook");
            final Constructor<?> lookPacketConstructor = lookPacketClass.getConstructor(int.class, byte.class, byte.class, boolean.class);
            final Object lookPacket = lookPacketConstructor.newInstance(this.entityId, getFixRotation(yaw), getFixRotation(pitch), true);

            NMSReflection.getInstance().sendPacket(this.player, lookPacket);
            NMSReflection.getInstance().sendPacket(this.player, headRotationPacket);
        } catch (final InstantiationException | IllegalAccessException | NoSuchMethodException
                | InvocationTargetException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void sneak(final boolean sneaking) {
        try {
            final Class<?> watcherClass = NMSReflection.getInstance().getClass("network.syncher.DataWatcher");
            final NMSObject watcher = new NMSObject(this.entityPlayer.getMethod(
                    Versions.getInstance().getId() == Versions.v1_17_R1 ? "getDataWatcher" : "ai").invoke());

            final Class<?> watcherObjectClass = NMSReflection.getInstance().getClass("network.syncher.DataWatcherObject");
            final Class<?> watcherSerializerClass = NMSReflection.getInstance().getClass("network.syncher.DataWatcherSerializer");
            final Class<?> watcherRegistryClass = NMSReflection.getInstance().getClass("network.syncher.DataWatcherRegistry");

            final Object watcherRegistryA = watcherRegistryClass.getField("a").get(watcherRegistryClass);
            final Object watcherRegistryC = watcherRegistryClass.getField("c").get(watcherRegistryClass);

            final Constructor<?> watcherObjectConstructor = watcherObjectClass.getConstructor(int.class, watcherSerializerClass);
            final Object watcherObject0 = watcherObjectConstructor.newInstance(0, watcherRegistryA);
            final Object watcherObject1 = watcherObjectConstructor.newInstance(
                    Versions.getInstance().getHealth(), watcherRegistryC);
            final Object watcherObject2 = watcherObjectConstructor.newInstance(
                    Versions.getInstance().getSkinOverlay(), watcherRegistryA);

            final String method = Versions.getInstance().getId() == Versions.v1_17_R1 ? "set" : "b";

            watcher.getMethod(method, watcherObjectClass, Object.class).invoke(watcherObject0, (byte) (sneaking ? 2 : 0));
            watcher.getMethod(method, watcherObjectClass, Object.class).invoke(watcherObject1, (float) 20);
            watcher.getMethod(method, watcherObjectClass, Object.class).invoke(watcherObject2, (byte) 127);

            final Object watcherRegistryS = watcherRegistryClass.getField("s").get(watcherRegistryClass);
            final Object watcherObject3 = watcherObjectConstructor.newInstance(6, watcherRegistryS);

            final Class<?> entityPoseClass = NMSReflection.getInstance().getClass("world.entity.EntityPose");
            final Object standingPose = NMSReflection.getInstance().getEnum(entityPoseClass, "STANDING");
            final Object sneakingPose = NMSReflection.getInstance().getEnum(entityPoseClass, "CROUCHING");

            watcher.getMethod(method, watcherObjectClass, Object.class).invoke(watcherObject3, sneaking ? sneakingPose : standingPose);

            final Class<?> packetClass = NMSReflection.getInstance().getClass("network.protocol.game.PacketPlayOutEntityMetadata");
            final Constructor<?> packetConstructor = packetClass.getConstructor(int.class, watcherClass, boolean.class);
            final Object packet = packetConstructor.newInstance(this.entityId, watcher.getObject(), true);

            NMSReflection.getInstance().sendPacket(this.player, packet);
        } catch (final NoSuchMethodException | InstantiationException | IllegalAccessException
                | InvocationTargetException | NoSuchFieldException exception) {
            exception.printStackTrace();
        }
    }

    private void setSkin() {
        final GameProfile profile = (GameProfile) new NMSObject(NMSReflection.getInstance().getEntityPlayer(this.player))
                .getMethod(Versions.getInstance().getId() == Versions.v1_18_R1 ? "fp" : "getProfile").invoke();
        final Property property = profile.getProperties().get("textures").iterator().next();
        this.value = property.getValue();
        this.signature = property.getSignature();
    }

    private byte getFixRotation(final float rotation){
        return (byte) ((int) (rotation * 256.0F / 360.0F));
    }
}
