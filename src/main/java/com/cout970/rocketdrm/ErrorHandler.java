/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  net.minecraft.launchwrapper.Launch
 *  org.apache.commons.lang3.text.WordUtils
 */
package com.cout970.rocketdrm;

import com.cout970.rocketdrm.JniUtil;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.JOptionPane;
import net.minecraft.launchwrapper.Launch;
import org.apache.commons.lang3.text.WordUtils;

public class ErrorHandler {
    public static void showError(String error) {
        if (error == null) {
            error = "Internal error";
        }
        String info = WordUtils.wrap((String)("[Error] " + error), (int)80);
        String msg = "================================================================================\nThe client has encountered and error and will be closed.\n\n" + info + "\n\nIf you believe this is a bug contact the developers.\n================================================================================\n";
        System.err.println("\n" + msg);
        JOptionPane.showMessageDialog(null, msg);
        Map drm_inject = (Map)Launch.blackboard.get("DRM-InjectData");
        List handlers = (List)drm_inject.get("error-handlers");
        if (handlers != null) {
            /*for (Object handler : handlers) {
                try {
                    handler.accept(error);
                }
                catch (Throwable e) {
                    e.printStackTrace();
                }
            }
             */
        }
        //JniUtil.hardCrash(); Funny Stuff.
    }

    public static void showCrash(Thread th, Throwable exception) {
        Throwable original = exception;
        while (original.getCause() != null) {
            original = original.getCause();
        }
        exception.printStackTrace();
        String msg = original.getMessage();
        if (msg == null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            original.printStackTrace(new PrintStream(stream));
            msg = stream.toString().split("\n", 2)[0];
        }
        ErrorHandler.showError(th + " " + msg);
    }
}

