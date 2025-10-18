package org.nandayo.dmentions.user;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@ApiStatus.Experimental
abstract class AbstractModuleDataHolder {

    private final @NotNull Map<String, ModuleData> moduleDataMap = new HashMap<>();
    public AbstractModuleDataHolder() {}

    @NotNull
    public Set<String> getModuleDataIds() {
        return Collections.unmodifiableSet(moduleDataMap.keySet());
    }

    public ModuleData getModuleData(String moduleId) {
        return moduleDataMap.get(moduleId);
    }

    public <T extends ModuleData> T getModuleData(String moduleId, Class<T> dataClass) {
        ModuleData data = moduleDataMap.get(moduleId);
        if (dataClass.isInstance(data)) {
            return dataClass.cast(data);
        }
        return null;
    }

    public void setModuleData(String moduleId, ModuleData data) {
        moduleDataMap.put(moduleId, data);
    }

    public boolean hasModuleData(String moduleId) {
        return moduleDataMap.containsKey(moduleId);
    }

    public void removeModuleData(String moduleId) {
        moduleDataMap.remove(moduleId);
    }

    @NotNull
    public <T extends ModuleData> T getOrCreateModuleData(String moduleId, Class<T> dataClass, Supplier<T> factory) {
        T existing = getModuleData(moduleId, dataClass);
        if (existing != null) {
            return existing;
        }

        T newData = factory.get();
        setModuleData(moduleId, newData);
        return newData;
    }
}
