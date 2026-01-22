package dev.hytaleone.query;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

/**
 * Configuration for the HytaleOne Query plugin.
 */
public class HytaleOneQueryConfig {

    public static final BuilderCodec<HytaleOneQueryConfig> CODEC = BuilderCodec.builder(HytaleOneQueryConfig.class, HytaleOneQueryConfig::new)
            .addField(new KeyedCodec<>("RegisterOnStartup", Codec.BOOLEAN),
                    (o, v) -> o.registerOnStartup = v, o -> o.registerOnStartup)
            .addField(new KeyedCodec<>("ServerIdDoNotChange", Codec.STRING),
                    (o, v) -> o.serverId = v, o -> o.serverId)
            .addField(new KeyedCodec<>("EnableFullQuery", Codec.BOOLEAN),
                    (o, v) -> o.enableFullQuery = v, o -> o.enableFullQuery)
            .build();

    private boolean registerOnStartup = false;
    private String serverId = null;
    private boolean enableFullQuery = false;

    public HytaleOneQueryConfig() {
    }

    /**
     * Whether to automatically register with the server list on startup.
     */
    public boolean isRegisterOnStartup() {
        return registerOnStartup;
    }

    public void setRegisterOnStartup(boolean register) {
        this.registerOnStartup = register;
    }

    /**
     * Unique server identifier for duplicate prevention.
     */
    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    /**
     * Though I know I'll never lose affection
     * For people and things that went before
     * I know I'll often stop and think about them
     * In my life, I love you more
     */
    public boolean isEnableFullQuery() {
        return enableFullQuery;
    }

    public void setEnableFullQuery(boolean register) {
        this.enableFullQuery = register;
    }
}
