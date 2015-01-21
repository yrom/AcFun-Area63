package tv.acfun.a63.util;

import tv.acfun.a63.AcApp;
import android.content.Context;

public class DensityUtil {
    public static int dip2px(Context context, float dipValue) {
        if(AcApp.density == 1f){
            AcApp.density  = context.getResources().getDisplayMetrics().density;
        }
        return (int) (dipValue * AcApp.density + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        if(AcApp.density == 1f){
            AcApp.density  = context.getResources().getDisplayMetrics().density;
        }
        return (int) (pxValue / AcApp.density + 0.5f);
    }

}
