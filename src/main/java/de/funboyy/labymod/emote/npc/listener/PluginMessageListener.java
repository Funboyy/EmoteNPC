package de.funboyy.labymod.emote.npc.listener;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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

        this.handleLabyJoin(player, Unpooled.wrappedBuffer(bytes), channel.equals(Protocol.LABYMOD_CHANNEL_LEGACY));
    }

    private void handleLabyJoin(final Player player, final ByteBuf byteBuf, final boolean legacy) {
        final PayloadData data = new PayloadData(byteBuf);
        final String version;

        if (legacy) {
            final String key = data.readString(Short.MAX_VALUE);

            if (!key.equals("INFO")) {
                return;
            }

            final JsonElement json = Protocol.GSON.fromJson(data.readString(Short.MAX_VALUE), JsonElement.class);

            if (!json.isJsonObject()) {
                return;
            }

            final JsonObject object = json.getAsJsonObject();

            if (!object.has("version")) {
                return;
            }

            final JsonElement element = object.get("version");

            if (!element.isJsonPrimitive()) {
                return;
            }

            version = element.getAsString();
        }

        else {
            final int id = data.readVarInt();

            if (id != 0) {
                return;
            }

            version = data.readString(Short.MAX_VALUE);
        }

        UserManager.getInstance().update(player, version);
    }

}
