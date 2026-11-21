package dev.dash.dmentions.mention;

import org.bukkit.entity.Player;

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

            type.candidates().forEach(c -> candidates.putIfAbsent(c.keyword(), c)); 
        }

        return candidates;
    }
}
