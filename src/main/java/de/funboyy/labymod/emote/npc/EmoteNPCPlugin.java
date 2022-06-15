package de.funboyy.labymod.emote.npc;

import de.funboyy.labymod.emote.npc.command.EmoteCommand;
import de.funboyy.labymod.emote.npc.config.Config;
import de.funboyy.labymod.emote.npc.emote.EmoteManager;
import de.funboyy.labymod.emote.npc.listener.InventoryListener;
import de.funboyy.labymod.emote.npc.listener.JoinQuitListener;
import de.funboyy.labymod.emote.npc.listener.MovementListener;
import de.funboyy.labymod.emote.npc.listener.PluginMessageListener;
import de.funboyy.labymod.emote.npc.packet.Protocol;
import de.funboyy.labymod.emote.npc.packet.mapping.ClassMapping;
import de.funboyy.labymod.emote.npc.packet.mapping.MethodMapping;
import de.funboyy.labymod.emote.npc.user.User;
import de.funboyy.labymod.emote.npc.user.UserManager;
import de.funboyy.labymod.emote.npc.utils.Version;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class EmoteNPCPlugin extends JavaPlugin {

    public static EmoteNPCPlugin getInstance() {
        return getPlugin(EmoteNPCPlugin.class);
    }

    @Getter private Protocol protocol;
    @Getter private ClassMapping classManager;
    @Getter private MethodMapping methodManager;

    @Override
    public void onEnable() {
        try {
            Version.init(getServer().getClass().getPackage().getName().split("\\.")[3]);

            this.classManager = new ClassMapping();
            this.methodManager = new MethodMapping();
            this.protocol = new Protocol();

            if (Config.getInstance().debug()) {
                Bukkit.getLogger().info("[" + EmoteNPCPlugin.getInstance().getName() + "] " +
                        "Setting up for version " + Version.getInstance().getVersion());
            }
        } catch (final RuntimeException ignored) {
            getLogger().warning("[" + this.getName() + "] You need to use the spigot versions [1.8 - 1.19] to use the plugin!");
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
            this.protocol.sendBrand(user.getPlayer());

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
