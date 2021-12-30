package de.funboyy.labymod.emote.npc.listener;

import de.funboyy.labymod.emote.npc.user.UserManager;
import de.funboyy.labymod.emote.npc.utils.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class PluginMessageListener implements org.bukkit.plugin.messaging.PluginMessageListener {

    @Override
    public void onPluginMessageReceived(final String channel, final Player player, final byte[] bytes) {
        if (!channel.equals("labymod3:main")) {
            return;
        }

        final ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
        final String key = ProtocolUtils.readString(byteBuf, Short.MAX_VALUE);
        final String json = ProtocolUtils.readString(byteBuf, Short.MAX_VALUE);

        if (!key.equals("INFO")) {
            return;
        }

        try {
            final JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
            final String version = (String) jsonObject.get("version");

            if (version == null) {
                return;
            }

            UserManager.getInstance().update(player, version);
        } catch (final ParseException exception) {
            exception.printStackTrace();
        }
    }
}
