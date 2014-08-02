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

import tv.acfun.a63.service.PushService;
import tv.acfun.a63.util.ActionBarUtil;
import tv.acfun.a63.util.FileUtil;
import tv.acfun.a63.util.Theme;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;

/**
 * @author Yrom
 * 
 */
@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity implements
        OnPreferenceClickListener, OnPreferenceChangeListener {
    private static final String KEY_UPDATE = "update";
    private static final String KEY_FEED_BACK = "feedback";
    private static final String KEY_IMAGE_CACHE = "image_cache";
    private static final String KEY_CLEAR_CACHE = "clear_cache";
    private static final String KEY_INTERVAL = "mention_interval";
    private static final String KEY_MENTION_ENABLE = "mention_enable";
    private static final String KEY_MENTION_WIFI_ONLY = "mention_enable_wifi_only";
    private static final String KEY_RATING = "rating";
    private String oldPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Theme.onActivityCreate(this, savedInstanceState);
        super.onCreate(savedInstanceState);
        getListView().setFooterDividersEnabled(false);
//        ActionBarUtil.setXiaomiFilterDisplayOptions(getActionBar(), false);
        addPreferencesFromResource(R.xml.preferences);
        setCache();
        Preference feedback = findPreference(KEY_FEED_BACK);
        feedback.setOnPreferenceClickListener(this);

        Preference update = findPreference(KEY_UPDATE);
        update.setSummary(AcApp.instance().getVersionName());
        update.setOnPreferenceClickListener(this);
        
        Preference rating = findPreference(KEY_RATING);
        rating.setOnPreferenceClickListener(this);
        findPreference(KEY_INTERVAL).setOnPreferenceChangeListener(this);
        findPreference(KEY_MENTION_ENABLE).setOnPreferenceChangeListener(this);
        findPreference(KEY_MENTION_WIFI_ONLY).setOnPreferenceChangeListener(this);
    }

    private void setCache() {
        Preference cache = findPreference(KEY_CLEAR_CACHE);
        String size = FileUtil.getFormatFolderSize(getExternalCacheDir());
        cache.setSummary(size);
        cache.setOnPreferenceClickListener(this);

        savePath = (EditTextPreference) findPreference(KEY_IMAGE_CACHE);
        savePath.setPersistent(true);
        String defaultPath = AcApp.getDefaultImageSaveDir();
        savePath.setDefaultValue(defaultPath);
        if (TextUtils.isEmpty(savePath.getText())) {
            savePath.setText(defaultPath);
            oldPath = defaultPath;
        } else
            oldPath = savePath.getText();
        savePath.setSummary(savePath.getText());
        savePath.setOnPreferenceChangeListener(this);
    }

    public static void start(Context context) {
        context.startActivity(new Intent(context, SettingsActivity.class));
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (KEY_CLEAR_CACHE.equals(preference.getKey())) {
            preference.setEnabled(false);
            if (FileUtil.deleteFiles(getExternalCacheDir()))
                preference.setSummary("清除完毕");
            else
                Toast.makeText(getApplicationContext(), "清除失败", 0).show();
            return true;
        } else if (KEY_FEED_BACK.equals(preference.getKey())) {
            // new FeedbackAgent(this).startFeedbackActivity();
            startActivity(new Intent(this, ConversationActivity.class));
        } else if (KEY_UPDATE.equals(preference.getKey())) {
            preference.setEnabled(false);
            update();
        } else if(KEY_RATING.equals(preference.getKey())){
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            if(ActionBarUtil.hasSB()){
                intent.setData(Uri.parse("mstore:http://app.meizu.com/phone/apps/3ccc35d9e3364b749df34d425c45667e"));
                try {
                    startActivity(intent);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            intent.setData(Uri.parse("market://details?id=tv.acfun.a63"));
            try {
                startActivity(intent);
                return true;
            } catch (Exception e) {
            }
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=tv.acfun.a63"));
            startActivity(intent);
            return true;
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (KEY_IMAGE_CACHE.equals(preference.getKey())) {
            if (FileUtil.validate(newValue.toString())) {
                preference.setSummary(newValue.toString());
                showPathChangeDialog();
                return true;
            } else {
                showPathInvalidateDialog();
                return false;
            }
        } else if (KEY_INTERVAL.equals(preference.getKey())
                || KEY_MENTION_ENABLE.equals(preference.getKey())
                || KEY_MENTION_WIFI_ONLY.equals(preference.getKey())) {

            PushService.start(this); // 重启服务
            return true;
        }
        return false;
    }

    private void update() {
        UmengUpdateAgent.setUpdateAutoPopup(false);
        UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {

            @Override
            public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
                switch (updateStatus) {
                case 0: // has update
                    UmengUpdateAgent.showUpdateDialog(SettingsActivity.this, updateInfo);
                    break;
                case 1: // has no update
                    Toast.makeText(SettingsActivity.this, "已是最新版", Toast.LENGTH_SHORT).show();
                    break;
                case 2: // none wifi
                    Toast.makeText(SettingsActivity.this, "没有wifi连接， 只在wifi下更新", Toast.LENGTH_SHORT)
                            .show();
                    break;
                case 3: // time out
                    Toast.makeText(SettingsActivity.this, "超时", Toast.LENGTH_SHORT).show();
                    break;
                }
            }

        });
        UmengUpdateAgent.forceUpdate(this);
    }

    protected void onDestroy() {
        super.onDestroy();
        UmengUpdateAgent.setUpdateListener(null);
        UmengUpdateAgent.setDownloadListener(null);
        UmengUpdateAgent.setDialogListener(null);
        UmengUpdateAgent.setUpdateAutoPopup(true);
    }

    OnClickListener listener = new OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                FileUtil.move(oldPath, savePath.getText());
            }
        }
    };

    private EditTextPreference savePath;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
//            scrollToFinishActivity();
            finish();
            break;

        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    void showPathChangeDialog() {
        new AlertDialog.Builder(this).setTitle("位置已改变").setMessage("是否将原有缓存迁移到新的位置？")
                .setPositiveButton("是", listener).setNegativeButton("否", null).show();
    }

    void showPathInvalidateDialog() {
        new AlertDialog.Builder(this).setTitle("位置无效").setMessage("请重新输入！")
                .setNegativeButton("好", null).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
