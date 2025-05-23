package org.nandayo.dmentions.enumeration;

import lombok.Getter;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

@Getter
public enum MentionType {
    PLAYER(Material.PLAYER_HEAD),
    NEARBY(Material.TARGET),
    EVERYONE(Material.BEACON),
    GROUP(Material.GREEN_BANNER);

    MentionType(@NotNull Material iconMaterial) {
        this.iconMaterial = iconMaterial;
    }

    private final @NotNull Material iconMaterial;
}
