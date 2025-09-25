package org.nandayo.dmentions.user;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @since 1.9
 */
@Getter
public final class MentionUser extends AbstractModuleDataHolder implements ConfigurationSerializable {

    private final @NotNull UUID uuid;
    @Setter
    private boolean mentionMode;
    @Setter
    private @Nullable String customizedDisplayName;

    @ApiStatus.Internal
    public MentionUser(UUID uuid, boolean mentionMode, String customizedDisplayName) {
        Preconditions.checkNotNull(uuid);

        this.uuid = uuid;
        this.mentionMode = mentionMode;
        this.customizedDisplayName = customizedDisplayName;
    }


    /**
     * Get display name of the user.
     * @return {@link #customizedDisplayName} if the user customized it, {@link OfflinePlayer#getName()} otherwise.
     * @since 1.9
     */
    @NotNull
    public String getDisplayName() {
        if(customizedDisplayName != null) return customizedDisplayName;
        OfflinePlayer offlinePlayer = getOfflinePlayer();
        return offlinePlayer.getName() != null ? offlinePlayer.getName() : "UNKNOWN";
    }

    /**
     * Get the player from user.
     * @return Option of Player
     * @since 1.9
     */
    public Optional<Player> getPlayer() {
        return Optional.ofNullable(Bukkit.getPlayer(uuid));
    }

    /**
     * Get the offline player from user.
     * @return OfflinePlayer
     * @since 1.9
     */
    @NotNull
    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(uuid);
    }

    /**
     * Get name of the player.
     * @return Name of the player. This might return null in case the player never joined -which shouldn't happen
     * for a user object-. You can use {@link #getDisplayName()} instead to get a nonnull result.
     * @since 1.9
     */
    @Nullable
    public String getName() {
        return getOfflinePlayer().getName();
    }


    @ApiStatus.Internal
    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("uuid", uuid.toString());
        map.put("mention_mode", mentionMode);
        map.put("customized_display_name", customizedDisplayName);

        return map;
    }

    @ApiStatus.Internal
    @NotNull
    public static MentionUser deserialize(Map<String, Object> map) {
        UUID uuid = UUID.fromString((String) map.get("uuid"));
        boolean mentionMode = (boolean) map.getOrDefault("mention_mode", true);
        String customizedDisplayName = (String) map.getOrDefault("customized_display_name", null);

        return new MentionUser(uuid, mentionMode, customizedDisplayName);
    }
}
