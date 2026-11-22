package dev.dash.dmentions.command.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dash.dmentions.command.SubCommand;
import dev.dash.dmentions.config.LangKeys;
import dev.dash.dmentions.message.MessageService;
import dev.dash.dmentions.message.Notification;
import dev.yoshi.xconfig.ConfigKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

public class VersionSubCommand implements SubCommand {

    private final MessageService messageService;
    private final String version;

    public VersionSubCommand(MessageService messageService, String version) {
        this.messageService = messageService;
        this.version = version;
    }

    @Override
    public String name() {
        return "version";
    }

    @Override
    public PermissionDefault permissionDefault() {
        return PermissionDefault.TRUE;
    }

    @Override
    public ConfigKey<List<Notification>> usage() {
        return LangKeys.COMMAND_HELP_VERSION;
    }

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> build(LiteralArgumentBuilder<CommandSourceStack> literal) {
        return literal.executes(ctx -> {
            messageService.send(ctx.getSource().getSender(), LangKeys.COMMAND_VERSION, Placeholder.unparsed("version", version));
            return Command.SINGLE_SUCCESS;
        });
    }
}
