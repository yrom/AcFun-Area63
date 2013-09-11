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

package tv.acfun.a63;

import tv.acfun.a63.util.ActionBarUtil;
import tv.acfun.a63.util.FileUtil;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

/**
 * @author Yrom
 *
 */
@SuppressWarnings("deprecation")
public class SettingsActivity extends SherlockPreferenceActivity implements OnPreferenceClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getListView().setFooterDividersEnabled(false);
        ActionBarUtil.setXiaomiFilterDisplayOptions(getSupportActionBar(), false);
        addPreferencesFromResource(R.xml.preferences);
        Preference cache = findPreference("clear_cache");
        String size = FileUtil.getFormatFolderSize(getExternalCacheDir());
        cache.setSummary(size);
        cache.setOnPreferenceClickListener(this);
        
    }
    public static void start(Context context){
        context.startActivity(new Intent(context, SettingsActivity.class));
    }
    @Override
    public boolean onPreferenceClick(Preference preference) {
        // TODO Auto-generated method stub
        if("clear_cache".equals(preference.getKey())){
            preference.setEnabled(false);
            if(FileUtil.deleteFiles(getExternalCacheDir()))
                preference.setSummary("清除完毕");
            else
                Toast.makeText(getApplicationContext(), "清除失败", 0).show();
            return true;
        }
        return false;
    }
}
