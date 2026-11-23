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

import java.util.List;
import java.util.function.BooleanSupplier;

public class ReloadSubCommand implements SubCommand {

    private final MessageService messageService;
    private final BooleanSupplier reloader;

    public ReloadSubCommand(MessageService messageService, BooleanSupplier reloader) {
        this.messageService = messageService;
        this.reloader = reloader;
    }

    @Override
    public String name() {
        return "reload";
    }

    @Override
    public ConfigKey<List<Notification>> usage() {
        return LangKeys.COMMAND_HELP_RELOAD;
    }

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> build(LiteralArgumentBuilder<CommandSourceStack> literal) {
        return literal
                .executes(ctx -> {
                    boolean success = reloader.getAsBoolean();
                    messageService.send(ctx.getSource().getSender(), success ? LangKeys.COMMAND_RELOAD_SUCCESS : LangKeys.COMMAND_RELOAD_FAILED);
                    return success ? Command.SINGLE_SUCCESS : 0;
                });
    }
}
