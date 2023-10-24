package de.funboyy.labymod.emote.npc.user;

import de.funboyy.labymod.emote.npc.EmoteNPCPlugin;
import de.funboyy.labymod.emote.npc.config.Config;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class UserManager {

    private static UserManager instance;

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    private final Set<User> users;

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

        user = new User(player);
        this.users.add(user);

        if (Config.getInstance().debug()) {
            EmoteNPCPlugin.getInstance().getLogger().info("User " + user.getPlayer().getName() + " registered");
        }

        return user;
    }

    public void update(final Player player, final String version) {
        User user = getUser(player);

        if (user == null) {
            user = register(player);
        }

        user.setVersion(version);
        user.setLegacy(version.startsWith("3"));

        if (Config.getInstance().debug()) {
            EmoteNPCPlugin.getInstance().getLogger().info("Update version of " + user.getPlayer().getName() + " to " + version);
        }
    }

    public void unregister(final User user) {
        this.users.remove(user);

        if (Config.getInstance().debug()) {
            EmoteNPCPlugin.getInstance().getLogger().info("User " + user.getPlayer().getName() + " unregistered");
        }
    }
}
