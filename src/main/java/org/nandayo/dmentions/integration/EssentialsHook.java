package org.nandayo.dmentions.integration;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import net.ess3.api.events.VanishStatusChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.nandayo.dmentions.DMentions;
import org.nandayo.dmentions.provider.VanishProvider;
import org.nandayo.dmentions.service.message.Message;

public class EssentialsHook implements IHook, VanishProvider {

    private final @NotNull DMentions plugin;
    private Object mask = null;
    public EssentialsHook(@NotNull DMentions plugin) {
        this.plugin = plugin;
        pluginCondition("Essentials", ess -> this.mask = ess);
    }

    @Override
    public Object mask() {
        return this.mask;
    }

    /**
     * Get Essentials. Use with caution as it may not be present. Check with {@link #isMaskNull()} beforehand.
     * @return Essentials
     * @since 1.8.3
     */
    private Essentials getAPI() {
        return (Essentials) this.mask;
    }

    /**
     * Check if a player is ignored by another player.
     * @param suspected Suspected player to be ignored
     * @param player Player
     * @return whether ignored or not
     */
    public boolean isIgnored(@NotNull Player suspected, @NotNull Player player) {
        if(isMaskNull()) return false;
        User user = getAPI().getUser(player);
        return user._getIgnoredPlayers().contains(suspected.getUniqueId());
    }

    /**
     * Check if a player is AFK.
     * @param player Player
     * @return whether AFK or not
     */
    public boolean isAFK(@NotNull Player player) {
        if(isMaskNull()) return false;
        return getAPI().getUser(player).isAfk();
    }

    /**
     * Check if a player is vanished.
     * @param player Player
     * @return whether vanished or not
     */
    @Override
    public boolean isVanished(Player player) {
        if(isMaskNull() || player == null) return false;
        return getAPI().getUser(player).isVanished();
    }




    static public class EssentialsListener implements Listener {

        private final @NotNull DMentions plugin;
        public EssentialsListener(@NotNull DMentions plugin) {
            this.plugin = plugin;
        }

        @EventHandler
        public void onVanishChange(VanishStatusChangeEvent event) {
            if(!plugin.getConfiguration().getConfig().getBoolean("vanish_respect", true)) return;

            Player player = event.getAffected().getBase();
            boolean value = event.getValue();
            if(value) {
                plugin.getMentionManager().removePlayer(player);
            }else {
                plugin.getMentionManager().addPlayer(player);
            }
            Message.VANISH_NOTIFY_X.replaceKey("{x}", String.valueOf(value)).sendMessage(player);
        }
    }
}
