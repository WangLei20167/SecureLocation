package utils;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * Created by Administrator on 2017/9/16 0016.
 */

public class PermissionUtils {
    /**
     * 检查指定权限是否开启
     * @param context
     * @param permissionName
     * @return
     */
    public static boolean checkPermission(Context context,String permissionName){
        PackageManager pm = context.getPackageManager();
        boolean permission = (PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission(permissionName, context.getPackageName()));
        if (permission) {
            return true;
        }else {
            return false;
        }
    }
}
