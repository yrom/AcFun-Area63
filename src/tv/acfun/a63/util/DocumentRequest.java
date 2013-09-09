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

import org.apache.commons.httpclient.Cookie;
import org.jsoup.nodes.Document;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;


/**
 * parsing html document from Internet
 * @author Yrom
 *
 */
public abstract class DocumentRequest extends UsingCookiesRequest<Document> {
    
    public DocumentRequest(String url, Cookie[] cookies, Listener<Document> listener,
            ErrorListener errorListner) {
        super(url, cookies, Document.class, listener, errorListner);
    }

    @Override
    protected Response<Document> parseNetworkResponse(NetworkResponse response) {
        String html;
        try {
            html = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            return Response.success(parse(html),HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            html = new String(response.data);
            return Response.success(parse(html),HttpHeaderParser.parseCacheHeaders(response));
        } catch (Exception e){
            return Response.error(new ParseError(e));
        }
    }
    protected abstract Document parse(String htmlFromNet);
}
