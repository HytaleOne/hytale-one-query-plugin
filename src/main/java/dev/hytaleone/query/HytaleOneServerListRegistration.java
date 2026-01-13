package dev.hytaleone.query;

import com.hypixel.hytale.common.util.java.ManifestUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.ProtocolSettings;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.io.ServerManager;
import com.hypixel.hytale.server.core.universe.Universe;

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Registers the server with a central server list service on startup.
 */
public final class HytaleOneServerListRegistration {

    private static final String ENDPOINT = "https://hytale.one/api/plugin/query/register";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private HytaleOneServerListRegistration() {
    }

    /**
     * Register the server with the server list service.
     * Runs asynchronously to not block server startup.
     */
    public static void register(@Nonnull HytaleLogger logger) {

        CompletableFuture.runAsync(() -> {
            try {
                String json = buildPayload();

                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(TIMEOUT)
                        .build();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(ENDPOINT))
                        .header("Content-Type", "application/json")
                        .timeout(TIMEOUT)
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> response = client.send(request,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    logger.at(Level.INFO).log("Server registered with hytale.one");
                } else {
                    logger.at(Level.WARNING).log("Server list registration failed (status: %d)",
                            response.statusCode());
                }
            } catch (Exception e) {
                logger.at(Level.WARNING).log("Failed to register with server list: %s", e.getMessage());
            }
        });
    }

    /**
     * Build the JSON payload with server information.
     */
    private static String buildPayload() {
        var config = HytaleServer.get().getConfig();

        String host = getHostAddress();
        int port = getHostPort();

        // Manual JSON building to avoid external dependencies
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"serverName\":").append(escapeJson(config.getServerName())).append(",");
        json.append("\"motd\":").append(escapeJson(config.getMotd())).append(",");
        json.append("\"host\":").append(escapeJson(host)).append(",");
        json.append("\"port\":").append(port).append(",");
        json.append("\"maxPlayers\":").append(config.getMaxPlayers()).append(",");
        json.append("\"currentPlayers\":").append(Universe.get().getPlayerCount()).append(",");
        json.append("\"version\":").append(escapeJson(getVersion())).append(",");
        json.append("\"protocolVersion\":").append(ProtocolSettings.PROTOCOL_VERSION);
        json.append("}");

        return json.toString();
    }

    private static String getVersion() {
        String version = ManifestUtil.getImplementationVersion();
        return version != null ? version : "unknown";
    }

    private static String getHostAddress() {
        try {
            InetSocketAddress address = ServerManager.get().getNonLoopbackAddress();
            if (address != null && address.getAddress() != null) {
                return address.getAddress().getHostAddress();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static int getHostPort() {
        try {
            InetSocketAddress address = ServerManager.get().getNonLoopbackAddress();
            if (address != null) {
                return address.getPort();
            }
        } catch (Exception ignored) {
        }
        return 5520;
    }

    /**
     * Escape a string for JSON.
     */
    private static String escapeJson(String str) {
        if (str == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("\"");
        for (char c : str.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 32) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append("\"");
        return sb.toString();
    }
}
