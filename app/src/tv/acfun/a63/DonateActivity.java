package tv.acfun.a63;

import java.io.IOException;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import tv.acfun.a63.base.BaseWebViewActivity;

public class DonateActivity extends BaseWebViewActivity {
    
    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        mWeb.setWebChromeClient(new WebChromeClient());
        mWeb.setWebViewClient(new WebViewClient(){
            
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(uri);
                if("alipayqr".equals(uri.getScheme())){
                    try {
                        startActivity(intent);
                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                        showErrorDialog();
                    }
                    return true;
                }else if(url.startsWith("http") && url.endsWith(".apk")){
                    startActivity(intent);
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                setSupportProgressBarIndeterminateVisibility(false);
            }
        });
    }
    
    @Override
    protected void initData() {
        super.initData();
        mWeb.loadUrl(getIntent().getDataString());
    }
    
    protected void showErrorDialog() {
        try {
            Drawable icon = Drawable.createFromStream(getAssets().open("emotion/ais/38.gif"), "38.gif");
            icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
            new AlertDialog.Builder(this)
                .setTitle("你的好意我心领了！")
                .setIcon(icon)
                .setMessage("仅接受支付宝捐助。。可是你都没有安装支付宝钱包=.=")
                .setPositiveButton("知道了", null)
                .show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
