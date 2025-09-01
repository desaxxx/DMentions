package org.nandayo.dmentions.integration;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * @since 1.8.3
 */
public interface IHook {

    /**
     * Accepts a consumer if the plugin is found and enabled.
     * @param pluginName Plugin name to look up.
     * @param pluginConsumer Consumer to accept upon plugin existence.
     * @since 1.8.3
     */
    default void pluginCondition(@NotNull String pluginName, @NotNull Consumer<@NotNull Plugin> pluginConsumer) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        if(plugin != null && plugin.isEnabled()) {
            pluginConsumer.accept(plugin);
        }
    }

    /**
     * Represents hooked API with Object mask.
     * @return Object as API
     * @since 1.8.3
     */
    @Nullable Object mask();

    /**
     * Check if masked API is null.
     * @return whether null or not.
     * @since 1.8.3
     */
    default boolean isMaskNull() {
        return mask() == null;
    }
}
