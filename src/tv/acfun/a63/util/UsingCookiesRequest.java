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

import java.util.Map;

import org.apache.commons.httpclient.Cookie;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

/**
 * @author Yrom
 *
 */
public abstract class UsingCookiesRequest<T> extends CustomUARequest<T> {
    private static final String HEADER_COOKIE = "Cookie";
    private Cookie[] mCookies;
    
    public UsingCookiesRequest(String url, Map<String, String> requestBody, Cookie[] cookies, Class<T> clazz,
            Listener<T> listener, ErrorListener errorListner) {
        super(url, requestBody, clazz, listener, errorListner);
        mCookies = cookies;
    }

    public UsingCookiesRequest(String url, Cookie[] cookies, Class<T> clazz, Listener<T> listener,
            ErrorListener errorListner) {
        this(url, null,cookies, clazz, listener, errorListner);
    }
    
    @Override
    public Map<String, String> getHeaders() {
        Map<String, String> headers = super.getHeaders();
        
        StringBuilder builder = new StringBuilder();
        for(int i=0;i<mCookies.length;i++){
            Cookie cookie = mCookies[i];
            builder.append(cookie.getName())
                .append("=")
                .append(cookie.getValue());
            if(i < mCookies.length -1)
                builder.append("; ");
        }
        headers.put(HEADER_COOKIE, builder.toString());
        return headers;
    }
    
}
