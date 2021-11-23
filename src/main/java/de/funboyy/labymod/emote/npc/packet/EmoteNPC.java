package de.funboyy.labymod.emote.npc.packet;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import de.funboyy.labymod.emote.npc.EmoteNPCPlugin;
import de.funboyy.labymod.emote.npc.config.Config;
import de.funboyy.labymod.emote.npc.utils.NMSObject;
import de.funboyy.labymod.emote.npc.utils.NMSReflection;
import de.funboyy.labymod.emote.npc.utils.Versions;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class EmoteNPC {

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
    }

    private void setName() {
        try {
            final String prefix = Config.getInstance().prefix().length() > 16 ?
                    Config.getInstance().prefix().substring(0, 16) : Config.getInstance().prefix();
            this.name = Config.getInstance().name().length() > 16 ?
                    Config.getInstance().name().substring(0, 16) : Config.getInstance().name();
            final String suffix = Config.getInstance().suffix().length() > 16 ?
                    Config.getInstance().suffix().substring(0, 16) : Config.getInstance().suffix();

            final Object scoreboard = NMSReflection.getInstance().getClass("Scoreboard").newInstance();
            final Class<?> scoreboardTeam = NMSReflection.getInstance().getClass("ScoreboardTeam");
            final Constructor<?> teamConstructor = scoreboardTeam.getConstructor(scoreboard.getClass(), String.class);
            this.team = new NMSObject(teamConstructor.newInstance(scoreboard, "000-NPC"));

            if (Versions.getInstance().getVersionId() <= 1121) {
                this.team.getMethod("setPrefix", String.class).invoke(prefix);
                this.team.getMethod("setSuffix", String.class).invoke(suffix);
            } else {
                final Class<?> chatBaseComponentClass = NMSReflection.getInstance().getClass("IChatBaseComponent");
                final Class<?> craftChatMessageClass = NMSReflection.getInstance().getBukkitClass("util.CraftChatMessage");

                final Object prefixObject = ((Object[]) new NMSObject(craftChatMessageClass)
                        .getDeclaredMethod("fromString", String.class).invoke(prefix))[0];
                final Object suffixObject = ((Object[]) new NMSObject(craftChatMessageClass)
                        .getDeclaredMethod("fromString", String.class).invoke(suffix))[0];

                this.team.getMethod("setPrefix", chatBaseComponentClass).invoke(prefixObject);
                this.team.getMethod("setSuffix", chatBaseComponentClass).invoke(suffixObject);
            }

            @SuppressWarnings("unchecked")
            final Collection<String> playerNameSet = (Collection<String>) this.team.getMethod("getPlayerNameSet").invoke();
            playerNameSet.add(this.name);
            final Class<?> packet = NMSReflection.getInstance().getClass("PacketPlayOutScoreboardTeam");
            final Constructor<?> packetConstructor = packet.getConstructor(scoreboardTeam, int.class);
            NMSReflection.getInstance().sendPacket(this.player, packetConstructor.newInstance(this.team.getObject(), 0));
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

            final Object packetPlayOutNamedEntitySpawn = NMSReflection.getInstance()
                    .getClass("PacketPlayOutNamedEntitySpawn").newInstance();

            if (location.getWorld() == null) {
                Bukkit.getLogger().warning("[" + EmoteNPCPlugin.getInstance().getName() + "] " +
                        "Can not spawn NPC because the specified world was not found");
                return;
            }

            final Object minecraftServer = new NMSObject(Bukkit.getServer()).getMethod("getServer").invoke();
            final Object worldServer = new NMSObject(location.getWorld()).getMethod("getHandle").invoke();

            final Class<?> worldClass = NMSReflection.getInstance().getClass("World");
            final Class<?> interactManagerClass = NMSReflection.getInstance().getClass("PlayerInteractManager");
            final Constructor<?> interactManagerConstructor = interactManagerClass.getConstructor(
                    Versions.getInstance().getVersionId() <= 1132 ? worldClass : worldServer.getClass());
            final Object interactManager = interactManagerConstructor.newInstance(worldServer);

            final Class<?> minecraftServerClass = NMSReflection.getInstance().getClass("MinecraftServer");
            final Class<?> entityPlayerClass = NMSReflection.getInstance().getClass("EntityPlayer");
            final Constructor<?> entityPlayerConstructor = entityPlayerClass.getConstructor(
                    minecraftServerClass, worldServer.getClass(), GameProfile.class, interactManagerClass);
            this.entityPlayer = new NMSObject(entityPlayerConstructor.newInstance(
                    minecraftServer, worldServer, this.gameProfile, interactManager));

            this.entityId = (int) this.entityPlayer.getMethod("getId").invoke();

            NMSReflection.getInstance().setValue(packetPlayOutNamedEntitySpawn, "a", this.entityId);
            NMSReflection.getInstance().setValue(packetPlayOutNamedEntitySpawn, "b", this.gameProfile.getId());

            NMSReflection.getInstance().setValue(packetPlayOutNamedEntitySpawn, "f", getFixRotation(location.getYaw()));
            NMSReflection.getInstance().setValue(packetPlayOutNamedEntitySpawn, "g", getFixRotation(location.getPitch()));

            final Class<?> entityClass = NMSReflection.getInstance().getClass("Entity");
            final Class<?> watcherClass = NMSReflection.getInstance().getClass("DataWatcher");
            final Constructor<?> watcherConstructor = watcherClass.getConstructor(entityClass);
            final NMSObject watcher = new NMSObject(watcherConstructor.newInstance((Object) null));

            if (Versions.getInstance().isMinecraft18()) {
                NMSReflection.getInstance().setValue(packetPlayOutNamedEntitySpawn, "c", getFixLocation(location.getX()));
                NMSReflection.getInstance().setValue(packetPlayOutNamedEntitySpawn, "d", getFixLocation(location.getY()));
                NMSReflection.getInstance().setValue(packetPlayOutNamedEntitySpawn, "e", getFixLocation(location.getZ()));
                NMSReflection.getInstance().setValue(packetPlayOutNamedEntitySpawn, "h", 0);
            }

            if (!Versions.getInstance().isMinecraft18()) {
                NMSReflection.getInstance().setValue(packetPlayOutNamedEntitySpawn, "c", location.getX());
                NMSReflection.getInstance().setValue(packetPlayOutNamedEntitySpawn, "d", location.getY());
                NMSReflection.getInstance().setValue(packetPlayOutNamedEntitySpawn, "e", location.getZ());
            }

            if (Versions.getInstance().getVersionId() <= 1141) {
                NMSReflection.getInstance().setValue(packetPlayOutNamedEntitySpawn,
                        Versions.getInstance().isMinecraft18() ? "i" : "h", watcher.getObject());
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

            final Class<?> animationPacketClass = NMSReflection.getInstance().getClass("PacketPlayOutAnimation");
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
            
            final Class<?> destroyPacket = NMSReflection.getInstance().getClass("PacketPlayOutEntityDestroy");
            final Constructor<?> destroyPacketConstructor = destroyPacket.getConstructor(int[].class);
            NMSReflection.getInstance().sendPacket(this.player, destroyPacketConstructor.newInstance(new int[]{ this.entityId }));

            final Class<?> teamPacket = NMSReflection.getInstance().getClass("PacketPlayOutScoreboardTeam");
            final Constructor<?> teamPacketConstructor = teamPacket.getConstructor(this.team.getObject().getClass(), int.class);
            NMSReflection.getInstance().sendPacket(this.player, teamPacketConstructor.newInstance(this.team.getObject(), 1));

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
            final Object packetPlayOutPlayerInfo = NMSReflection.getInstance().getClass("PacketPlayOutPlayerInfo").newInstance();

            final Class<?> chatBaseComponentClass = NMSReflection.getInstance().getClass("IChatBaseComponent");
            final Class<?> enumGameModeClass = NMSReflection.getInstance().getClass(Versions.getInstance().getVersionId()
                    != 181 && (Versions.getInstance().isMinecraft18() || Versions.getInstance().isMinecraft19())
                    ? "WorldSettings$EnumGamemode" : "EnumGamemode");
            final Class<?> craftChatMessageClass = NMSReflection.getInstance().getBukkitClass("util.CraftChatMessage");
            final Class<?> enumPlayerInfoActionClass = NMSReflection.getInstance().getClass(Versions.getInstance().getVersionId()
                    == 181 ? "EnumPlayerInfoAction" : "PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
            final Class<?> playerInfoDataClass = NMSReflection.getInstance().getClass(Versions.getInstance().getVersionId()
                    == 181 ? "PlayerInfoData" : "PacketPlayOutPlayerInfo$PlayerInfoData");

            final Object enumGameMode = new NMSObject(enumGameModeClass)
                    .getDeclaredMethod("valueOf", String.class).invoke("NOT_SET");
            final Object craftChatMessage = ((Object[]) new NMSObject(craftChatMessageClass)
                    .getDeclaredMethod("fromString", String.class).invoke(""))[0];
            final Object enumPlayerInfoAction = new NMSObject(enumPlayerInfoActionClass)
                    .getDeclaredMethod("valueOf", String.class).invoke(action);

            final Constructor<?> playerInfoDataConstructor = playerInfoDataClass.getConstructor(
                    packetPlayOutPlayerInfo.getClass(), GameProfile.class, int.class, enumGameModeClass, chatBaseComponentClass);
            final Object playerInfoData = playerInfoDataConstructor.newInstance(
                    packetPlayOutPlayerInfo, this.gameProfile, 1, enumGameMode, craftChatMessage);

            final List<Object> players = (List<Object>) NMSReflection.getInstance().getValue(packetPlayOutPlayerInfo, "b");

            players.add(playerInfoData);

            NMSReflection.getInstance().setValue(packetPlayOutPlayerInfo, "a", enumPlayerInfoAction);
            NMSReflection.getInstance().setValue(packetPlayOutPlayerInfo, "b", players);

            NMSReflection.getInstance().sendPacket(this.player, packetPlayOutPlayerInfo);
        } catch (final InstantiationException | IllegalAccessException | NoSuchMethodException
                | InvocationTargetException exception) {
            exception.printStackTrace();
        }
    }

    public void headRotation(final float yaw, final float pitch) {
        try {
            final Object headRotationPacket = NMSReflection.getInstance().getClass("PacketPlayOutEntityHeadRotation").newInstance();

            final Class<?> lookPacketClass = NMSReflection.getInstance().getClass(Versions.getInstance().getVersionId()
                    == 181 ? "PacketPlayOutEntityLook" : "PacketPlayOutEntity$PacketPlayOutEntityLook");
            final Constructor<?> lookPacketConstructor = lookPacketClass.getConstructor(int.class, byte.class, byte.class, boolean.class);
            final Object lookPacket = lookPacketConstructor.newInstance(this.entityId, getFixRotation(yaw), getFixRotation(pitch), true);

            NMSReflection.getInstance().setValue(headRotationPacket, "a", this.entityId);
            NMSReflection.getInstance().setValue(headRotationPacket, "b", getFixRotation(yaw));

            NMSReflection.getInstance().sendPacket(this.player, lookPacket);
            NMSReflection.getInstance().sendPacket(this.player, headRotationPacket);
        } catch (final InstantiationException | IllegalAccessException | NoSuchMethodException
                | InvocationTargetException exception) {
            exception.printStackTrace();
        }
    }

    public void sneak(final boolean sneaking) {
        try {
            final Class<?> watcherClass = NMSReflection.getInstance().getClass("DataWatcher");
            final NMSObject watcher = new NMSObject(this.entityPlayer.getMethod("getDataWatcher").invoke());



            /*final Object playerWatcher = new NMSObject(NMSReflection.getInstance()
                    .getEntityPlayer(this.player)).getMethod("getDataWatcher").invoke();

            final Map<Integer, Object> entries = (Map<Integer, Object>) NMSReflection.getInstance().getValue(playerWatcher, "entries");
            final Map<Integer, Object> entries1 = (Map<Integer, Object>) NMSReflection.getInstance().getValue(watcher.getObject(), "entries");

            for (int i = 0; i < 254; i++) {
                if (entries.containsKey(i)) {
                    final Object item = entries.get(i);
                    final Object itemObject = NMSReflection.getInstance().getValue(item, "b");
                    System.out.println("Player (#" + i + "): " + itemObject + " " + itemObject.getClass());
                }

                if (entries1.containsKey(i)) {
                    final Object item = entries.get(i);
                    final Object itemObject = NMSReflection.getInstance().getValue(item, "b");
                    System.out.println("NPC (#" + i + "): " + itemObject + " " + itemObject.getClass());
                }
            }*/




            if (Versions.getInstance().isMinecraft18()) {
                watcher.getMethod("watch", int.class, Object.class).invoke(0, (byte) (sneaking ? 2 : 0));
                watcher.getMethod("watch", int.class, Object.class).invoke(6, (float) 20);
                watcher.getMethod("watch", int.class, Object.class).invoke(10, (byte) 127);
            }

            if (!Versions.getInstance().isMinecraft18()) {
                final Class<?> watcherObjectClass = NMSReflection.getInstance().getClass("DataWatcherObject");
                final Class<?> watcherSerializerClass = NMSReflection.getInstance().getClass("DataWatcherSerializer");
                final Class<?> watcherRegistryClass = NMSReflection.getInstance().getClass("DataWatcherRegistry");

                final Object watcherRegistryA = watcherRegistryClass.getField("a").get(watcherRegistryClass);
                final Object watcherRegistryC = watcherRegistryClass.getField("c").get(watcherRegistryClass);

                final Constructor<?> watcherObjectConstructor = watcherObjectClass.getConstructor(int.class, watcherSerializerClass);
                final Object watcherObject0 = watcherObjectConstructor.newInstance(0, watcherRegistryA);
                final Object watcherObject1 = watcherObjectConstructor.newInstance(
                        Versions.getInstance().getHealth(), watcherRegistryC);
                final Object watcherObject2 = watcherObjectConstructor.newInstance(
                        Versions.getInstance().getSkinOverlay(), watcherRegistryA);

                watcher.getMethod("set", watcherObjectClass, Object.class).invoke(watcherObject0, (byte) (sneaking ? 2 : 0));
                watcher.getMethod("set", watcherObjectClass, Object.class).invoke(watcherObject1, (float) 20);
                watcher.getMethod("set", watcherObjectClass, Object.class).invoke(watcherObject2, (byte) 127);

                if (Versions.getInstance().getVersionId() >= 1141) {
                    final Object watcherRegistryS = watcherRegistryClass.getField("s").get(watcherRegistryClass);
                    final Object watcherObject3 = watcherObjectConstructor.newInstance(6, watcherRegistryS);

                    final Class<?> entityPoseClass = NMSReflection.getInstance().getClass("EntityPose");
                    final Object standingPose = new NMSObject(entityPoseClass)
                            .getDeclaredMethod("valueOf", String.class).invoke("STANDING");
                    final Object sneakingPose = new NMSObject(entityPoseClass)
                            .getDeclaredMethod("valueOf", String.class).invoke(
                                    Versions.getInstance().getVersionId() == 1141 ? "SNEAKING" : "CROUCHING");

                    watcher.getMethod("set", watcherObjectClass, Object.class).invoke(watcherObject3, sneaking ? sneakingPose : standingPose);
                }
            }

            final Class<?> packetClass = NMSReflection.getInstance().getClass("PacketPlayOutEntityMetadata");
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
                .getMethod("getProfile").invoke();
        final Property property = profile.getProperties().get("textures").iterator().next();
        this.value = property.getValue();
        this.signature = property.getSignature();
    }

    private Integer getFixLocation(final double position) {
        try {
            final Method method = NMSReflection.getInstance().getClass("MathHelper").getDeclaredMethod("floor", double.class);
            method.setAccessible(true);
            return (Integer) method.invoke(null, position * 32.0D);
        } catch (final NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            exception.printStackTrace();
        }

        return null;
    }

    private byte getFixRotation(final float rotation){
        return (byte) ((int) (rotation * 256.0F / 360.0F));
    }
}
