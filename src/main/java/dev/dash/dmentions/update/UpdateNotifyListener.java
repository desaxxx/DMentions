package dev.dash.dmentions.update;

import dev.dash.dmentions.config.LangKeys;
import dev.dash.dmentions.message.MessageService;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class UpdateNotifyListener implements Listener {

    public static final String PERMISSION = "dmentions.update.notify";

    private final UpdateChecker checker;
    private final MessageService messageService;

    public UpdateNotifyListener(UpdateChecker checker, MessageService messageService) {
        this.checker = checker;
        this.messageService = messageService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission(PERMISSION)) return;

        checker.newerVersion().ifPresent(latest -> messageService.send(player, LangKeys.UPDATE_AVAILABLE,
                Placeholder.unparsed("current", checker.currentVersion()),
                Placeholder.unparsed("latest", latest)));
    }
}
