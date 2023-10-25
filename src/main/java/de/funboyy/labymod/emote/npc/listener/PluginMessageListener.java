package de.funboyy.labymod.emote.npc.listener;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.funboyy.labymod.emote.npc.user.UserManager;
import de.funboyy.labymod.emote.npc.utils.Protocol;
import de.funboyy.version.helper.payload.PayloadData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.entity.Player;

public class PluginMessageListener implements org.bukkit.plugin.messaging.PluginMessageListener {

    @Override
    public void onPluginMessageReceived(final String channel, final Player player, final byte[] bytes) {
        if (!channel.equals(Protocol.LABYMOD_CHANNEL_LEGACY) && !channel.equals(Protocol.LABYMOD_CHANNEL)) {
            return;
        }

        handleLabyJoin(player, Unpooled.wrappedBuffer(bytes), channel.equals(Protocol.LABYMOD_CHANNEL_LEGACY));
    }

    private void handleLabyJoin(final Player player, final ByteBuf byteBuf, final boolean legacy) {
        final PayloadData data = new PayloadData(byteBuf);

        if (legacy) {
            final String key = data.readString(Short.MAX_VALUE);

            if (!key.equals("INFO")) {
                return;
            }
        }

        else {
            final int id = data.readInt();

            if (id != 0) {
                return;
            }
        }

        final String json = data.readString(Short.MAX_VALUE);
        @SuppressWarnings("deprecation")
        final JsonElement element = new JsonParser().parse(json).getAsJsonObject();

        if (!element.isJsonObject()) {
            return;
        }

        final JsonObject object = element.getAsJsonObject();

        if (!object.has("version") || !object.get("version").isJsonPrimitive()) {
            return;
        }

        UserManager.getInstance().update(player, object.get("version").getAsString());
    }

}
