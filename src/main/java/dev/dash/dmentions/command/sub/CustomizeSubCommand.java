package dev.dash.dmentions.command.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.dash.dmentions.command.SubCommand;
import dev.dash.dmentions.config.LangKeys;
import dev.dash.dmentions.message.MessageService;
import dev.dash.dmentions.message.Notification;
import dev.dash.dmentions.user.UserService;
import dev.yoshi.xconfig.ConfigKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.regex.Pattern;

public class CustomizeSubCommand implements SubCommand {

    /** Same rule as a Minecraft username. */
    public static final Pattern CUSTOM_NAME = Pattern.compile("[a-zA-Z0-9_]{3,16}");

    private final MessageService messageService;
    private final UserService userService;

    public CustomizeSubCommand(MessageService messageService, UserService userService) {
        this.messageService = messageService;
        this.userService = userService;
    }

    @Override
    public String name() {
        return "customize";
    }

    @Override
    public ConfigKey<List<Notification>> usage() {
        return LangKeys.COMMAND_HELP_CUSTOMIZE;
    }

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> build(LiteralArgumentBuilder<CommandSourceStack> literal) {
        return literal
                .executes(ctx -> {
                    messageService.send(ctx.getSource().getSender(), usage());
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.argument("custom_name", StringArgumentType.word())
                        .executes(this::customize));
    }

    private int customize(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        if (!(sender instanceof Player player)) {
            messageService.send(sender, LangKeys.COMMAND_MUST_BE_PLAYER);
            return 0;
        }

        String input = StringArgumentType.getString(ctx, "custom_name");
        if (!CUSTOM_NAME.matcher(input).matches()) {
            messageService.send(player, LangKeys.COMMAND_CUSTOMIZE_INVALID_CUSTOM_NAME, Placeholder.unparsed("value", input));
            return 0;
        }

        String oldValue = userService.customName(player.getUniqueId()).orElse(player.getName());
        // The real name in any case means "no custom name", so nothing is stored for it.
        String customName = input.equalsIgnoreCase(player.getName()) ? null : input;
        userService.setCustomName(player.getUniqueId(), customName);

        messageService.send(player, LangKeys.COMMAND_CUSTOMIZE_SUCCESS,
                Placeholder.unparsed("old_value", oldValue),
                Placeholder.unparsed("value", customName == null ? player.getName() : customName));
        return Command.SINGLE_SUCCESS;
    }
}
