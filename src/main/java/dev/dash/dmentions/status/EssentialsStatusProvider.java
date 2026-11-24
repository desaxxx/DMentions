package dev.dash.dmentions.status;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class EssentialsStatusProvider implements PlayerStatusProvider {

    private final Essentials ess;

    public EssentialsStatusProvider() {
        this.ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
    }

    @Override
    public boolean isAFK(Player player) {
        User user = ess.getUser(player);
        return user != null && user.isAfk();
    }

    @Override
    public boolean isIgnoring(Player player, Player other) {
        User u1 = ess.getUser(player);
        User u2 = ess.getUser(other);
        return u1 != null && u2 != null && u1.isIgnoredPlayer(u2);
    }
}
