/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  net.minecraft.launchwrapper.Launch
 *  net.minecraft.launchwrapper.LaunchClassLoader
 */
package com.cout970.rocketdrm;

import com.cout970.rocketdrm.JniUtil;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.Map;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

public class ReplaceClassLoader
extends URLClassLoader {
    private final Map<String, Class<?>> cachedClasses;

    private ReplaceClassLoader(URLClassLoader original, Map<String, Class<?>> cachedClasses) {
        super(original.getURLs(), (ClassLoader)original);
        this.cachedClasses = cachedClasses;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (name.startsWith("com.cout970.rocketdrm.")) {
            return super.loadClass(name);
        }
        JniUtil.onClassLoad(name);
        if (this.cachedClasses.containsKey(name)) {
            return this.cachedClasses.get(name);
        }
        return super.loadClass(name);
    }

    public static byte[] onMixinLoad(String name) {
        try {
            Class<?> clazz = Class.forName("com.cout970.rocketdrm.JniUtil", true, (ClassLoader)Launch.classLoader);
            Method onMixinLoad = clazz.getDeclaredMethod("onMixinLoad", String.class);
            onMixinLoad.setAccessible(true);
            Object result = onMixinLoad.invoke(null, name);
            return (byte[])result;
        }
        catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    public static void inject(LaunchClassLoader loader) {
        try {
            Field parent = LaunchClassLoader.class.getDeclaredField("parent");
            parent.setAccessible(true);
            URLClassLoader original = (URLClassLoader)parent.get((Object)loader);
            Field cache = LaunchClassLoader.class.getDeclaredField("cachedClasses");
            cache.setAccessible(true);
            Map cachedClasses = (Map)cache.get((Object)loader);
            ReplaceClassLoader newLoader = new ReplaceClassLoader(original, cachedClasses);
            parent.set((Object)loader, newLoader);
        }
        catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}

