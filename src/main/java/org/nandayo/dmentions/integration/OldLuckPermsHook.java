//package org.nandayo.dmentions.integration;
//
//import net.luckperms.api.LuckPerms;
//import net.luckperms.api.model.group.Group;
//import net.luckperms.api.model.user.User;
//import net.luckperms.api.query.QueryOptions;
//import org.bukkit.Bukkit;
//import org.bukkit.entity.Player;
//import org.bukkit.plugin.Plugin;
//import org.bukkit.plugin.RegisteredServiceProvider;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//public class OldLuckPermsHook {
//    static private final Map<UUID, String> playerGroupCache = new HashMap<>();
//
//    private LuckPerms api = null;
//    private OldLuckPermsHook(LuckPerms api) {
//        this.api = api;
//    }
//    private OldLuckPermsHook() {}
//
//
//    static public OldLuckPermsHook createInstance() {
//        Plugin plugin = Bukkit.getPluginManager().getPlugin("LuckPerms");
//        if(plugin == null || !plugin.isEnabled()) return new OldLuckPermsHook();
//
//        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
//        if(provider == null) return new OldLuckPermsHook();
//
//        return new OldLuckPermsHook(provider.getProvider());
//    }
//
//    public boolean isHooked() {
//        return api != null;
//    }
//
//    public Optional<LuckPerms> getAPI() {
//        return Optional.ofNullable(api);
//    }
//
//    //GET GROUPS
//    public List<String> getGroups() {
//        if(!isHooked()) return new ArrayList<>();
//        return api.getGroupManager().getLoadedGroups().stream()
//                .map(Group::getName)
//                .collect(Collectors.toList());
//    }
//
//    //GET ONLINE GROUP MEMBERS
//    public Player[] getOnlinePlayersInGroup(String groupName) {
//        if (!isHooked() || groupName == null || groupName.isEmpty()) return new Player[0];
//        return Bukkit.getOnlinePlayers().stream()
//                .filter(player -> {
//                    User user = api.getUserManager().getUser(player.getUniqueId());
//                    if (user == null) return false;
//
//                    return getGroup(player).equals(groupName);
//                })
//                .toArray(Player[]::new);
//    }
//
//    //GET PLAYER PRIMARY GROUP
//    public String getGroup(Player player) {
//        String group = playerGroupCache.get(player.getUniqueId());
//        return group != null ? group : updatePlayerGroupCache(player);
//    }
//
//    /*
//     * CACHING THE PLAYER GROUP WITH UTMOST WEIGHT
//     */
//    public String updatePlayerGroupCache(Player player) {
//        String group = getHighestPriorityGroup(player);
//        return playerGroupCache.put(player.getUniqueId(), group);
//    }
//
//    @Nullable
//    private String getHighestPriorityGroup(Player player) {
//        if (!isHooked()) return null;
//        User user = api.getUserManager().getUser(player.getUniqueId());
//        if (user == null) return null;
//        QueryOptions queryOptions = api.getContextManager().getQueryOptions(user).orElse(null);
//        if (queryOptions == null) return null;
//
//        return user.getInheritedGroups(queryOptions).stream()
//                .max(Comparator.comparingInt(g -> g.getWeight().orElse(0)))
//                .map(Group::getName)
//                .orElse(user.getPrimaryGroup());
//    }
//}
