package dev.dash.dmentions.mention;

import java.util.Map;

public record Candidates(Map<String, MentionCandidate> allowed, Map<String, BlockReason> blocked) {
}
