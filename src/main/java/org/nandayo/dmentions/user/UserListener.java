package org.nandayo.dmentions.user;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class UserListener implements Listener {
    public static final UserListener INSTANCE = new UserListener();
    private UserListener() {}

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UserManager manager = UserManager.getInstance();
        MentionUser user = manager.loadUser(player.getUniqueId());
        manager.register(user);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UserManager manager = UserManager.getInstance();
        MentionUser user = manager.getUser(event.getPlayer().getUniqueId());
        if(user == null) return;

        manager.unregister(user.getUuid());
        manager.saveToFile(user);
    }
}
