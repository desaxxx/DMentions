package org.nandayo.DMentions.mention;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class MentionHolder {

    /**
     * As it states
     */
    private final @NotNull MentionType type;
    /**
     * Permission to mention
     */
    private final @NotNull String perm;
    /**
     * Case PLAYER -> player name<br>
     * Case GROUP -> group name<br>
     * Case OTHER -> null
     */
    private final @Nullable String target;

    public MentionHolder(@NotNull MentionType type, @NotNull String perm, @Nullable String target) {
        this.type = type;
        this.perm = perm;
        this.target = target;
    }
    public MentionHolder(MentionType type, @NotNull String perm) {
        this(type, perm, null);
    }
}
