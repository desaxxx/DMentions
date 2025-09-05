package org.nandayo.dmentions.provider;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nandayo.dmentions.DMentions;

public interface VanishProvider {

    static VanishProvider get() {
        return DMentions.inst().getVanishProvider();
    }

    boolean isVanished(Player player);


    enum Type {
        AUTO("auto"),
        ESSENTIALS("essentials"),
        STAFFPLUSPLUS("staff++");

        private final String configurationKey;
        Type(String configurationKey) {
            this.configurationKey = configurationKey;
        }


        @NotNull
        public static Type find(String configurationKey) {
            for (Type type : Type.values()) {
                if (type.configurationKey.equals(configurationKey)) {
                    return type;
                }
            }
            return AUTO;
        }
    }
}
