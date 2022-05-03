/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  net.minecraft.launchwrapper.Launch
 *  net.minecraftforge.fml.relauncher.IFMLLoadingPlugin
 *  net.minecraftforge.fml.relauncher.IFMLLoadingPlugin$MCVersion
 *  net.minecraftforge.fml.relauncher.IFMLLoadingPlugin$Name
 *  net.minecraftforge.fml.relauncher.IFMLLoadingPlugin$SortingIndex
 *  net.minecraftforge.fml.relauncher.IFMLLoadingPlugin$TransformerExclusions
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.cout970.rocketdrm;

import com.cout970.rocketdrm.ErrorHandler;
import com.cout970.rocketdrm.JniUtil;
import com.cout970.rocketdrm.ReplaceClassLoader;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@IFMLLoadingPlugin.MCVersion(value="1.12.2")
@IFMLLoadingPlugin.Name(value="Rocket DRM")
@IFMLLoadingPlugin.SortingIndex(value=0x7FFFFFFF)
@IFMLLoadingPlugin.TransformerExclusions(value={"com.cout970.rocketdrm", "com.sun.jna"})
public class RocketDRM
implements IFMLLoadingPlugin {
    public static final Logger LOGGER = LogManager.getLogger((String)"Rocket DRM");
    private static boolean init = false;

    public RocketDRM() {
        if (init) {
            return;
        }
        init = true;
        RocketDRM.log("+----------------------------------------------------------------------------------------------------------------------+\n+         _______      ___      ______  ___  ____   ________  _________     ______   _______     ____    ____          +\n+        |_   __ #   .'   `.  .' ___  ||_  ||_  _| |_   __  ||  _   _  |   |_   _ `.|_   __ #   |_   #  /   _|         +\n+          | |__) | /  .-.  #/ .'   #_|  | |_/ /     | |_ #_||_/ | | #_|     | | `. # | |__) |    |   #/   |           +\n+          |  __ /  | |   | || |         |  __'.     |  _| _     | |         | |  | | |  __ /     | |#  /| |           +\n+         _| |  # #_#  `-'  /# `.___.'# _| |  # #_  _| |__/ |   _| |_       _| |_.' /_| |  # #_  _| |_#/_| |_          +\n+        |____| |___|`.___.'  `.____ .'|____||____||________|  |_____|     |______.'|____| |___||_____||_____|         +\n+                                                                                                                      +\n+----------------------------------------------------------------------------------------------------------------------+\n".replace('#', '\\'));
        this.debugClassloaders();
        System.setProperty("jna.tmpdir", new File("tmp/").getAbsolutePath());
        ReplaceClassLoader.inject(Launch.classLoader);
        File jar_location = this.setupJarLocation();
        Thread.setDefaultUncaughtExceptionHandler(ErrorHandler::showCrash);
        JniUtil.onInit();
        this.fixClassloader(jar_location);
        try {
            LOGGER.info("Preloading mixin services");
            Class.forName("org.spongepowered.asm.service.IMixinServiceBootstrap", true, Launch.classLoader.getClass().getClassLoader());
            Class.forName("org.spongepowered.asm.service.IMixinService", true, Launch.classLoader.getClass().getClassLoader());
            Class.forName("org.spongepowered.asm.service.MixinService", true, Launch.classLoader.getClass().getClassLoader());
            Class.forName("org.spongepowered.asm.service.mojang.MixinServiceLaunchWrapperBootstrap", true, Launch.classLoader.getClass().getClassLoader());
            Class.forName("org.spongepowered.asm.launch.MixinBootstrap", true, Launch.classLoader.getClass().getClassLoader());
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
        RocketDRM.log("+----------------------------------------------------------------------------------------------------------------------+\n+                                                         DONE                                                         +\n+----------------------------------------------------------------------------------------------------------------------+\n");
    }

    public static void log(String str) {
        for (String s : str.split("\n")) {
            LOGGER.info(s);
        }
    }

    private void debugClassloaders() {
        LOGGER.info("RocketDRM classloader: " + this.getClass().getClassLoader());
        LOGGER.info("Launch classloader: " + (Object)Launch.classLoader);
        ClassLoader appClassLoader = Launch.classLoader.getClass().getClassLoader();
        LOGGER.info("App classloader: " + appClassLoader);
        try {
            Class<?> mixinTweakerClass = Class.forName("org.spongepowered.asm.launch.MixinTweaker", false, appClassLoader);
            LOGGER.info("Mixin classloader: " + mixinTweakerClass.getClassLoader());
        }
        catch (Throwable e) {
            LOGGER.info("Mixin classloader: not found");
        }
        LOGGER.info("Working dir: " + new File(".").getAbsolutePath());
    }

    private void fixClassloader(File jar_location) {
        ClassLoader rootClassLoader = ClassLoader.getSystemClassLoader();
        if (rootClassLoader instanceof URLClassLoader && jar_location != null) {
            URLClassLoader loader = (URLClassLoader)rootClassLoader;
            List<URL> list = Arrays.asList(loader.getURLs());
            try {
                URL drm_uri = jar_location.toURI().toURL();
                if (!list.contains(drm_uri)) {
                    Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                    method.setAccessible(true);
                    method.invoke(rootClassLoader, drm_uri);
                }
            }
            catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    private File setupJarLocation() {
        File home = Launch.minecraftHome != null ? Launch.minecraftHome : new File(".");
        File mods = new File(home, "mods");
        File[] files = Objects.requireNonNull(mods.listFiles());
        HashMap<String, String> map = new HashMap<String, String>();
        boolean found = false;
        File result = null;
        for (File file : files) {
            if (!file.getName().endsWith(".jar")) continue;
            try {
                Attributes mainAttributes;
                JarFile jar = new JarFile(file);
                if (jar.getManifest() == null || !Objects.equals((mainAttributes = jar.getManifest().getMainAttributes()).getValue("FMLCorePlugin"), "com.cout970.rocketdrm.RocketDRM")) continue;
                result = file.getCanonicalFile();
                map.put("RocketJar", result.getAbsolutePath());
                found = true;
                break;
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        if (!found) {
            try {
                map.put("RocketJar", "");
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        Launch.blackboard.put("DRM-InjectData", map);
        return result;
    }

    public String[] getASMTransformerClass() {
        return new String[0];
    }

    public String getModContainerClass() {
        return null;
    }

    public String getSetupClass() {
        return null;
    }

    public void injectData(Map<String, Object> map) {
        HashMap inject_data = (HashMap)Launch.blackboard.get("DRM-InjectData");
        inject_data.putAll(map);
        JniUtil.onGameInit();
    }

    public String getAccessTransformerClass() {
        return null;
    }
}

