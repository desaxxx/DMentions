package org.nandayo.dmentions.integration;

import net.shortninja.staffplusplus.IStaffPlus;
import net.shortninja.staffplusplus.session.IPlayerSession;
import net.shortninja.staffplusplus.vanish.VanishOffEvent;
import net.shortninja.staffplusplus.vanish.VanishOnEvent;
import net.shortninja.staffplusplus.vanish.VanishType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nandayo.dmentions.DMentions;
import org.nandayo.dmentions.provider.VanishProvider;

public class StaffPPHook implements IHook, VanishProvider {

    private final @NotNull DMentions plugin;
    private Object mask = null;
    public StaffPPHook(@NotNull DMentions plugin) {
        this.plugin = plugin;
        pluginCondition("StaffPlusPlus", spp -> {
            RegisteredServiceProvider<IStaffPlus> provider = Bukkit.getServicesManager().getRegistration(IStaffPlus.class);
            if (provider != null) {
                this.mask = provider.getProvider();
            }
        });
    }

    @Override
    public @Nullable Object mask() {
        return mask;
    }

    /**
     * Get IStaffPlus. Use with caution as it may not be present. Check with {@link #isMaskNull()} beforehand.
     * @return IStaffPlus
     * @since 1.8.3
     */
    private IStaffPlus getAPI() {
        return (IStaffPlus) this.mask;
    }

    @Override
    public boolean isVanished(Player player) {
        if(player == null || isMaskNull()) return false;
        IPlayerSession session = getAPI().getSessionManager().get(player);
        if(session == null) return false;
        VanishType vanishType = session.getVanishType();
        return vanishType != null && isVanish(vanishType);
    }

    // PLayerSession#isVanished() return true when type is either PLAYER or TOTAL.
    private static boolean isVanish(VanishType vanishType) {
        // PLAYER type doesn't prevent players tab completing the vanished player.
        switch (vanishType) {
            case LIST:
            case TOTAL:
                return true;
            default:
                return false;
        }
    }

    static public class StaffPlusPlusListener implements Listener {

        private final @NotNull DMentions plugin;
        public StaffPlusPlusListener(@NotNull DMentions plugin) {
            this.plugin = plugin;
        }

        @EventHandler
        public void onVanishOn(VanishOnEvent event) {
            if(!plugin.getConfiguration().getConfig().getBoolean("vanish_respect", true)) return;
            if(!isVanish(event.getType())) return;

            Player player = event.getPlayer();
            plugin.getMentionManager().removePlayer(player);
        }

        @EventHandler
        public void onVanishOff(VanishOffEvent event) {
            if(!plugin.getConfiguration().getConfig().getBoolean("vanish_respect", true)) return;
            if(!isVanish(event.getType())) return;

            Player player = event.getPlayer();
            plugin.getMentionManager().addPlayer(player);
        }
    }
}
