package org.nandayo.DMentions.mention;

public class MentionHolder {

    private final MentionType type;
    private final String perm;
    private final String target; // Could be a player name or a group name (null in cases it's everyone and nearby)

    public MentionHolder(MentionType type, String perm, String target) {
        this.type = type;
        this.perm = perm;
        this.target = target;
    }

    public MentionType getType() {
        return type;
    }
    public String getTarget() {
        return target;
    }
    public String getPerm() {
        return perm;
    }
}
