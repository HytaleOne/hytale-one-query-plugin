package dev.hytaleone.query;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.common.util.java.ManifestUtil;
import com.hypixel.hytale.protocol.ProtocolSettings;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.io.ServerManager;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * Query protocol constants and packet building utilities.
 */
public final class HytaleOneQueryProtocol {

    public static final byte[] REQUEST_MAGIC = "HYQUERY\0".getBytes(StandardCharsets.US_ASCII);
    public static final byte[] RESPONSE_MAGIC = "HYREPLY\0".getBytes(StandardCharsets.US_ASCII);

    public static final byte TYPE_BASIC = 0x00;
    public static final byte TYPE_FULL = 0x01;

    public static final int MIN_REQUEST_SIZE = REQUEST_MAGIC.length + 1; // magic + type

    private HytaleOneQueryProtocol() {
    }

    /**
     * Check if the buffer starts with the request magic bytes.
     */
    public static boolean isQueryRequest(@Nonnull ByteBuf buf) {
        if (buf.readableBytes() < MIN_REQUEST_SIZE) {
            return false;
        }
        for (int i = 0; i < REQUEST_MAGIC.length; i++) {
            if (buf.getByte(i) != REQUEST_MAGIC[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the query type from the request buffer.
     */
    public static byte getQueryType(@Nonnull ByteBuf buf) {
        return buf.getByte(REQUEST_MAGIC.length);
    }

    /**
     * Build a basic query response containing server info only.
     */
    @Nonnull
    public static ByteBuf buildBasicResponse(@Nonnull ByteBufAllocator alloc) {
        ByteBuf buf = alloc.buffer();

        // Magic header
        buf.writeBytes(RESPONSE_MAGIC);

        // Response type
        buf.writeByte(TYPE_BASIC);

        // Server name
        writeString(buf, HytaleServer.get().getConfig().getServerName());

        // MOTD
        writeString(buf, HytaleServer.get().getConfig().getMotd());

        // Current players
        buf.writeIntLE(Universe.get().getPlayerCount());

        // Max players
        buf.writeIntLE(Math.max(HytaleServer.get().getConfig().getMaxPlayers(), 0));

        // Host port
        buf.writeShortLE(getHostPort());

        // Server version
        writeString(buf, getVersion());

        // Protocol version
        buf.writeIntLE(ProtocolSettings.PROTOCOL_VERSION);

        // Protocol hash
        writeString(buf, ProtocolSettings.PROTOCOL_HASH);

        return buf;
    }

    /**
     * Build a full query response containing server info, player list, and plugins.
     */
    @Nonnull
    public static ByteBuf buildFullResponse(@Nonnull ByteBufAllocator alloc) {
        ByteBuf buf = alloc.buffer();

        // Magic header
        buf.writeBytes(RESPONSE_MAGIC);

        // Response type
        buf.writeByte(TYPE_FULL);

        // Server name
        writeString(buf, HytaleServer.get().getConfig().getServerName());

        // MOTD
        writeString(buf, HytaleServer.get().getConfig().getMotd());

        // Current players
        List<PlayerRef> players = Universe.get().getPlayers();
        buf.writeIntLE(players.size());

        // Max players
        buf.writeIntLE(Math.max(HytaleServer.get().getConfig().getMaxPlayers(), 0));

        // Host port
        buf.writeShortLE(getHostPort());

        // Server version
        writeString(buf, getVersion());

        // Protocol version
        buf.writeIntLE(ProtocolSettings.PROTOCOL_VERSION);

        // Protocol hash
        writeString(buf, ProtocolSettings.PROTOCOL_HASH);

        // Player list
        buf.writeIntLE(players.size());
        for (PlayerRef player : players) {
            writeString(buf, player.getUsername());
            writeUUID(buf, player.getUuid());
        }

        // Plugin list
        List<PluginBase> plugins = PluginManager.get().getPlugins();
        buf.writeIntLE(plugins.size());
        for (PluginBase plugin : plugins) {
            PluginIdentifier id = plugin.getIdentifier();
            writeString(buf, id.toString()); // e.g. "HytaleOne:Query"
            writeString(buf, plugin.getManifest().getVersion().toString());
            buf.writeBoolean(plugin.isEnabled());
        }

        return buf;
    }

    /**
     * Get the server version string.
     */
    private static String getVersion() {
        String version = ManifestUtil.getImplementationVersion();
        return version != null ? version : "unknown";
    }

    /**
     * Write a string with 2-byte little-endian length prefix.
     */
    private static void writeString(@Nonnull ByteBuf buf, @Nonnull String str) {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        buf.writeShortLE(bytes.length);
        buf.writeBytes(bytes);
    }

    /**
     * Write a UUID as 16 bytes (MSB first, then LSB).
     */
    private static void writeUUID(@Nonnull ByteBuf buf, @Nonnull UUID uuid) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }

    /**
     * Get the host port from ServerManager.
     */
    private static int getHostPort() {
        try {
            InetSocketAddress address = ServerManager.get().getNonLoopbackAddress();
            if (address != null) {
                return address.getPort();
            }
        } catch (SocketException ignored) {
        }
        return 5520; // Default port
    }
}
