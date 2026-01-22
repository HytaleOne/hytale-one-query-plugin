package dev.hytaleone.query;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.io.ServerManager;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;

import javax.annotation.Nonnull;
import java.util.logging.Level;

/**
 * Lightweight UDP query protocol plugin for Hytale servers.
 * Adds a query handler to the server's Netty pipeline to intercept
 * query requests on the same port as the game server.
 */
public class HytaleOneQueryPlugin extends JavaPlugin {

    private static final String HANDLER_NAME = "hytaleone-query";
    private static final String CONFIG_MODULE = "HytaleOneQuery";

    private HytaleOneQueryHandler queryHandler;
    private HytaleOneQueryConfig config;

    public HytaleOneQueryPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        // Load configuration from server config
        loadConfig();
    }

    @Override
    protected void start() {
        // Wait for ServerManager to finish binding
        ServerManager.get().waitForBindComplete();

        // Create the shared query handler
        this.queryHandler = new HytaleOneQueryHandler(getLogger(), getConfig());

        // Inject handler into all listener pipelines
        int registered = 0;
        for (Channel channel : ServerManager.get().getListeners()) {
            try {
                ChannelPipeline pipeline = channel.pipeline();
                pipeline.addFirst(HANDLER_NAME, queryHandler);
                registered++;
                getLogger().at(Level.FINE).log("Registered query handler on %s", channel.localAddress());
            } catch (Exception e) {
                getLogger().at(Level.WARNING).withCause(e).log(
                        "Failed to register query handler on %s", channel.localAddress());
            }
        }

        getLogger().at(Level.INFO).log("Query protocol enabled on %d listener(s)", registered);

        // Register with server list service (if enabled)
        if (config.isRegisterOnStartup()) {
            HytaleOneServerListRegistration.register(getLogger(), config, this::saveConfig);
        } else {
            getLogger().at(Level.INFO).log("Server list registration is disabled");
        }
    }

    @Override
    protected void shutdown() {
        if (queryHandler == null) {
            return;
        }

        // Remove handler from all listener pipelines
        int removed = 0;
        for (Channel channel : ServerManager.get().getListeners()) {
            try {
                ChannelPipeline pipeline = channel.pipeline();
                if (pipeline.get(HANDLER_NAME) != null) {
                    pipeline.remove(HANDLER_NAME);
                    removed++;
                }
            } catch (Exception e) {
                getLogger().at(Level.FINE).log("Handler already removed from %s", channel.localAddress());
            }
        }

        getLogger().at(Level.INFO).log("Query protocol disabled, removed from %d listener(s)", removed);
        this.queryHandler = null;
    }

    private void loadConfig() {
        var serverConfig = HytaleServer.get().getConfig();
        var module = serverConfig.getModule(CONFIG_MODULE);

        this.config = module.decode(HytaleOneQueryConfig.CODEC);
        if (this.config == null) {
            this.config = new HytaleOneQueryConfig();
            module.encode(HytaleOneQueryConfig.CODEC, this.config);
            serverConfig.markChanged();
        }
    }

    private void saveConfig() {
        var serverConfig = HytaleServer.get().getConfig();
        var module = serverConfig.getModule(CONFIG_MODULE);
        module.encode(HytaleOneQueryConfig.CODEC, this.config);
        serverConfig.markChanged();
    }

    @Nonnull
    public HytaleOneQueryConfig getConfig() {
        return config;
    }
}
