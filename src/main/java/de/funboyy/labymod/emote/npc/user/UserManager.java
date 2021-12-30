package de.funboyy.labymod.emote.npc.user;

import de.funboyy.labymod.emote.npc.EmoteNPCPlugin;
import de.funboyy.labymod.emote.npc.config.Config;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class UserManager {

    private static UserManager instance;

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    @Getter private final Set<User> users;

    public UserManager() {
        this.users = new HashSet<>();
    }

    public User getUser(final Player player) {
        return this.users.stream().filter(user -> user.getPlayer().equals(player))
                .findAny().orElse(null);
    }

    public User register(final Player player) {
        User user = getUser(player);

        if (user != null) {
            return user;
        }

        try {
            user = new User(player);
            this.users.add(user);

            if (Config.getInstance().debug()) {
                Bukkit.getLogger().info("[" + EmoteNPCPlugin.getInstance().getName() + "] " +
                        "User " + user.getPlayer().getName() + " registered");
            }

            return user;
        } catch (final InvocationTargetException | NoSuchMethodException |
                InstantiationException | IllegalAccessException exception) {
            throw new RuntimeException(exception.getMessage(), exception.getCause());
        }
    }

    public void update(final Player player, final String version) {
        User user = getUser(player);

        if (user == null) {
            user = register(player);
        }

        user.setVersion(version);

        if (Config.getInstance().debug()) {
            Bukkit.getLogger().info("[" + EmoteNPCPlugin.getInstance().getName() + "] " +
                    "Update version of " + user.getPlayer().getName() + " to " + version);
        }
    }

    public void unregister(final Player player) {
        final User user = getUser(player);

        if (user == null) {
            return;
        }

        user.getReader().uninject();
        this.users.remove(user);

        if (Config.getInstance().debug()) {
            Bukkit.getLogger().info("[" + EmoteNPCPlugin.getInstance().getName() + "] " +
                    "User " + user.getPlayer().getName() + " unregistered");
        }
    }
}
