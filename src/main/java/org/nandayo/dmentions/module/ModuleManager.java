package org.nandayo.dmentions.module;

import org.nandayo.dapi.util.Util;
import org.nandayo.dmentions.DMentions;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class ModuleManager {
    public static final ModuleManager INSTANCE = new ModuleManager();
    private ModuleManager() {}

    private final List<BaseModule> loadedModules = new ArrayList<>();
    private URLClassLoader moduleClassLoader;

    public void loadModules() {
        File modulesDir = new File(DMentions.inst().getDataFolder(), "modules");
        if (!modulesDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            modulesDir.mkdirs();
        }

        File[] jarFiles = modulesDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jarFiles == null || jarFiles.length == 0) {
            Util.log("Debug, No modules found to load.");
            return;
        }

        List<URL> urls = new ArrayList<>();
        for (File jarFile : jarFiles) {
            try {
                urls.add(jarFile.toURI().toURL());
                Util.log("Debug, Found module JAR: " + jarFile.getName());
            } catch (Exception e) {
                Util.log("Could not get URL for module: " + jarFile.getName());
                e.printStackTrace();
            }
        }
        if (urls.isEmpty()) {
            return;
        }

        moduleClassLoader = new URLClassLoader(
                urls.toArray(new URL[0]),
                DMentions.inst().getClass().getClassLoader()
        );

        ServiceLoader<BaseModule> loader = ServiceLoader.load(BaseModule.class, moduleClassLoader);
        for (BaseModule module : loader) {
            try {
                module.onEnable();
                loadedModules.add(module);
                Util.log("Debug, Successfully loaded and enabled module: " + module.getClass().getSimpleName());
            } catch (Exception e) {
                Util.log("Failed to enable module: " + module.getClass().getSimpleName());
                e.printStackTrace();
            }
        }
    }

    public void unloadModules() {
        for (BaseModule module : loadedModules) {
            try {
                module.onDisable();
                Util.log("Debug, Disabled module: " + module.getClass().getSimpleName());
            } catch (Exception e) {
                Util.log("Error disabling module " + module.getClass().getSimpleName());
                e.printStackTrace();
            }
        }
        loadedModules.clear();
    }
}
