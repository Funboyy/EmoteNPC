package de.funboyy.labymod.emote.npc;

import de.funboyy.labymod.emote.npc.command.EmoteCommand;
import de.funboyy.labymod.emote.npc.config.Config;
import de.funboyy.labymod.emote.npc.emote.EmoteManager;
import de.funboyy.labymod.emote.npc.listener.InventoryListener;
import de.funboyy.labymod.emote.npc.listener.JoinQuitListener;
import de.funboyy.labymod.emote.npc.listener.MovementListener;
import de.funboyy.labymod.emote.npc.listener.PluginMessageListener;
import de.funboyy.labymod.emote.npc.user.User;
import de.funboyy.labymod.emote.npc.user.UserManager;
import de.funboyy.labymod.emote.npc.utils.NMSObject;
import de.funboyy.labymod.emote.npc.utils.NMSReflection;
import de.funboyy.labymod.emote.npc.utils.Versions;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class EmoteNPCPlugin extends JavaPlugin {

    public static EmoteNPCPlugin getInstance() {
        return getPlugin(EmoteNPCPlugin.class);
    }

    @Override
    public void onEnable() {
        try {
            Versions.init(getServer().getClass().getPackage().getName().split("\\.")[3]);

            if (Config.getInstance().debug()) {
                Bukkit.getLogger().info("[" + EmoteNPCPlugin.getInstance().getName() + "] " +
                        "Setting up for version " + Versions.getInstance().getVersion());
            }
        } catch (final RuntimeException ignored) {
            getLogger().warning("[" + this.getName() + "] You need to use the spigot versions [1.8 - 1.16.5] to use the plugin!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        loadConfig();

        EmoteManager.getInstance().loadEmotes();

        getServer().getPluginManager().registerEvents(new JoinQuitListener(), this);
        getServer().getPluginManager().registerEvents(new MovementListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);

        getCommand("emote").setExecutor(new EmoteCommand());

        getServer().getMessenger().registerIncomingPluginChannel(this, "labymod3:main", new PluginMessageListener());

        for (final Player player : Bukkit.getOnlinePlayers()) {
            UserManager.getInstance().register(player);
        }

        for (final User user : UserManager.getInstance().getUsers()) {
            try {
                final Object minecraftServer = new NMSObject(NMSReflection.getInstance().getClass("MinecraftServer"))
                        .getDeclaredMethod("getServer").invoke();
                final String serverModName = (String) new NMSObject(minecraftServer).getMethod("getServerModName").invoke();

                final Constructor<?> serializerConstructor = NMSReflection.getInstance()
                        .getClass("PacketDataSerializer").getConstructor(ByteBuf.class);
                final NMSObject serializerNMS = new NMSObject(serializerConstructor.newInstance(Unpooled.buffer()));
                final Object serializer = serializerNMS.getMethod("a", String.class).invoke(serverModName);

                final Object key;

                if (Versions.getInstance().getVersionId() <= 1121) {
                    key = "MC|Brand";
                } else {
                    final Constructor<?> keyConstructor = NMSReflection.getInstance()
                            .getClass("MinecraftKey").getConstructor(String.class, String.class);
                    key = keyConstructor.newInstance("minecraft", "brand");
                }

                final Constructor<?> packetConstructor = NMSReflection.getInstance()
                        .getClass("PacketPlayOutCustomPayload").getConstructor(key.getClass(), serializer.getClass());
                final Object packet = packetConstructor.newInstance(key, serializer);

                NMSReflection.getInstance().sendPacket(user.getPlayer(), packet);
            } catch (final InstantiationException | IllegalAccessException | InvocationTargetException
                    | NoSuchMethodException exception) {
                exception.printStackTrace();
            }

            if (user.getNpc().isSpawned()) {
                continue;
            }

            if (!user.isNearNPC()) {
                continue;
            }

            user.getNpc().spawn();
        }
    }

    @Override
    public void onDisable() {
        for (final User user : UserManager.getInstance().getUsers()) {
            user.getReader().uninject();

            if (!user.getNpc().isSpawned()) {
                continue;
            }

            user.getNpc().remove();
        }
    }

    private void loadConfig() {
        this.saveDefaultConfig();

        this.getConfig().options().copyDefaults(true);
        this.reloadConfig();
    }

    public void updateConfig() {
        loadConfig();
        respawnNPC();
    }

    public void respawnNPC() {
        for (final User user : UserManager.getInstance().getUsers()) {
            if (user.getNpc().isSpawned()) {
                user.getNpc().remove();
            }

            if (!user.isNearNPC()) {
                continue;
            }

            user.getNpc().spawn();
        }
    }
}
