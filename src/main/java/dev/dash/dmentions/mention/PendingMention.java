package dev.dash.dmentions.mention;

import org.bukkit.entity.Player;

public record PendingMention(Player sender, Player target) {
}
