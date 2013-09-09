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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

/**
 * use custom ua ({@link Connectivity#UA_MAP}) in http request headers
 * @author Yrom
 *
 */
public abstract class CustomUARequest<T> extends Request<T> {
    protected final Class<T> mClazz;
    private final Listener<T> mListener;
    protected final Map<String, String> mPostBody;
    
    /**
     * @param method
     * @param url
     * @param requestBody the form data to post,
     * @param clazz
     * @param listener
     * @param errorListner
     */
    public CustomUARequest(int method, String url, Map<String, String> requestBody,Class<T> clazz,Listener<T> listener,ErrorListener errorListner) {
        super(method, url, errorListner);
        this.mClazz = clazz;
        this.mListener = listener;
        mPostBody = requestBody;
    }

    public CustomUARequest(String url, Map<String, String> requestBody, Class<T> clazz,Listener<T> listener, ErrorListener errorListner) {
        this(requestBody == null? Method.GET:Method.POST, url, requestBody, clazz, listener, errorListner);
    }
    
    public CustomUARequest(String url, Class<T> clazz,Listener<T> listener, ErrorListener errorListner){
        this(url,null,clazz,listener,errorListner);
    }
    
    @Override
    protected final void deliverResponse(T response) {
        this.mListener.onResponse(response);
    }
    
    @Override
    public Map<String, String> getHeaders() {
        return Connectivity.UA_MAP;
    }
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return mPostBody;
    }
}
