package dev.dash.dmentions.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.dash.dmentions.config.LangKeys;
import dev.dash.dmentions.message.MessageService;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class DMentionsCommand {

    private final List<SubCommand> subCommands;
    private final MessageService messageService;

    public DMentionsCommand(List<SubCommand> subCommands, MessageService messageService) {
        this.subCommands = List.copyOf(subCommands);
        this.messageService = messageService;
    }

    public void register(LifecycleEventManager<Plugin> lifecycle) {
        lifecycle.registerEventHandler(LifecycleEvents.COMMANDS, event ->
                event.registrar().register(
                        buildTree(),
                        "DMentions commands",
                        List.of("dms", "mentions")
                )
        );
    }

    private LiteralCommandNode<CommandSourceStack> buildTree() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("dmentions")
                .executes(ctx -> {
                    sendHelp(ctx.getSource().getSender());
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.literal("help")
                        .executes(ctx -> {
                            sendHelp(ctx.getSource().getSender());
                            return Command.SINGLE_SUCCESS;
                        })
                );

        for (SubCommand sub : subCommands) {
            LiteralArgumentBuilder<CommandSourceStack> literal = Commands.literal(sub.name())
                    .requires(source -> sub.permission().isEmpty() || source.getSender().hasPermission(sub.permission()));
            root.then(sub.build(literal));
        }

        return root.build();
    }

    private void sendHelp(CommandSender sender) {
        messageService.send(sender, LangKeys.COMMAND_HELP_HEADER);
        for (SubCommand sub : subCommands) {
            if (sub.permission().isEmpty() || sender.hasPermission(sub.permission()))
                messageService.send(sender, sub.usage());
        }
    }

    public List<SubCommand> subCommands() {
        return subCommands;
    }
}
