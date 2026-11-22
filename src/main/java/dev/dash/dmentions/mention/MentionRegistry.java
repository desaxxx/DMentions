package dev.dash.dmentions.mention;

import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class MentionRegistry {

    private final List<MentionType> types;

    public MentionRegistry(MentionType... types) {
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
                candidates.putIfAbsent(c.keyword(), c);
            }
        }

        return candidates;
    }

    public List<MentionType> types() {
        return Collections.unmodifiableList(types);
    }
}
