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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.text.TextUtils;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

/**
 * @author Yrom
 *
 */
@SuppressWarnings("deprecation")
public class SettingsActivity extends SherlockPreferenceActivity implements OnPreferenceClickListener, OnPreferenceChangeListener {
    private static final String KEY_IMAGE_CACHE = "image_cache";
    private static final String KEY_CLEAR_CACHE = "clear_cache";
    private String oldPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getListView().setFooterDividersEnabled(false);
        ActionBarUtil.setXiaomiFilterDisplayOptions(getSupportActionBar(), false);
        addPreferencesFromResource(R.xml.preferences);
        Preference cache = findPreference(KEY_CLEAR_CACHE);
        String size = FileUtil.getFormatFolderSize(getExternalCacheDir());
        cache.setSummary(size);
        cache.setOnPreferenceClickListener(this);
        
        savePath = (EditTextPreference) findPreference(KEY_IMAGE_CACHE);
        savePath.setPersistent(true);
        String defaultPath = AcApp.getDefaultImageSaveDir();
        savePath.setDefaultValue(defaultPath);
        if(TextUtils.isEmpty(savePath.getText())){
            savePath.setText(defaultPath);
            oldPath = defaultPath;
        }else
            oldPath = savePath.getText();
        savePath.setSummary(savePath.getText());
        savePath.setOnPreferenceChangeListener(this);
    }
    public static void start(Context context){
        context.startActivity(new Intent(context, SettingsActivity.class));
    }
    @Override
    public boolean onPreferenceClick(Preference preference) {
        if(KEY_CLEAR_CACHE.equals(preference.getKey())){
            preference.setEnabled(false);
            if(FileUtil.deleteFiles(getExternalCacheDir()))
                preference.setSummary("清除完毕");
            else
                Toast.makeText(getApplicationContext(), "清除失败", 0).show();
            return true;
        }
        return false;
    }
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if(KEY_IMAGE_CACHE.equals(preference.getKey())){
            if(FileUtil.validate(newValue.toString())){
                preference.setSummary(newValue.toString());
                showPathChangeDialog();
                return true;
            }else{
                showPathInvalidateDialog();
                return false;
            }
        }
        return false;
    }
    OnClickListener listener = new OnClickListener() {
        
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if(which==DialogInterface.BUTTON_POSITIVE){
                FileUtil.move(oldPath,savePath.getText());
            }
        }
    };

    private EditTextPreference savePath;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            break;

        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }
    void showPathChangeDialog(){
        new AlertDialog.Builder(this)
            .setTitle("位置已改变")
            .setMessage("是否将原有缓存迁移到新的位置？")
            .setPositiveButton("是", listener)
            .setNegativeButton("否",null)
            .show();
    }
    void showPathInvalidateDialog(){
        new AlertDialog.Builder(this)
            .setTitle("位置无效")
            .setMessage("请重新输入！")
            .setNegativeButton("好",null)
            .show();
    }
}
