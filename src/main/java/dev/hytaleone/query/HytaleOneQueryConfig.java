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
            .build();

    private boolean registerOnStartup = true;
    private String serverId = null;

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
}
