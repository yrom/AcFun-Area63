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

package tv.acfun.a63.base;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.umeng.analytics.MobclickAgent;

import tv.acfun.a63.AcApp;
import tv.acfun.a63.R;
import tv.acfun.a63.SettingsActivity;
import tv.acfun.a63.swipe.SwipeAppcompatActivity;
import tv.acfun.a63.util.ActionBarUtil;
import tv.acfun.a63.util.Theme;

/**
 * @author Yrom
 *
 */
public class BaseActivity extends SwipeAppcompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActionBarUtil.compatibleDeviceWithSB(this);
        Theme.onActivityCreate(this, savedInstanceState);
        super.onCreate(savedInstanceState);
    }
    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        setViratorEnable(AcApp.isViratorEnabled());
    }
    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            scrollToFinishActivity();
            return true;
        case R.id.action_settings:
            SettingsActivity.start(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
