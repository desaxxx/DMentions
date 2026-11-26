package dev.dash.dmentions.mention;

import org.bukkit.entity.Player;

import java.util.*;


public final class MentionRegistry {

    private final TargetFilter targetFilter;
    private final List<MentionType> types;

    public MentionRegistry(TargetFilter targetFilter, MentionType... types) {
        this.targetFilter = targetFilter;
        this.types = List.of(types);
    }

    public Candidates candidatesFor(Player sender) {
        Map<String, MentionCandidate> allowed = new HashMap<>();
        Map<String, BlockReason> blocked = new HashMap<>();

        for (MentionType type : types) {
            if (!type.enabled()) continue;

            for (MentionCandidate c : type.candidates()) {
                if (sender.getUniqueId().toString().equals(c.targetId())) continue;
                String permission = type.permission(c);
                if (!permission.isEmpty() && !sender.hasPermission(permission)) continue;
                Optional<Player> target = type.singleTarget(c);
                if (target.isPresent()) {
                    BlockReason blockReason = targetFilter.check(sender, target.get(), type).orElse(null);
                    if (blockReason == BlockReason.AFK || blockReason == BlockReason.RESTRICTED) {
                        blocked.putIfAbsent(c.keyword(), blockReason);
                    }
                    if (blockReason != null) continue;
                }

                allowed.putIfAbsent(c.keyword(), c);
            }
        }

        return new Candidates(allowed, blocked);
    }

    public List<MentionType> types() {
        return Collections.unmodifiableList(types);
    }
}
