/*
 * Copyright (C) 2013 Yrom Wang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tv.acfun.a63.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import tv.acfun.a63.R;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.view.ViewConfiguration;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;

/**
 * Action Bar 相关工具类
 * 
 * @author Yrom
 * 
 */
public final class ActionBarUtil {
    /**
     * 强制显示“溢出”菜单（三条杠）
     * 
     * @param context
     */
    public static void forceShowActionBarOverflowMenu(Context context) {
        try {
            ViewConfiguration config = ViewConfiguration.get(context);
            Field menuKeyField = ViewConfiguration.class
                    .getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ignored) {
        }
    }

    public static void addSearchView(Activity activity, Menu menu) {
        SearchView searchView = new SearchView(activity);
        searchView.setSubmitButtonEnabled(true);
        SearchManager searchManager = (SearchManager) activity
                .getSystemService(Context.SEARCH_SERVICE);
        SearchableInfo info = searchManager.getSearchableInfo(activity
                .getComponentName());
        searchView.setSearchableInfo(info);
        menu.add("Search")
                .setIcon(R.drawable.action_search)
                .setActionView(searchView)
                .setShowAsAction(
                        MenuItem.SHOW_AS_ACTION_IF_ROOM
                                | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

    }
    /**
     * 过滤掉小米设备。。其只显示title
     * @param bar
     * @param showHome 是否显示home icon
     */
    public static void setXiaomiFilterDisplayOptions(ActionBar bar, boolean showHome) {
        boolean isXiaomi = "Xiaomi".equalsIgnoreCase(Build.MANUFACTURER);
        boolean isJB = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
        bar.setDisplayOptions(isXiaomi && !showHome && !isJB? ActionBar.DISPLAY_SHOW_TITLE
                | ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_USE_LOGO: ActionBar.DISPLAY_SHOW_TITLE
                | ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_USE_LOGO);
    }
    /**
     * 是否有SB
     * @return 
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static boolean hasSB(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            return false;
        try {
            Method method = Build.class.getDeclaredMethod("hasSmartBar");
            return ((Boolean) method.invoke(null)).booleanValue();
        } catch (Exception e) {
            // ignore
        }
        
        return Build.DEVICE.equalsIgnoreCase("mx2") || Build.DEVICE.equalsIgnoreCase("mx3");
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static void compatibleDeviceWithSB(Activity activity){
        if(hasSB()){
            activity.getWindow().setUiOptions(ActivityInfo.UIOPTION_SPLIT_ACTION_BAR_WHEN_NARROW); 
        }
    }
}
