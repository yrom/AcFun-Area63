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
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

/**
 * using fastjson lib to parse json string 
 * @author Yrom
 *
 */
public class FastJsonRequest<T> extends CustomUARequest<T> {
    
    public FastJsonRequest(int method, String url, Class<T> clazz,Listener<T> listener, ErrorListener errorListner) {
        super(method, url,null, clazz,listener, errorListner);
        
    }
    
    public FastJsonRequest(String url, Class<T> clazz,Listener<T> listener, ErrorListener errorListner) {
        this(Method.GET, url, clazz, listener, errorListner);
    }
    
    public FastJsonRequest(String url,Map<String, String> postBody, Class<T> clazz,Listener<T> listener, ErrorListener errorListner) {
        super(url, postBody, clazz, listener, errorListner);
    }
    
    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(
                    response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(JSON.parseObject(json, mClazz),HttpHeaderParser.parseCacheHeaders(response));
        }catch (UnsupportedEncodingException e) {
            String json = new String(response.data);
            return Response.success(JSON.parseObject(json, mClazz),HttpHeaderParser.parseCacheHeaders(response));
        } catch(Exception e){
            return Response.error(new ParseError(e));
        }
    }

}
