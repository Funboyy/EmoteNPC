package de.funboyy.labymod.emote.npc;

import de.funboyy.labymod.emote.npc.command.EmoteCommand;
import de.funboyy.labymod.emote.npc.config.Config;
import de.funboyy.labymod.emote.npc.emote.EmoteManager;
import de.funboyy.labymod.emote.npc.listener.JoinQuitListener;
import de.funboyy.labymod.emote.npc.listener.NPCListener;
import de.funboyy.labymod.emote.npc.listener.PluginMessageListener;
import de.funboyy.labymod.emote.npc.user.User;
import de.funboyy.labymod.emote.npc.user.UserManager;
import de.funboyy.labymod.emote.npc.utils.Protocol;
import de.funboyy.version.helper.Version;
import de.funboyy.version.helper.npc.manager.NPCManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class EmoteNPCPlugin extends JavaPlugin {

    public static EmoteNPCPlugin getInstance() {
        return getPlugin(EmoteNPCPlugin.class);
    }

    private NPCManager manager;

    @Override
    public void onEnable() {
        if (Version.getVersion() == null) {
            getLogger().warning("[" + this.getName() + "] You need to use the spigot versions [1.8 - 1.20.2] to use the plugin!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        this.manager = new NPCManager(this);

        if (Config.getInstance().debug()) {
            getLogger().info("Setting up for version " + Version.getVersion());
        }

        loadConfig();

        EmoteManager.getInstance().loadEmotes();

        getServer().getPluginManager().registerEvents(new JoinQuitListener(), this);
        getServer().getPluginManager().registerEvents(new NPCListener(), this);

        getCommand("emote").setExecutor(new EmoteCommand());

        getServer().getMessenger().registerIncomingPluginChannel(this, Protocol.LABYMOD_CHANNEL_LEGACY, new PluginMessageListener());
        getServer().getMessenger().registerIncomingPluginChannel(this, Protocol.LABYMOD_CHANNEL, new PluginMessageListener());

        Bukkit.getOnlinePlayers().forEach(player -> {
            final User user = UserManager.getInstance().register(player);

            if (user.getNpc().isSpawned()) {
                return;
            }

            if (this.manager.isInRange(user.getPlayer(), user.getNpc())) {
                Bukkit.getScheduler().runTaskLater(this, () -> user.getNpc().spawn(), 10);
            }
        });
    }

    @Override
    public void onDisable() {
        this.manager.disable();

        UserManager.getInstance().getUsers().forEach(user -> UserManager.getInstance().unregister(user));
    }

    private void loadConfig() {
        this.saveDefaultConfig();

        this.getConfig().options().copyDefaults(true);
        this.reloadConfig();
    }

    public void updateConfig() {
        loadConfig();
        updateSetting();
    }

    public void updateSetting() {
        Config.getInstance().reload();

        this.manager.getNpcs().forEach(npc -> {
            npc.getTeam().setPrefix(Config.getInstance().prefix());
            npc.getTeam().setSuffix(Config.getInstance().suffix());

            if (Config.getInstance().nameColor() != null) {
                npc.getTeam().setColor(Config.getInstance().nameColor());
            }

            npc.setSetting(Config.getInstance().setting());
            npc.setLocation(Config.getInstance().getLocation());
            npc.setName(Config.getInstance().name());
            npc.respawn();
        });
    }

}
