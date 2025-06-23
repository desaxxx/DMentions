package org.nandayo.dmentions.model;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class MentionCandidate {

    private final @NotNull String matched;
    private final int start;
    private final int end;

    public MentionCandidate(@NotNull String matched, int start, int end) {
        this.matched = matched;
        this.start = start;
        this.end = end;
    }
}
