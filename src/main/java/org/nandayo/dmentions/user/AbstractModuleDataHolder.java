package org.nandayo.dmentions.user;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings("unused")
abstract class AbstractModuleDataHolder {

    private final @NotNull Map<String, ModuleData> moduleDataMap = new HashMap<>();
    public AbstractModuleDataHolder() {}

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
