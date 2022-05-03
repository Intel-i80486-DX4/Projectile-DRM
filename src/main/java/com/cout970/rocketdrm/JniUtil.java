/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  com.sun.jna.Native
 */
package com.cout970.rocketdrm;

import com.sun.jna.Native;
import java.io.File;
import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class JniUtil {
    private JniUtil() {
    }

    public static native void onInit();

    public static native void onPostInit(String var0);

    public static native void onGameInit();

    public static native void onClassLoad(String var0);

    public static native byte[] onMixinLoad(String var0);

    public static native String decodeString(String var0);

    public static native int decodeInt(String var0, int var1, int var2);

    public static native long decodeLong(String var0, long var1, long var3);

    public static native CallSite decodeInvoke(MethodHandles.Lookup var0, String var1, MethodType var2);

    public static void hardCrash() {
        /*
        try {
            Class<?> clazz = Class.forName("sun.misc.Unsafe");
            Field theUnsafe = clazz.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            Object unsafe = theUnsafe.get(null);
            clazz.getDeclaredMethod("putAddress", Long.TYPE, Long.TYPE).invoke(unsafe, 0L, 0L);
        }
        catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
         */
    }

    static {
        try {
            File file = Native.extractFromResourcePath((String)"jna_rocket_drm");
            System.load(file.getAbsolutePath());
        }
        catch (Exception e) {
            e.printStackTrace();
            JniUtil.hardCrash();
        }
    }
}

