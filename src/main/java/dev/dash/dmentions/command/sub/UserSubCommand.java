package dev.dash.dmentions.command.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.dash.dmentions.command.SubCommand;
import dev.dash.dmentions.config.LangKeys;
import dev.dash.dmentions.message.MessageService;
import dev.dash.dmentions.message.Notification;
import dev.dash.dmentions.user.UserService;
import dev.yoshi.xconfig.ConfigKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * /dms user &lt;player&gt; mentionable &lt;true|false&gt; — an admin sets whether a player can be mentioned.
 * /dms user &lt;player&gt; custom_name &lt;name&gt; — an admin sets the name shown when a player is mentioned.
 * Only online players: user data is cached while a player is online, an offline player's data is not loaded.
 * The player is a plain name, not a selector, so "player not found" is our own message.
 */
public class UserSubCommand implements SubCommand {

    private final MessageService messageService;
    private final UserService userService;

    public UserSubCommand(MessageService messageService, UserService userService) {
        this.messageService = messageService;
        this.userService = userService;
    }

    @Override
    public String name() {
        return "user";
    }

    @Override
    public ConfigKey<List<Notification>> usage() {
        return LangKeys.COMMAND_HELP_USER;
    }

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> build(LiteralArgumentBuilder<CommandSourceStack> literal) {
        return literal
                .executes(this::showUsage)
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(this::suggestPlayers)
                        .executes(this::showUsage)
                        .then(Commands.literal("mentionable")
                                .executes(this::showUsage)
                                // bool() suggests and accepts only true/false
                                .then(Commands.argument("value", BoolArgumentType.bool())
                                        .executes(this::setMentionable)))
                        .then(Commands.literal("custom_name")
                                .executes(this::showUsage)
                                .then(Commands.argument("value", StringArgumentType.word())
                                        .executes(this::setCustomName))));
    }

    private int showUsage(CommandContext<CommandSourceStack> ctx) {
        messageService.send(ctx.getSource().getSender(), usage());
        return Command.SINGLE_SUCCESS;
    }

    /** The online player named in the command, or empty after telling the sender they were not found. */
    private Optional<Player> findTarget(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "player");
        Player target = Bukkit.getPlayerExact(name);
        if (target == null) {
            messageService.send(ctx.getSource().getSender(), LangKeys.COMMAND_PLAYER_NOT_FOUND, Placeholder.unparsed("player", name));
        }
        return Optional.ofNullable(target);
    }

    private int setMentionable(CommandContext<CommandSourceStack> ctx) {
        Optional<Player> found = findTarget(ctx);
        if (found.isEmpty()) return 0;
        Player target = found.get();
        boolean mentionable = BoolArgumentType.getBool(ctx, "value");

        userService.setMentionable(target.getUniqueId(), mentionable);
        messageService.send(ctx.getSource().getSender(),
                mentionable ? LangKeys.COMMAND_USER_MENTIONABLE_ENABLED : LangKeys.COMMAND_USER_MENTIONABLE_DISABLED,
                Placeholder.unparsed("target", target.getName()));
        return Command.SINGLE_SUCCESS;
    }

    private int setCustomName(CommandContext<CommandSourceStack> ctx) {
        Optional<Player> found = findTarget(ctx);
        if (found.isEmpty()) return 0;
        Player target = found.get();

        String input = StringArgumentType.getString(ctx, "value");
        if (!CustomizeSubCommand.CUSTOM_NAME.matcher(input).matches()) {
            messageService.send(ctx.getSource().getSender(), LangKeys.COMMAND_CUSTOMIZE_INVALID_CUSTOM_NAME, Placeholder.unparsed("value", input));
            return 0;
        }

        String oldValue = userService.customName(target.getUniqueId()).orElse(target.getName());
        // Same rule as /dms customize: the real name in any case removes the custom name.
        String customName = input.equalsIgnoreCase(target.getName()) ? null : input;
        userService.setCustomName(target.getUniqueId(), customName);

        messageService.send(ctx.getSource().getSender(), LangKeys.COMMAND_USER_CUSTOM_NAME_SUCCESS,
                Placeholder.unparsed("target", target.getName()),
                Placeholder.unparsed("old_value", oldValue),
                Placeholder.unparsed("value", customName == null ? target.getName() : customName));
        return Command.SINGLE_SUCCESS;
    }

    private CompletableFuture<Suggestions> suggestPlayers(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        String typed = builder.getRemainingLowerCase();
        Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(typed))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .forEach(builder::suggest);
        return builder.buildFuture();
    }
}
