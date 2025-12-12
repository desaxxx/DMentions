package org.nandayo.dmentions.user;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nandayo.dapi.util.Util;
import org.nandayo.dmentions.DMentions;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class UserManager {
    private static final Map<UUID, MentionUser> USERS = new HashMap<>();

    public static UserManager getInstance() {
        return DMentions.inst().getUserManager();
    }

    public UserManager() {}

    @ApiStatus.Internal
    public void register(MentionUser user) {
        Preconditions.checkNotNull(user, "User cannot be null.");
        USERS.put(user.getUuid(), user);
        user.onRegister();
    }

    @ApiStatus.Internal
    public void unregister(MentionUser user) {
        Preconditions.checkNotNull(user.getUuid(), "User uuid cannot be null.");
        USERS.remove(user.getUuid());
        user.onUnregister();
    }

    /**
     * @since 1.9
     */
    @ApiStatus.Internal
    public void registerAll() {
        for(Player player : Bukkit.getOnlinePlayers()) {
            MentionUser user = loadUser(player.getUniqueId());
            register(user);
        }
    }

    /**
     * @since 1.9
     */
    @ApiStatus.Internal
    public void unregisterAll() {
        for(MentionUser user : getUsers()) {
            unregister(user);
        }
    }

    @NotNull
    public Collection<MentionUser> getUsers() {
        return Collections.unmodifiableCollection(USERS.values());
    }

    @Nullable
    public MentionUser getUser(UUID uuid) {
        return USERS.get(uuid);
    }

    @NotNull
    public MentionUser loadUser(UUID uuid) {
        Preconditions.checkNotNull(uuid, "User uuid cannot be null.");
        File file = new File(DMentions.inst().getDataFolder(), "players/" + uuid + ".yml");
        if(!file.getParentFile().exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.getParentFile().mkdirs();
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("==");
        if(section == null) {
            return new MentionUser(uuid, true, null);
        }
        return MentionUser.deserialize(section.getValues(true));
    }

    @ApiStatus.Internal
    public void saveToFile(MentionUser user) {
        Preconditions.checkNotNull(user, "User cannot be null.");
        File file = new File(DMentions.inst().getDataFolder(), "players/" + user.getUuid() + ".yml");
        if(!file.getParentFile().exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.getParentFile().mkdirs();
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("==", user.serialize());
        try {
            config.save(file);
        } catch (IOException e) {
            Util.log(String.format("Failed to save player file for '%s'.", user.getUuid()));
        }
    }

    @ApiStatus.Internal
    public void saveAllToFile() {
        for(MentionUser user : USERS.values()) {
            saveToFile(user);
        }
    }








    @Deprecated(since = "1.8.4")
    public UserManager(DMentions ignore) {}

    @Deprecated(since = "1.8.4", forRemoval = true)
    public void saveChanges() {}

    @Deprecated(since = "1.8.4", forRemoval = true)
    public boolean getMentionMode(Player player) {
        MentionUser user = getUser(player.getUniqueId());
        return user != null && user.isMentionMode();
    }

    @Deprecated(since = "1.8.4", forRemoval = true)
    public void setMentionMode(Player player, boolean mode) {
        MentionUser user = getUser(player.getUniqueId());
        if(user == null) return;
        user.setMentionMode(mode);
    }

    @Deprecated(since = "1.8.4", forRemoval = true)
    public String getMentionDisplay(Player player) {
        MentionUser user = getUser(player.getUniqueId());
        return user == null ? player.getDisplayName() : user.getDisplayName();
    }

    @Deprecated(since = "1.8.4", forRemoval = true)
    public void setMentionDisplay(Player player, String display) {
        MentionUser user = getUser(player.getUniqueId());
        if(user == null) return;
        user.setCustomizedDisplayName(display);
    }
}
