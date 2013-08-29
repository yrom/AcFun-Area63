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

import java.io.UnsupportedEncodingException;

import com.alibaba.fastjson.JSON;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

/**
 * @author Yrom
 *
 */
public class FastJsonRequest<T> extends Request<T> {
    protected final Class<T> clazz;
    private final Listener<T> listener;
    
    public FastJsonRequest(int method, String url, Class<T> clazz,Listener<T> listener, ErrorListener errorListner) {
        super(method, url, errorListner);
        this.clazz = clazz;
        this.listener = listener;
        
    }
    public FastJsonRequest(String url, Class<T> clazz,Listener<T> listener, ErrorListener errorListner) {
        this(Method.GET, url, clazz, listener, errorListner);
    }
    /* (non-Javadoc)
     * @see com.android.volley.Request#parseNetworkResponse(com.android.volley.NetworkResponse)
     */
    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(
                    response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(JSON.parseObject(json, clazz),HttpHeaderParser.parseCacheHeaders(response));
        }catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }

    /* (non-Javadoc)
     * @see com.android.volley.Request#deliverResponse(java.lang.Object)
     */
    @Override
    protected void deliverResponse(T response) {
        // TODO Auto-generated method stub
        listener.onResponse(response);
    }
    
}
