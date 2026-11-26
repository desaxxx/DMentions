package dev.dash.dmentions.command.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dash.dmentions.command.SubCommand;
import dev.dash.dmentions.config.LangKeys;
import dev.dash.dmentions.message.MessageService;
import dev.dash.dmentions.message.Notification;
import dev.dash.dmentions.user.UserService;
import dev.yoshi.xconfig.ConfigKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

/** /dms toggle — the player turns being mentioned on or off. */
public class ToggleSubCommand implements SubCommand {

    private final MessageService messageService;
    private final UserService userService;

    public ToggleSubCommand(MessageService messageService, UserService userService) {
        this.messageService = messageService;
        this.userService = userService;
    }

    @Override
    public String name() {
        return "toggle";
    }

    @Override
    public PermissionDefault permissionDefault() {
        return PermissionDefault.OP;
    }

    @Override
    public ConfigKey<List<Notification>> usage() {
        return LangKeys.COMMAND_HELP_TOGGLE;
    }

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> build(LiteralArgumentBuilder<CommandSourceStack> literal) {
        return literal
                .executes(ctx -> {
                    CommandSender sender = ctx.getSource().getSender();
                    if (!(sender instanceof Player player)) {
                        messageService.send(sender, LangKeys.COMMAND_MUST_BE_PLAYER);
                        return 0;
                    }

                    boolean mentionable = !userService.isMentionable(player.getUniqueId());
                    userService.setMentionable(player.getUniqueId(), mentionable);
                    messageService.send(player, mentionable ? LangKeys.COMMAND_TOGGLE_ENABLED : LangKeys.COMMAND_TOGGLE_DISABLED);
                    return Command.SINGLE_SUCCESS;
                });
    }
}
