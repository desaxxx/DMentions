package dev.dash.dmentions.command.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dash.dmentions.command.SubCommand;
import dev.dash.dmentions.config.LangKeys;
import dev.dash.dmentions.menu.ConfigMenus;
import dev.dash.dmentions.message.MessageService;
import dev.dash.dmentions.message.Notification;
import dev.yoshi.xconfig.ConfigKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ConfigSubCommand implements SubCommand {

    private final MessageService messageService;
    private final ConfigMenus menus;

    public ConfigSubCommand(MessageService messageService, ConfigMenus menus) {
        this.messageService = messageService;
        this.menus = menus;
    }

    @Override
    public String name() {
        return "config";
    }

    @Override
    public ConfigKey<List<Notification>> usage() {
        return LangKeys.COMMAND_HELP_CONFIG;
    }

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> build(LiteralArgumentBuilder<CommandSourceStack> literal) {
        return literal.executes(ctx -> {
            CommandSender sender = ctx.getSource().getSender();
            if (!(sender instanceof Player player)) {
                messageService.send(sender, LangKeys.COMMAND_MUST_BE_PLAYER);
                return 0;
            }
            menus.open(player);
            return Command.SINGLE_SUCCESS;
        });
    }
}
