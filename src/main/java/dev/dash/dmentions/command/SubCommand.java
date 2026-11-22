package dev.dash.dmentions.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dash.dmentions.message.Notification;
import dev.yoshi.xconfig.ConfigKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

public interface SubCommand {

    String name();

    default String permission() {
        return "dmentions.command." + name();
    }

    default PermissionDefault permissionDefault() {
        return PermissionDefault.OP;
    }

    ConfigKey<List<Notification>> usage();

    ArgumentBuilder<CommandSourceStack, ?> build(LiteralArgumentBuilder<CommandSourceStack> literal);
}