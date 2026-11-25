package dev.dash.dmentions.menu;

import dev.yoshi.xconfig.ConfigException;
import dev.yoshi.xconfig.ConfigKey;
import dev.yoshi.xconfig.WritableXConfig;

import java.util.function.BooleanSupplier;
import java.util.logging.Level;
import java.util.logging.Logger;

// Saves a menu change to config.yml (comments kept), then applies it the way /dms reload does.
public final class ConfigEditor {

    private final WritableXConfig config;
    private final BooleanSupplier applier;
    private final Logger logger;

    public ConfigEditor(WritableXConfig config, BooleanSupplier applier, Logger logger) {
        this.config = config;
        this.applier = applier;
        this.logger = logger;
    }

    public WritableXConfig config() {
        return config;
    }

    public <T> boolean set(ConfigKey<T> key, T value) {
        try {
            config.set(key, value);
        } catch (ConfigException | IllegalArgumentException e) {
            logger.log(Level.SEVERE, "Could not save " + key.path() + " to " + config.file().getFileName() + ".", e);
            return false;
        }
        return applier.getAsBoolean();
    }
}
