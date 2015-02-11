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

import java.io.IOException;
import tv.acfun.a63.BuildConfig;
import tv.acfun.a63.R;
import tv.acfun.a63.util.ActionBarUtil;
import tv.acfun.a63.util.Connectivity;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.ValueCallback;
import android.webkit.WebView;

/**
 * @author Yrom
 * 
 */
@SuppressLint("SetJavaScriptEnabled")
@TargetApi(19)
public class BaseWebViewActivity extends BaseActivity {
    protected WebView mWeb;
    private View mProgress;
    
    @Override
    protected void onPause() {
        super.onPause();
        mWeb.pauseTimers();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        mWeb.resumeTimers();
    }
    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBarUtil.setXiaomiFilterDisplayOptions(getSupportActionBar(), false);
        setContentView(R.layout.activity_article);
        mProgress = findViewById(R.id.loading);
        mWeb = (WebView) findViewById(R.id.webview);
        mWeb.getSettings().setAllowFileAccess(true);
        mWeb.getSettings().setJavaScriptEnabled(true);
        mWeb.getSettings().setUserAgentString(Connectivity.UA);
        mWeb.getSettings().setUseWideViewPort(true);
        mWeb.getSettings().setLoadWithOverviewMode(true);
        /*
         * fixed issues #12
         * http://stackoverflow.com/questions/9476151/webview-flashing-with-white-background-if-hardware-acceleration-is-enabled-an
         */
        if(Build.VERSION.SDK_INT >= 11)
            mWeb.setBackgroundColor(Color.argb(1, 0, 0, 0));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);
        }
        initView(savedInstanceState);
        initData();
    }

    protected DialogInterface.OnClickListener mErrorDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            if (which == DialogInterface.BUTTON_POSITIVE) {
                initData();
            } else {
                finish();
            }
        }
    };

    protected void showErrorDialog() {
        try {
            Drawable icon = Drawable.createFromStream(getAssets().open("emotion/ais/27.gif"), "27.gif");
            icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
            new AlertDialog.Builder(this).setTitle("加载失败！").setIcon(icon).setMessage("是否重试？").setPositiveButton("重试", mErrorDialogListener)
                    .setNegativeButton("算了", mErrorDialogListener).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void initView(Bundle savedInstanceState) {}

    protected void initData() {
        setSupportProgressBarIndeterminateVisibility(true);
    }
    @Override
    public void setSupportProgressBarIndeterminateVisibility(boolean visible) {
        mProgress.setVisibility(visible?View.VISIBLE:View.GONE);
        mWeb.setVisibility(visible?View.GONE:View.VISIBLE);
    }
    /**
     * 
     * @param script
     *            the JavaScript to execute.
     * @param resultCallback
     *            A callback to be invoked when the script execution completes
     *            with the result of the execution (if any). May be null if no
     *            notificaion of the result is required.
     */
    public void evaluateJavascript(String script, ValueCallback<String> resultCallback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                mWeb.evaluateJavascript(script, resultCallback);
                return;
            } catch (Exception ignored) {
            }
        }
        mWeb.loadUrl(script);
    }
}
