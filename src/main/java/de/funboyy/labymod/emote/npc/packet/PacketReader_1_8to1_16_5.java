package de.funboyy.labymod.emote.npc.packet;

import de.funboyy.labymod.emote.npc.EmoteNPCPlugin;
import de.funboyy.labymod.emote.npc.config.Config;
import de.funboyy.labymod.emote.npc.user.User;
import de.funboyy.labymod.emote.npc.user.UserManager;
import de.funboyy.labymod.emote.npc.utils.NMSReflection;
import de.funboyy.labymod.emote.npc.utils.Versions;
import io.netty.channel.*;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PacketReader_1_8to1_16_5 implements IPacketReader {

    private final Player player;
    private final UUID uniqueId;

    public PacketReader_1_8to1_16_5(final Player player) {
        this.player = player;
        this.uniqueId = UUID.randomUUID();
    }

    @Override
    public void inject() {
        final ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {

            @Override
            public void channelRead(final ChannelHandlerContext channelHandlerContext, final Object packet) throws Exception {
                try {
                    if (!packet.getClass().getSimpleName().equals("PacketPlayInUseEntity")) {
                        super.channelRead(channelHandlerContext, packet);
                        return;
                    }

                    if (NMSReflection.getInstance().getValue(packet, "action").toString().equals("INTERACT")) {
                        final User user = UserManager.getInstance().getUser(PacketReader_1_8to1_16_5.this.player);

                        if (user == null || user.getNpc().getEntityId() == null) {
                            super.channelRead(channelHandlerContext, packet);
                            return;
                        }

                        if ((int) NMSReflection.getInstance().getValue(packet, "a") != user.getNpc().getEntityId()) {
                            super.channelRead(channelHandlerContext, packet);
                            return;
                        }

                        if (!user.isPermitted()) {
                            if (user.getDelay() > System.currentTimeMillis()) {
                                super.channelRead(channelHandlerContext, packet);
                                return;
                            }

                            user.setDelay(System.currentTimeMillis() + 100);
                            PacketReader_1_8to1_16_5.this.player.sendMessage(Config.getInstance().getLabyMod());
                            super.channelRead(channelHandlerContext, packet);
                            return;
                        }

                        Bukkit.getScheduler().runTaskLater(EmoteNPCPlugin.getInstance(), () -> {
                            if (PacketReader_1_8to1_16_5.this.player.getOpenInventory().getTitle().equals(Config.getInstance().getInventory())) {
                                return;
                            }

                            UserManager.getInstance().getUser(PacketReader_1_8to1_16_5.this.player).openInventory(1);
                            PacketReader_1_8to1_16_5.this.player.playSound(PacketReader_1_8to1_16_5.this.player.getLocation(),
                                    Versions.getInstance().getSound(), 1f, 1f);
                        }, 1);

                        super.channelRead(channelHandlerContext, packet);
                        return;
                    }

                    if (NMSReflection.getInstance().getValue(packet, "action").toString().equals("ATTACK")) {
                        final User user = UserManager.getInstance().getUser(PacketReader_1_8to1_16_5.this.player);

                        if (user == null || user.getNpc().getEntityId() == null) {
                            super.channelRead(channelHandlerContext, packet);
                            return;
                        }

                        if ((int) NMSReflection.getInstance().getValue(packet, "a") != user.getNpc().getEntityId()) {
                            super.channelRead(channelHandlerContext, packet);
                            return;
                        }

                        user.playEmote(149);
                        super.channelRead(channelHandlerContext, packet);
                        return;
                    }

                    super.channelRead(channelHandlerContext, packet);
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
        pipeline.addBefore("packet_handler", "EmoteNPC_1_8to1_16_5-" + this.uniqueId.toString(), channelDuplexHandler);

        if (Config.getInstance().debug()) {
            Bukkit.getLogger().info("[" + EmoteNPCPlugin.getInstance().getName() + "] " +
                    "Enabled PacketHandler for " + this.player.getName());
        }
    }

    @Override
    public void uninject() {
        final Channel channel = NMSReflection.getInstance().getChannel(this.player);
        channel.eventLoop().submit(() -> {
            channel.pipeline().remove("EmoteNPC_1_8to1_16_5-" + this.uniqueId.toString());
            return null;
        });

        if (Config.getInstance().debug()) {
            Bukkit.getLogger().info("[" + EmoteNPCPlugin.getInstance().getName() + "] " +
                    "Disabled PacketHandler for " + this.player.getName());
        }
    }
}