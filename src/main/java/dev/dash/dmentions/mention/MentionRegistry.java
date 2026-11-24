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

    public Map<String, MentionCandidate> candidatesFor(Player sender) {
        Map<String, MentionCandidate> candidates = new HashMap<>();

        for (MentionType type : types) {
            if (!type.enabled()) continue;

            for (MentionCandidate c : type.candidates()) {
                if (sender.getUniqueId().toString().equals(c.targetId())) continue;
                String permission = type.permission(c);
                if (!permission.isEmpty() && !sender.hasPermission(permission)) continue;
                Optional<Player> target = type.singleTarget(c);
                if (target.isPresent() && !targetFilter.allows(sender, target.get(), type)) continue;

                candidates.putIfAbsent(c.keyword(), c);
            }
        }

        return candidates;
    }

    public List<MentionType> types() {
        return Collections.unmodifiableList(types);
    }
}
