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

import java.io.UnsupportedEncodingException;

import org.apache.commons.httpclient.Cookie;

import tv.acfun.a63.api.Constants;
import tv.acfun.a63.api.entity.User;
import tv.acfun.a63.util.UsingCookiesRequest;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

import com.actionbarsherlock.app.SherlockActivity;
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
    private Listener<Profile> listener = new Listener<ProfileActivity.Profile>() {

        @Override
        public void onResponse(Profile response) {
            // TODO Auto-generated method stub
            Log.d(TAG, "on response");
        }
    };
    private ErrorListener errorListner = new ErrorListener() {

        @Override
        public void onErrorResponse(VolleyError error) {
            // TODO Auto-generated method stub
            Log.e(TAG, "VolleyError", error);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUser = AcApp.getUser();
        mWeb = new WebView(this);
        mWeb.getSettings();
        Cookie[] cookies = JSON.parseObject(mUser.cookies, Cookie[].class);
        AcApp.addRequest(new ProfileRequest(cookies, listener, errorListner));
    }
    private class ProfileRequest extends UsingCookiesRequest<Profile>{

        public ProfileRequest(Cookie[] cookies,
                Listener<Profile> listener, ErrorListener errorListner) {
            super(Constants.URL_PROFILE, cookies, Profile.class, listener, errorListner);
        }

        @Override
        protected Response<Profile> parseNetworkResponse(NetworkResponse response) {
            String json;
            try {
                json = new String(response.data,
                        HttpHeaderParser.parseCharset(response.headers));
                return Response.success(JSON.parseObject(json, Profile.class),
                        HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException e) {
                return Response.error(new ParseError(e));
            } catch (Exception e){
                return Response.error(new ParseError(e));
            }
            
        }
        
    }
    /**
     * {
          "uid": 458354,
          "sign": "哦呵呵",
          "username": "yrom",
          "sextrend": -1,
          "email": "80*@qq.com",
          "regTime": 1357034245000,
          "gender": true,
          "blog": "http://www.yrom.net",
          "success": true,
          "userImg": "http://w5cdn.ranktv.cn/dotnet/artemis/u/cms/www/201308/04173814xr8l.jpg"
        }
     * @author Yrom
     *
     */
    public static class Profile{
        public int uid;
        public String sign;
        public String username;
        public String email;
        public long regTime;
        public boolean gender;
        public String blog;
        public String userImg;
    }
}
