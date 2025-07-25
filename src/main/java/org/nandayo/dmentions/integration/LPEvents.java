package org.nandayo.dmentions.integration;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeClearEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.nandayo.dmentions.DMentions;

public class LPEvents {

    private final DMentions plugin;
    private final LuckPerms api;

    public LPEvents(DMentions plugin, LuckPerms api) {
        this.plugin = plugin;
        this.api = api;
    }

    public void register() {
        EventBus eventBus = api.getEventBus();
        eventBus.subscribe(plugin, NodeAddEvent.class, this::onAdd);
        eventBus.subscribe(plugin, NodeRemoveEvent.class, this::onRemove);
        eventBus.subscribe(plugin, NodeClearEvent.class, this::onClear);
    }

    private void onAdd(NodeAddEvent event) {
        if(!event.isUser()) return;
        User user = (User) event.getTarget();

        Bukkit.getScheduler().runTask(plugin, () -> {
            if(event.getNode() instanceof InheritanceNode) {
                Player player = Bukkit.getPlayer(user.getUniqueId());
                if(player == null) {
                    return;
                }
                LP.updatePlayerGroupCache(player);
            }
        });
    }

    private void onRemove(NodeRemoveEvent event) {
        if(!event.isUser()) return;
        User user = (User) event.getTarget();

        Bukkit.getScheduler().runTask(plugin, () -> {
            if(event.getNode() instanceof InheritanceNode) {
                Player player = Bukkit.getPlayer(user.getUniqueId());
                if(player == null) {
                    return;
                }
                LP.updatePlayerGroupCache(player);
            }
        });
    }

    private void onClear(NodeClearEvent event) {
        if(!event.isUser()) return;
        User user = (User) event.getTarget();

        Bukkit.getScheduler().runTask(plugin, () -> {
            Player player = Bukkit.getPlayer(user.getUniqueId());
            if(player == null) {
                return;
            }
            LP.updatePlayerGroupCache(player);
        });
    }
}
