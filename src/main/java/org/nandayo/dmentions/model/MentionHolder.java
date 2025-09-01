package org.nandayo.dmentions.model;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nandayo.dmentions.enumeration.MentionType;

@Getter
public class MentionHolder {

    /**
     * Mention type
     */
    private final @NotNull MentionType type;

    /**
     * Permission to mention this holder
     */
    private final @NotNull String permission;

    /**
     * Case PLAYER -> player name<br>
     * Case GROUP -> group name<br>
     * Case other -> null
     */
    private final @Nullable String target;

    public MentionHolder(@NotNull MentionType type, @NotNull String permission, @Nullable String target) {
        this.type = type;
        this.permission = permission;
        this.target = target;
    }
    public MentionHolder(MentionType type, @NotNull String permission) {
        this(type, permission, null);
    }

    @Deprecated(since = "1.8.3")
    public @NotNull String getPerm() {
        return permission;
    }
}
