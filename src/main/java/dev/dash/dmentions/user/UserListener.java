package dev.dash.dmentions.user;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class UserListener implements Listener {

    private final UserService userService;

    public UserListener(UserService userService) {
        this.userService = userService;
    }

    /** Already async, so the blocking load is fine here; the player is not in the game yet. */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;
        userService.load(event.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        userService.evict(event.getPlayer().getUniqueId());
    }
}
