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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.httpclient.Cookie;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import tv.acfun.a63.api.Constants;
import tv.acfun.a63.api.entity.User;
import tv.acfun.a63.util.ActionBarUtil;
import tv.acfun.a63.util.DocumentRequest;
import tv.acfun.a63.util.UsingCookiesRequest;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.alibaba.fastjson.JSON;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

/**
 * @author Yrom
 *
 */
public class ProfileActivity extends SherlockActivity {

    
    protected static final String TAG = "ProfileActivity";
    private WebView mWeb;
    private User mUser;
    private Listener<Document> mSplashListener = new Listener<Document>() {

        @Override
        public void onResponse(Document response) {
            // TODO Auto-generated method stub
            String data = response.html();
            mWeb.loadDataWithBaseURL(Constants.URL_BASE, data, "text/html", "utf-8", null);
        }
        
    }; 
    private ErrorListener mErrorListner = new ErrorListener() {

        @Override
        public void onErrorResponse(VolleyError error) {
            // TODO Auto-generated method stub
            Log.e(TAG, "VolleyError", error);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBarUtil.setXiaomiFilterDisplayOptions(getSupportActionBar(), false);
        getSupportActionBar().setTitle("个人信息");
        mUser = AcApp.getUser();
        Cookie[] cookies = JSON.parseObject(mUser.cookies, Cookie[].class);
//        AcApp.addRequest(new ProfileRequest(cookies, mProfileListener, mErrorListner));
        setContentView(R.layout.activity_article);
        mWeb = (WebView) findViewById(R.id.webview);
        mWeb.getSettings().setJavaScriptEnabled(true);
        AcApp.addRequest(new SplashDocumentRequest(cookies, mSplashListener, mErrorListner));
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        
        
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    
    
    private class SplashDocumentRequest extends DocumentRequest{

        public SplashDocumentRequest(Cookie[] cookies, Listener<Document> listener,
                ErrorListener errorListner) {
            super(Constants.URL_SPLAH, cookies, listener, errorListner);
        }

        @Override
        protected Document parse(String htmlFromNet) {
            try {
                InputStream in = getAssets().open("splash.html");
                Document doc = Jsoup.parse(in, "utf-8","");
                doc.getElementById("area-cont-splash").html(htmlFromNet);
                doc.getElementsByClass("alert-info").remove();
                doc.getElementById("hint-unread-splash").remove();
                return doc;
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            return null;
        }
        
    }
}
