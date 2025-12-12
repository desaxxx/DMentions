package org.nandayo.dmentions.module;

import org.jetbrains.annotations.ApiStatus;
import org.nandayo.dapi.util.Util;
import org.nandayo.dmentions.DMentions;
import org.nandayo.dmentions.user.MentionUser;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * @since 1.9
 */
public class ModuleManager {
    public static final ModuleManager INSTANCE = new ModuleManager();
    private ModuleManager() {}

    private final List<BaseModule> loadedModules = new ArrayList<>();
    private URLClassLoader moduleClassLoader;

    @ApiStatus.Internal
    public void callLogin(MentionUser user) {
        for (BaseModule module : loadedModules) {
            try {
                module.onUserLogin(user);
            } catch (Exception e) {
                Util.log("&cFailed to call login for user " + user.getUuid() + " from module " + module.getClass().getSimpleName() + ", skipping. " + e);
            }
        }
    }

    @ApiStatus.Internal
    public void callLogout(MentionUser user) {
        for (BaseModule module : loadedModules) {
            try {
                module.onUserLogout(user);
            } catch (Exception e) {
                Util.log("6cFailed to call logout for user " + user.getUuid() + " from module " + module.getClass().getSimpleName() + ", skipping. " + e);
            }
        }
    }



    @ApiStatus.Internal
    public void loadModules() {
        File modulesDir = new File(DMentions.inst().getDataFolder(), "modules");
        if (!modulesDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            modulesDir.mkdirs();
        }

        File[] jarFiles = modulesDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jarFiles == null || jarFiles.length == 0) {
            return;
        }

        List<URL> urls = new ArrayList<>();
        for (File jarFile : jarFiles) {
            try {
                urls.add(jarFile.toURI().toURL());
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
            } catch (Exception e) {
                Util.log("Failed to enable module: " + module.getClass().getSimpleName() + ", skipping. " + e);
            }
        }
    }

    @ApiStatus.Internal
    public void unloadModules() {
        for (BaseModule module : loadedModules) {
            try {
                module.onDisable();
            } catch (Exception e) {
                Util.log("Error disabling module " + module.getClass().getSimpleName() + ", skipping. " + e);
            }
        }
        loadedModules.clear();
    }
}
