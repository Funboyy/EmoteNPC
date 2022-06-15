package de.funboyy.labymod.emote.npc.packet;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import de.funboyy.labymod.emote.npc.EmoteNPCPlugin;
import de.funboyy.labymod.emote.npc.config.Config;
import de.funboyy.labymod.emote.npc.packet.mapping.ClassMapping;
import de.funboyy.labymod.emote.npc.packet.mapping.MethodMapping;
import de.funboyy.labymod.emote.npc.packet.nms.NMSObject;
import de.funboyy.labymod.emote.npc.packet.nms.NMSReflection;
import de.funboyy.labymod.emote.npc.utils.Version;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class EmoteNPC {

    private final ClassMapping classMapping;
    private final MethodMapping methodMapping;

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

    public EmoteNPC(final Player player) {
        this.player = player;
        this.classMapping = EmoteNPCPlugin.getInstance().getClassManager();
        this.methodMapping = EmoteNPCPlugin.getInstance().getMethodManager();
    }

    private void setName() {
        try {
            final String prefix = Config.getInstance().prefix().length() > 16 ?
                    Config.getInstance().prefix().substring(0, 16) : Config.getInstance().prefix();
            this.name = Config.getInstance().name().length() > 16 ?
                    Config.getInstance().name().substring(0, 16) : Config.getInstance().name();
            final String suffix = Config.getInstance().suffix().length() > 16 ?
                    Config.getInstance().suffix().substring(0, 16) : Config.getInstance().suffix();

            final Object scoreboard = this.classMapping.getScoreboard().newInstance();
            final Class<?> scoreboardTeam = this.classMapping.getScoreboardTeam();
            final Constructor<?> teamConstructor = scoreboardTeam.getConstructor(scoreboard.getClass(), String.class);
            this.team = new NMSObject(teamConstructor.newInstance(scoreboard, "000-NPC"));

            if (Version.getInstance().getId() >= Version.v1_13_R1) {
                final Class<?> chatBaseComponentClass = this.classMapping.getIChatBaseComponent();
                final Class<?> craftChatMessageClass = this.classMapping.getCraftChatMessage();

                final Object prefixObject = ((Object[]) new NMSObject(craftChatMessageClass)
                        .getDeclaredMethod("fromString", String.class).invoke(prefix))[0];
                final Object suffixObject = ((Object[]) new NMSObject(craftChatMessageClass)
                        .getDeclaredMethod("fromString", String.class).invoke(suffix))[0];

                this.team.getMethod(this.methodMapping.setPrefix(), chatBaseComponentClass).invoke(prefixObject);
                this.team.getMethod(this.methodMapping.setSuffix(), chatBaseComponentClass).invoke(suffixObject);
            } else {
                this.team.getMethod(this.methodMapping.setPrefix(), String.class).invoke(prefix);
                this.team.getMethod(this.methodMapping.setSuffix(), String.class).invoke(suffix);
            }

            @SuppressWarnings("unchecked")
            final Collection<String> playerNameSet = (Collection<String>) this.team.getMethod(this.methodMapping.getPlayerNameSet()).invoke();
            playerNameSet.add(this.name);

            final Class<?> packet = this.classMapping.getPacketPlayOutScoreboardTeam();
            if (Version.getInstance().hasNewProtocol()) {
                final NMSObject packetObject = new NMSObject(packet);
                final Object scoreboardTeamPacket = packetObject.getDeclaredMethod("a",
                        scoreboardTeam, boolean.class).invoke(this.team.getObject(), false);
                NMSReflection.getInstance().sendPacket(this.player, scoreboardTeamPacket);
            } else {
                final Constructor<?> packetConstructor = packet.getConstructor(scoreboardTeam, int.class);
                NMSReflection.getInstance().sendPacket(this.player, packetConstructor.newInstance(this.team.getObject(), 0));
            }
        } catch (final InstantiationException | IllegalAccessException | NoSuchMethodException
                | InvocationTargetException exception) {
            exception.printStackTrace();
        }
    }

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

            final Class<?> minecraftServerClass = this.classMapping.getMinecraftServer();
            final Class<?> entityPlayerClass = this.classMapping.getEntityPlayer();

            if (Version.getInstance().getId() == Version.v1_19_R1) {
                final Class<?> profilePublicKeyClass = this.classMapping.getProfilePublicKey();
                final Constructor<?> entityPlayerConstructor = entityPlayerClass.getConstructor(
                        minecraftServerClass, worldServer.getClass(), GameProfile.class, profilePublicKeyClass);
                this.entityPlayer = new NMSObject(entityPlayerConstructor.newInstance(
                        minecraftServer, worldServer, this.gameProfile, null));
            } else if (Version.getInstance().hasNewProtocol()) {
                final Constructor<?> entityPlayerConstructor = entityPlayerClass.getConstructor(
                        minecraftServerClass, worldServer.getClass(), GameProfile.class);
                this.entityPlayer = new NMSObject(entityPlayerConstructor.newInstance(
                        minecraftServer, worldServer, this.gameProfile));
            } else {
                final Class<?> worldClass = this.classMapping.getWorld();
                final Class<?> interactManagerClass = this.classMapping.getPlayerInteractManager();
                final Constructor<?> interactManagerConstructor = interactManagerClass.getConstructor(
                        Version.getInstance().getId() <= Version.v1_13_R2 ? worldClass : worldServer.getClass());
                final Object interactManager = interactManagerConstructor.newInstance(worldServer);
                final Constructor<?> entityPlayerConstructor = entityPlayerClass.getConstructor(
                        minecraftServerClass, worldServer.getClass(), GameProfile.class, interactManagerClass);
                this.entityPlayer = new NMSObject(entityPlayerConstructor.newInstance(
                        minecraftServer, worldServer, this.gameProfile, interactManager));
            }

            this.entityId = (int) this.entityPlayer.getMethod(this.methodMapping.getId()).invoke();

            final Class<?> entitySpawnClass = this.classMapping.getPacketPlayOutNamedEntitySpawn();
            final Constructor<?> entitySpawnConstructor = entitySpawnClass.getConstructor(this.classMapping.getEntityHuman());
            final Object packetPlayOutNamedEntitySpawn = entitySpawnConstructor.newInstance(this.entityPlayer.getObject());

            NMSReflection.getInstance().setValue(packetPlayOutNamedEntitySpawn, "a", this.entityId);
            NMSReflection.getInstance().setValue(packetPlayOutNamedEntitySpawn, "b", this.gameProfile.getId());

            NMSReflection.getInstance().setValue(packetPlayOutNamedEntitySpawn, "f", getFixRotation(location.getYaw()));
            NMSReflection.getInstance().setValue(packetPlayOutNamedEntitySpawn, "g", getFixRotation(location.getPitch()));

            if (Version.getInstance().getId() <= Version.v1_8_R3) {
                NMSReflection.getInstance().setValue(packetPlayOutNamedEntitySpawn, "c", getFixLocation(location.getX()));
                NMSReflection.getInstance().setValue(packetPlayOutNamedEntitySpawn, "d", getFixLocation(location.getY()));
                NMSReflection.getInstance().setValue(packetPlayOutNamedEntitySpawn, "e", getFixLocation(location.getZ()));

                NMSReflection.getInstance().setValue(packetPlayOutNamedEntitySpawn, "h", 0);
            } else {
                NMSReflection.getInstance().setValue(packetPlayOutNamedEntitySpawn, "c", location.getX());
                NMSReflection.getInstance().setValue(packetPlayOutNamedEntitySpawn, "d", location.getY());
                NMSReflection.getInstance().setValue(packetPlayOutNamedEntitySpawn, "e", location.getZ());
            }

            final Class<?> entityClass = this.classMapping.getEntity();

            if (Version.getInstance().getId() <= Version.v1_14_R1) {
                final Class<?> watcherClass = this.classMapping.getDataWatcher();
                final Constructor<?> watcherConstructor = watcherClass.getConstructor(entityClass);
                final NMSObject watcher = new NMSObject(watcherConstructor.newInstance((Object) null));

                NMSReflection.getInstance().setValue(packetPlayOutNamedEntitySpawn,
                        Version.getInstance().getId() <= Version.v1_8_R3 ? "i" : "h", watcher.getObject());
            }

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

            final Class<?> animationPacketClass = this.classMapping.getPacketPlayOutAnimation();
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
    public void remove() {
        try {
            if (this.entityId == null || !this.spawned) {
                return;
            }

            this.spawned = false;
            
            final Class<?> destroyPacket = this.classMapping.getPacketPlayOutEntityDestroy();
            final Constructor<?> destroyPacketConstructor = destroyPacket.getConstructor(int[].class);
            NMSReflection.getInstance().sendPacket(this.player, destroyPacketConstructor.newInstance(new int[]{ this.entityId }));

            if (Version.getInstance().hasNewProtocol()) {
                final NMSObject teamPacket = new NMSObject(this.classMapping.getPacketPlayOutScoreboardTeam());
                final Object packet = teamPacket.getDeclaredMethod("a", this.team.getObject().getClass()).invoke(this.team.getObject());
                NMSReflection.getInstance().sendPacket(this.player, packet);
            } else {
                final Class<?> teamPacket = this.classMapping.getPacketPlayOutScoreboardTeam();
                final Constructor<?> teamPacketConstructor = teamPacket.getConstructor(this.team.getObject().getClass(), int.class);
                NMSReflection.getInstance().sendPacket(this.player, teamPacketConstructor.newInstance(this.team.getObject(), 1));
            }
            
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
            final Class<?> playerInfoClass = this.classMapping.getPacketPlayOutPlayerInfo();
            final Class<?> chatBaseComponentClass = this.classMapping.getIChatBaseComponent();
            final Class<?> enumGameModeClass = this.classMapping.getEnumGamemode();
            final Class<?> craftChatMessageClass = this.classMapping.getCraftChatMessage();
            final Class<?> enumPlayerInfoActionClass = this.classMapping.getEnumPlayerInfoAction();
            final Class<?> playerInfoDataClass = this.classMapping.getPlayerInfoData();
            final Class<?> entityPlayerClass = this.classMapping.getEntityPlayer();

            final Object enumGameMode = NMSReflection.getInstance().getEnum(enumGameModeClass, "SURVIVAL");
            final Object craftChatMessage = ((Object[]) new NMSObject(craftChatMessageClass)
                    .getDeclaredMethod("fromString", String.class).invoke(""))[0];
            final Object enumPlayerInfoAction = NMSReflection.getInstance().getEnum(enumPlayerInfoActionClass, action);

            final Object[] entityPlayerArray = (Object[]) Array.newInstance(entityPlayerClass, 1);
            entityPlayerArray[0] = this.entityPlayer.getObject();

            final Constructor<?> playerInfoConstructor = playerInfoClass.getConstructor(enumPlayerInfoActionClass,
                    entityPlayerArray.getClass());
            final Object playerInfoPacket = playerInfoConstructor.newInstance(enumPlayerInfoAction, entityPlayerArray);

            final Object playerInfoData;
            if (Version.getInstance().getId() == Version.v1_19_R1) {
                final Class<?> profilePublicKeyAClass = this.classMapping.getProfilePublicKeyA();
                final Constructor<?> playerInfoDataConstructor = playerInfoDataClass.getConstructor(
                        GameProfile.class, int.class, enumGameModeClass, chatBaseComponentClass, profilePublicKeyAClass);
                playerInfoData = playerInfoDataConstructor.newInstance(
                        this.gameProfile, 1, enumGameMode, craftChatMessage, null);
            } else if (Version.getInstance().hasNewProtocol()) {
                final Constructor<?> playerInfoDataConstructor = playerInfoDataClass.getConstructor(
                        GameProfile.class, int.class, enumGameModeClass, chatBaseComponentClass);
                playerInfoData = playerInfoDataConstructor.newInstance(
                        this.gameProfile, 1, enumGameMode, craftChatMessage);
            } else {
                final Constructor<?> playerInfoDataConstructor = playerInfoDataClass.getConstructor(
                        playerInfoClass, GameProfile.class, int.class, enumGameModeClass, chatBaseComponentClass);
                playerInfoData = playerInfoDataConstructor.newInstance(
                        playerInfoPacket, this.gameProfile, 1, enumGameMode, craftChatMessage);
            }

            final List<Object> players = new ArrayList<>();
            players.add(playerInfoData);
            
            NMSReflection.getInstance().setValue(playerInfoPacket, "b", players);

            NMSReflection.getInstance().sendPacket(this.player, playerInfoPacket);
        } catch (final InstantiationException | IllegalAccessException | NoSuchMethodException
                | InvocationTargetException exception) {
            exception.printStackTrace();
        }
    }

    public void headRotation(final float yaw, final float pitch) {
        try {
            final Class<?> headRotationPacketClass = this.classMapping.getPacketPlayOutEntityHeadRotation();
            final Constructor<?> headRotationPacketConstructor = headRotationPacketClass.getConstructor(
                    this.classMapping.getEntity(), byte.class);
            final Object headRotationPacket = headRotationPacketConstructor.newInstance(
                    this.entityPlayer.getObject(), getFixRotation(yaw));
            
            final Class<?> lookPacketClass = this.classMapping.getPacketPlayOutEntityLook();
            final Constructor<?> lookPacketConstructor = lookPacketClass.getConstructor(int.class, byte.class, byte.class, boolean.class);
            final Object lookPacket = lookPacketConstructor.newInstance(this.entityId, getFixRotation(yaw), getFixRotation(pitch), true);
            
            NMSReflection.getInstance().sendPacket(this.player, lookPacket);
            NMSReflection.getInstance().sendPacket(this.player, headRotationPacket);
        } catch (final InstantiationException | IllegalAccessException | NoSuchMethodException
                | InvocationTargetException exception) {
            exception.printStackTrace();
        }
    }

    public void sneak(final boolean sneaking) {
        try {
            final Version version = Version.getInstance();
            final int versionId = version.getId();
            
            final Class<?> watcherClass = this.classMapping.getDataWatcher();
            final NMSObject watcher = new NMSObject(this.entityPlayer.getMethod(this.methodMapping.getDataWatcher()).invoke());

            if (versionId <= Version.v1_8_R3) {
                watcher.getMethod("watch", int.class, Object.class).invoke(0, (byte) (sneaking ? 2 : 0));
                watcher.getMethod("watch", int.class, Object.class).invoke(6, (float) 20);
                watcher.getMethod("watch", int.class, Object.class).invoke(10, (byte) 127);
            } else {
                final Class<?> watcherObjectClass = this.classMapping.getDataWatcherObject();
                final Class<?> watcherSerializerClass = this.classMapping.getDataWatcherSerializer();
                final Class<?> watcherRegistryClass = this.classMapping.getDataWatcherRegistry();

                final Object watcherRegistryA = watcherRegistryClass.getField("a").get(watcherRegistryClass);
                final Object watcherRegistryC = watcherRegistryClass.getField("c").get(watcherRegistryClass);
                
                final Constructor<?> watcherObjectConstructor = watcherObjectClass.getConstructor(int.class, watcherSerializerClass);
                final Object watcherObject0 = watcherObjectConstructor.newInstance(0, watcherRegistryA);
                final Object watcherObject1 = watcherObjectConstructor.newInstance(version.getHealth(), watcherRegistryC);
                final Object watcherObject2 = watcherObjectConstructor.newInstance(version.getSkinOverlay(), watcherRegistryA);

                watcher.getMethod(this.methodMapping.set(), watcherObjectClass, Object.class).invoke(watcherObject0, (byte) (sneaking ? 2 : 0));
                watcher.getMethod(this.methodMapping.set(), watcherObjectClass, Object.class).invoke(watcherObject1, (float) 20);
                watcher.getMethod(this.methodMapping.set(), watcherObjectClass, Object.class).invoke(watcherObject2, (byte) 127);
                
                if (versionId >= Version.v1_14_R1) {
                    final Object watcherRegistryS = watcherRegistryClass.getField("s").get(watcherRegistryClass);
                    final Object watcherObject3 = watcherObjectConstructor.newInstance(6, watcherRegistryS);

                    final Class<?> entityPoseClass = this.classMapping.getEntityPose();
                    final Object standingPose = NMSReflection.getInstance().getEnum(entityPoseClass, "STANDING");
                    final Object sneakingPose = NMSReflection.getInstance().getEnum(entityPoseClass,
                            versionId == Version.v1_14_R1 ? "SNEAKING" : "CROUCHING");

                    watcher.getMethod(this.methodMapping.set(), watcherObjectClass, Object.class).invoke(watcherObject3, sneaking ? sneakingPose : standingPose);
                }
            }

            final Class<?> packetClass = this.classMapping.getPacketPlayOutEntityMetadata();
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
                .getMethod(this.methodMapping.getProfile()).invoke();
        final Property property = profile.getProperties().get("textures").iterator().next();
        this.value = property.getValue();
        this.signature = property.getSignature();
    }

    private Integer getFixLocation(final double position) {
        try {
            final Method method = this.classMapping.getMathHelper().getDeclaredMethod("floor", double.class);
            method.setAccessible(true);
            return (Integer) method.invoke(null, position * 32.0D);
        } catch (final NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            exception.printStackTrace();
        }

        return 0;
    }

    private byte getFixRotation(final float rotation){
        return (byte) ((int) (rotation * 256.0F / 360.0F));
    }
}
