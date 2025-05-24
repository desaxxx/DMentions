package org.nandayo.dmentions.integration;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import net.ess3.api.events.VanishStatusChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.nandayo.dmentions.DMentions;
import org.nandayo.dmentions.service.MessageManager;

public class EssentialsHook {

    static private Essentials ess = null;
    public EssentialsHook() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Essentials");
        if (plugin instanceof Essentials) ess = (Essentials) plugin;
    }

    /**
     * Check if a player is ignored by another player.
     * @param suspected Suspected player to be ignored
     * @param player Player
     * @return whether ignored or not
     */
    static public boolean isIgnored(@NotNull Player suspected, @NotNull Player player) {
        if(ess == null) return false;
        User user = ess.getUser(player);
        return user._getIgnoredPlayers().contains(suspected.getUniqueId());
    }

    /**
     * Check if a player is AFK.
     * @param player Player
     * @return whether AFK or not
     */
    static public boolean isAFK(@NotNull Player player) {
        if(ess == null) return false;
        return ess.getUser(player).isAfk();
    }

    /**
     * Check if a player is vanished.
     * @param player Player
     * @return whether vanished or not
     */
    static public boolean isVanished(@NotNull Player player) {
        if(ess == null) return false;
        return ess.getUser(player).isVanished();
    }



    public static class EssentialsListener implements Listener {

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
            MessageManager.sendSortedMessage(player, plugin.getLanguageManager().getString("vanish_notify." + value));
        }
    }
}
