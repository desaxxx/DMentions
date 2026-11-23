package dev.dash.dmentions.command.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.dash.dmentions.command.SubCommand;
import dev.dash.dmentions.config.ConfigKeys;
import dev.dash.dmentions.config.LangKeys;
import dev.dash.dmentions.mention.MentionRegistry;
import dev.dash.dmentions.message.MessageService;
import dev.dash.dmentions.message.Notification;
import dev.yoshi.xconfig.ConfigKey;
import dev.yoshi.xconfig.XConfig;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class SendSubCommand implements SubCommand {

    private final XConfig config;
    private final MessageService messageService;
    private final MentionRegistry mentionRegistry;

    public SendSubCommand(XConfig config, MessageService messageService, MentionRegistry mentionRegistry) {
        this.config = config;
        this.messageService = messageService;
        this.mentionRegistry = mentionRegistry;
    }

    @Override
    public String name() {
        return "send";
    }

    @Override
    public ConfigKey<List<Notification>> usage() {
        return LangKeys.COMMAND_HELP_SEND;
    }

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> build(LiteralArgumentBuilder<CommandSourceStack> literal) {
        return literal
                .executes(ctx -> {
                    messageService.send(ctx.getSource().getSender(), usage());
                    return Command.SINGLE_SUCCESS;
                })
                // greedy: keywords like "@everyone" contain characters a plain word argument rejects
                .then(Commands.argument("keyword", StringArgumentType.greedyString())
                        .suggests(this::suggestKeywords)
                        .executes(this::send));
    }

    private int send(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        if (!(sender instanceof Player player)) {
            messageService.send(sender, LangKeys.COMMAND_MUST_BE_PLAYER);
            return 0;
        }

        String input = StringArgumentType.getString(ctx, "keyword").trim();
        Optional<String> keyword = findKeyword(mentionRegistry.candidatesFor(player).keySet(), input);
        if (keyword.isEmpty()) {
            messageService.send(sender, LangKeys.COMMAND_SEND_INVALID_KEYWORD, Placeholder.unparsed("keyword", input));
            return 0;
        }

        player.chat(keyword.get());
        return Command.SINGLE_SUCCESS;
    }

    private Optional<String> findKeyword(Set<String> keywords, String input) {
        if (keywords.contains(input)) return Optional.of(input);
        if (config.get(ConfigKeys.CASE_SENSITIVE)) return Optional.empty();
        return keywords.stream().filter(k -> k.equalsIgnoreCase(input)).findFirst();
    }

    private CompletableFuture<Suggestions> suggestKeywords(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        if (ctx.getSource().getSender() instanceof Player player) {
            String typed = builder.getRemainingLowerCase();
            mentionRegistry.candidatesFor(player).keySet().stream()
                    .filter(k -> k.toLowerCase(Locale.ROOT).startsWith(typed))
                    .sorted()
                    .forEach(builder::suggest);
        }
        return builder.buildFuture();
    }
}
