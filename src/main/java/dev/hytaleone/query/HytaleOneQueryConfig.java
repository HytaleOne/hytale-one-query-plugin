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
            .build();

    private boolean registerOnStartup = true;

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
}
