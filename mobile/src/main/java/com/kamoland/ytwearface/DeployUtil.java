package com.kamoland.ytwearface;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class DeployUtil {
    public static boolean isDebuggable(Context ctx) {
        PackageManager manager = ctx.getPackageManager();
        ApplicationInfo appInfo = null;
        try {
            appInfo = manager.getApplicationInfo(ctx.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        if ((appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) == ApplicationInfo.FLAG_DEBUGGABLE)
            return true;
        return false;
    }

    public static boolean isExistRequireYtGold(Context context) {
        return  DeployUtil.isVersionAppExists(context, "com.kamoland.ytlog_g", 435) ||
                DeployUtil.isVersionAppExists(context, "com.kamoland.ytlog_gau", 200);
    }

    public static boolean isVersionAppExists(Context ctx, String packname, int requireVersion) {
        PackageManager manager = ctx.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(packname, 0);
            int ver = info.versionCode;
            if (ver >= requireVersion) {
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return false;
    }
}
