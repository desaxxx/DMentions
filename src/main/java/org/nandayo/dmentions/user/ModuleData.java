package org.nandayo.dmentions.user;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Experimental
public interface ModuleData extends ConfigurationSerializable {

    @NotNull String id();
    default int getDataVersion() {
        return 1;
    }
}
