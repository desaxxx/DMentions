package org.nandayo.dmentions.user;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface ModuleData extends ConfigurationSerializable {

    @NotNull String id();
    default int getDataVersion() {
        return 1;
    }
}
