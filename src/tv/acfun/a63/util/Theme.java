/*
 * Copyright (C) 2013 YROM.NET
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

import tv.acfun.a63.AcApp;
import tv.acfun.a63.R;
import android.app.Activity;
import android.os.Bundle;

/**
 * @author Yrom
 *
 */
public class Theme {
    
    public static int getCurrentThemeId(){
        boolean isNightMode = AcApp.getConfig().getBoolean("is_night_mode", false);
        return isNightMode? R.style.AppTheme_Night : R.style.AppTheme;
    }

    public static void onActivityCreate(Activity activity, Bundle savedInstanceState) {
        activity.setTheme(getCurrentThemeId());
    }
}
