package de.funboyy.labymod.emote.npc.packet;

import de.funboyy.labymod.emote.npc.EmoteNPCPlugin;
import de.funboyy.labymod.emote.npc.config.Config;
import de.funboyy.labymod.emote.npc.user.User;
import de.funboyy.labymod.emote.npc.user.UserManager;
import de.funboyy.labymod.emote.npc.packet.nms.NMSReflection;
import de.funboyy.labymod.emote.npc.utils.Version;
import io.netty.channel.*;
import java.lang.reflect.Method;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PacketReader {

    private final Player player;
    private final UUID uniqueId;

    public PacketReader(final Player player) {
        this.player = player;
        this.uniqueId = UUID.randomUUID();
    }

    public void inject() {
        final ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {

            @Override
            public void channelRead(final ChannelHandlerContext channelHandlerContext, final Object packet) throws Exception {
                try {
                    if (!packet.getClass().getSimpleName().equals("PacketPlayInUseEntity")) {
                        super.channelRead(channelHandlerContext, packet);
                        return;
                    }

                    final int entityId = (int) NMSReflection.getInstance().getValue(packet, "a");
                    final User user = UserManager.getInstance().getUser(PacketReader.this.player);

                    if (user == null || user.getNpc().getEntityId() == null) {
                        super.channelRead(channelHandlerContext, packet);
                        return;
                    }

                    if (entityId != user.getNpc().getEntityId()) {
                        super.channelRead(channelHandlerContext, packet);
                        return;
                    }

                    if (Version.getInstance().hasNewProtocol()) {
                        final Object actionObject = NMSReflection.getInstance().getValue(packet, "b");
                        final Method actionMethod = actionObject.getClass().getMethod("a");
                        actionMethod.setAccessible(true);

                        final Object action = actionMethod.invoke(actionObject);
                        final Class<?> enumEntityUseAction = EmoteNPCPlugin.getInstance().getClassManager().getEnumEntityUseAction();

                        if (NMSReflection.getInstance().getEnum(enumEntityUseAction, "INTERACT") == action) {
                            interact(user);
                            super.channelRead(channelHandlerContext, packet);
                            return;
                        }

                        if (NMSReflection.getInstance().getEnum(enumEntityUseAction, "ATTACK") == action) {
                            user.playEmote(149);
                            super.channelRead(channelHandlerContext, packet);
                            return;
                        }
                    } else {
                        final Object enumEntityUseAction = NMSReflection.getInstance().getValue(packet, "action");

                        if (enumEntityUseAction.toString().equals("INTERACT")) {
                            interact(user);
                            super.channelRead(channelHandlerContext, packet);
                            return;
                        }

                        if (enumEntityUseAction.toString().equals("ATTACK")) {
                            user.playEmote(149);
                            super.channelRead(channelHandlerContext, packet);
                            return;
                        }
                    }
                } catch (final Exception exception) {
                    exception.printStackTrace();
                }

                super.channelRead(channelHandlerContext, packet);
            }

            @Override
            public void write(final ChannelHandlerContext channelHandlerContext, final Object packet, final ChannelPromise channelPromise) throws Exception {
                super.write(channelHandlerContext, packet, channelPromise);
            }

        };
        final ChannelPipeline pipeline = NMSReflection.getInstance().getChannel(this.player).pipeline();
        pipeline.addBefore("packet_handler", "EmoteNPC-" + this.uniqueId.toString(), channelDuplexHandler);

        if (Config.getInstance().debug()) {
            Bukkit.getLogger().info("[" + EmoteNPCPlugin.getInstance().getName() + "] " +
                    "Enabled PacketHandler for " + this.player.getName());
        }
    }

    private void interact(final User user) {
        if (!user.isPermitted()) {
            if (user.getDelay() > System.currentTimeMillis()) {
                return;
            }

            user.setDelay(System.currentTimeMillis() + 100);
            this.player.sendMessage(Config.getInstance().getLabyMod());
            return;
        }

        Bukkit.getScheduler().runTaskLater(EmoteNPCPlugin.getInstance(), () -> {
            if (this.player.getOpenInventory().getTitle().equals(Config.getInstance().getInventory())) {
                return;
            }

            UserManager.getInstance().getUser(this.player).openInventory(1);
            this.player.playSound(this.player.getLocation(),
                    Version.getInstance().getSound(), 1f, 1f);
        }, 1);
    }

    public void uninject() {
        final Channel channel = NMSReflection.getInstance().getChannel(this.player);
        channel.eventLoop().submit(() -> {
            channel.pipeline().remove("EmoteNPC-" + this.uniqueId.toString());
            return null;
        });

        if (Config.getInstance().debug()) {
            Bukkit.getLogger().info("[" + EmoteNPCPlugin.getInstance().getName() + "] " +
                    "Disabled PacketHandler for " + this.player.getName());
        }
    }
}
