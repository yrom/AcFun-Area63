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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import tv.acfun.a63.api.ArticleApi;
import tv.acfun.a63.api.entity.Article;
import tv.acfun.a63.api.entity.Article.SubContent;
import tv.acfun.a63.util.ActionBarUtil;
import tv.acfun.a63.util.Connectivity;
import tv.acfun.a63.util.FileUtil;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

/**
 * 文章页
 * 结构：
 * {@code
    <div id="title">
    <h1 class="article-title"></h1>
        <div id="info" class="article-info">
          <span class="article-publisher"><i class="icon-slash"></i></span>
          <span class="article-pubdate"></span>
          <span class="article-category"></span>
        </div>
    </div>
    <section id="content" class="article-body"></section>
    }
 * 
 * @author Yrom
 * 
 */
public class ArticleActivity extends SherlockActivity implements Listener<Article>, ErrorListener {
    private static String ARTICLE_PATH ;
    public static void start(Context context, int aid) {
        Intent intent = new Intent(context, ArticleActivity.class);
        intent.putExtra("aid", aid);
        context.startActivity(intent);
    }

    private Request<?> request;
    private Document mDoc;
    private List<String> imgUrls;  
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        ARTICLE_PATH = AcApp.getExternalCacheDir("article").getAbsolutePath();
        ActionBarUtil.setXiaomiFilterDisplayOptions(getSupportActionBar(), false);
        int aid = getIntent().getIntExtra("aid", 0);
        if (aid == 0) {

        } else {
            initData(aid);
            getSupportActionBar().setTitle("ac"+aid);
            setSupportProgressBarIndeterminateVisibility(true);
//            mWeb = new WebView(this);
//            mWeb.setPadding(0, 0, 0, 0);
            setContentView(R.layout.activity_article);
            mWeb = (WebView) findViewById(R.id.webview);
            mWeb.getSettings().setAllowFileAccess(true);
            mWeb.getSettings().setAppCachePath(ARTICLE_PATH);
            // TODO 无图模式
            mWeb.getSettings().setBlockNetworkImage(true);
            mWeb.getSettings().setJavaScriptEnabled(true);
            mWeb.addJavascriptInterface(new ACJSObject(), "AC");
            mWeb.setWebChromeClient(new WebChromeClient() {

                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    // TODO Auto-generated method stub
                    super.onProgressChanged(view, newProgress);
                }

                @Override
                public void onCloseWindow(WebView window) {
                    // TODO Auto-generated method stub
                    super.onCloseWindow(window);
                }

            });
            mWeb.setWebViewClient(new WebViewClient() {

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    // TODO Auto-generated method stub
                    return super.shouldOverrideUrlLoading(view, url);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    // TODO Auto-generated method stub
                    if(imgUrls == null && imgUrls.isEmpty())
                        return;
                    Log.i(TAG, "on finished:"+url);
                    String[] arr = new String[imgUrls.size()];
                    new DownloadImageTask().execute(imgUrls.toArray(arr));
                }
                

            });
            mWeb.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
            mWeb.getSettings().setUserAgentString(Connectivity.UA);
            // web.loadUrl("file:///android_asset/article.html", null);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initData(int aid) {
        request = new ArticleRequest(aid, this, this);
        request.setTag(TAG);
        AcApp.addRequest(request);

    }
    private String buildTitle(Article article){
        StringBuilder builder = new StringBuilder();
        builder.append("<h1 class=\"article-title\">")
            .append(article.title).append("</h1>")
            .append("<div id=\"info\" class=\"article-info\">")
            .append("<span class=\"article-publisher\"><img id=\"icon\" src=\"file:///android_asset/wen2.png\" width='18px' height='18px'/> ")
            .append("<a href=\"http://www.acfun.tv/member/user.aspx?uid=").append(article.poster.id).append("\" >")
            .append(article.poster.name)
            .append("</a>")
            .append("</span>")
            .append("<span class=\"article-pubdate\">")
            .append(getPubDate(article.postTime))
            .append("发布于</span>")
            .append("<span class=\"article-category\">")
            .append(article.channelName)
            .append("</span>")
            .append("</div>")
            
            
            ;
        
        return builder.toString();
    }
    static final long _1_min = 60 * 1000;
    static final long _1_hour = 60 * _1_min;
    static final long _24_hour = 24 * _1_hour;
    private String getPubDate(long postTime) {
        long delta = System.currentTimeMillis() - postTime;
        if( delta <  _24_hour && delta >= _1_hour){
            int time = (int) (delta / _1_hour);
            return time+"小时前";
        } else if( delta < _1_hour && delta >= _1_min){
            int time = (int) (delta / _1_min);
            return time+"分钟前";
        } else if( delta < _1_min){
            return "半分钟前";
        } else {
            int time = (int) (delta / _24_hour);
            if(time <= 6){
                return time+"天前" ;
            }else{
                return AcApp.getDateTime(postTime);
            }
        }
    }

    private static final String TAG = "Article";
    private Article mArticle;
    private WebView mWeb;

    static class ArticleRequest extends Request<Article> {
        Listener<Article> mListener;

        public ArticleRequest(int aid, Listener<Article> listener, ErrorListener errListener) {
            super(Method.GET, ArticleApi.getContentUrl(aid), errListener);
            this.mListener = listener;
        }

        @Override
        protected Response<Article> parseNetworkResponse(NetworkResponse response) {
            try {
                String json = new String(response.data,
                        HttpHeaderParser.parseCharset(response.headers));
                return Response.success(Article.newArticle(new JSONObject(json)),
                        HttpHeaderParser.parseCacheHeaders(response));
            } catch (Exception e) {
                Log.e(TAG, "parse article error", e);
                return Response.error(new ParseError(e));
            }
        }

        @Override
        protected void deliverResponse(Article response) {
            mListener.onResponse(response);

        }

        @Override
        public Map<String, String> getHeaders() {
            return Connectivity.UA_MAP;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AcApp.cancelAllRequest(TAG);
    }

    @Override
    public void onResponse(Article response) {
        mArticle = response;
        imgUrls = response.imgUrls;
        new BuildDocTask().execute(mArticle);
        

    }

    @Override
    public void onErrorResponse(VolleyError error) {
        // TODO Auto-generated method stub
        AcApp.showToast("加载失败");
    }
    
    private class BuildDocTask extends AsyncTask<Article, Void, Boolean>{
        boolean hasUseMap;
        @Override
        protected Boolean doInBackground(Article... params) {
            try {
                InputStream in = getAssets().open("article.html");
                mDoc = Jsoup.parse(in, "utf-8", "");
                if(imgUrls !=null)
                    imgUrls.clear();
                else
                    imgUrls = new ArrayList<String>();
                Element title = mDoc.getElementById("title");
                title.append(buildTitle(params[0]));
                Element content = mDoc.getElementById("content");
                
                ArrayList<SubContent> contents = params[0].contents;
                
                for(int i=0;i<contents.size();i++){
                    SubContent sub = contents.get(i);
                    
                    if(!params[0].title.equals(sub.subTitle)){
                        content.append("<h2 class=\"article-subtitle\">"+sub.subTitle +"</h2><hr>");
                    }
                    content.append(sub.content);
                    Elements imgs = content.select("img");
                    if(imgs.hasAttr("usemap")){
                        hasUseMap = true;
                    }else{
                        for(int imgIndex=0;imgIndex<imgs.size();imgIndex++){
                            Element img = imgs.get(imgIndex);
                            String src = img.attr("src");
                            imgUrls.add(src); // 
                            img.attr("org", src);
                            img.attr("src","file:///android_asset/loading.gif");
                            img.attr("loc",FileUtil.getLocalFileUri(new File(AcApp.getExternalCacheDir(AcApp.IMAGE),FileUtil.getName(src))).toString());
                            // 给 img 标签加上点击事件
                            try {
                                if ("icon".equals(img.attr("class"))
                                        || Integer.parseInt(img.attr("width")) < 100
                                        || Integer.parseInt(img.attr("height")) < 100) {
                                    continue;
                                }
                            } catch (Exception e) {
                            }
                            if (src.contains("emotion/images/"))
                                continue;
                            // 统一宽度
                            img.attr("width", "90%");
                            // 过滤掉图片的url跳转
                            if (img.parent() != null
                                    && img.parent().tagName().equalsIgnoreCase("a")) {
                                img.parent().attr("href",
                                        "javascript:window.AC.viewImage('" + src + "');");
                            } else {
                                img.attr("onclick", "javascript:window.AC.viewImage(this.src);");
                            }
                        }
                        
                        
                    }
                }
                
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            if(result){
                setSupportProgressBarIndeterminateVisibility(false);
                mWeb.loadDataWithBaseURL("http://www.acfun.tv/", mDoc.html(), "text/html", "UTF-8", null);
                if(hasUseMap)
                    mWeb.getSettings().setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS);
                else
                    mWeb.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
                
            }else{
                mWeb.loadData("<h1>加载失败请重试！</h1>", "text/html", "utf-8");
            }
        }
        
        
    };
    /**
     * 异步下载图片到缓存目录
     * @author Yrom
     *
     */
    private class DownloadImageTask extends AsyncTask<String, Integer, Void>{

        private int timeoutMs = 3000;

        @Override
        protected Void doInBackground(String... params) {
            // TODO Auto-generated method stub
            for(int index=0;index<params.length;index++){
                String url = params[index];
                if(!url.startsWith("http")){
                    url = "http://www.acfun.tv"+url;
                }
                File cache = new File(AcApp.getExternalCacheDir(AcApp.IMAGE),FileUtil.getName(url));
                if(cache.exists() && cache.canRead()){
                    publishProgress(index);
                    continue;
                }else{
                    try {
                        cache.getParentFile().mkdirs();
                        cache.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    
                }
                InputStream in = null;
                OutputStream out = null;
                try {
                    URL parsedUrl = new URL(url);
                    HttpURLConnection connection = (HttpURLConnection) parsedUrl.openConnection();
                    connection.setConnectTimeout(timeoutMs);
                    connection.setReadTimeout(timeoutMs*2);
                    connection.setUseCaches(false);
                    int responseCode = connection.getResponseCode();
                    if (responseCode == 200) {
                        in = connection.getInputStream();
                        out = new FileOutputStream(cache);
                        FileUtil.copyStream(in,out);
                        publishProgress(index);
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (in != null)
                            in.close();
                    } catch (IOException e) {
                    }
                    try {
                        if (out != null)
                            out.close();
                    } catch (IOException e) {
                    }
                }
            
            }
            return null;
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            if(imgUrls != null){
                String url = imgUrls.get(values[0]);
                if(url== null) return;
                Log.i(TAG, url+"cached");
                mWeb.loadUrl("javascript:(function(){"
                        + "var images = document.getElementsByTagName(\"img\"); "
                        + "var imgSrc = images[" + values[0] + "].getAttribute(\"org\"); "
                        + "if(imgSrc == \"" + url + "\"){ " 
                        +   "images[" + values[0]+ "].setAttribute(\"src\",images[" + values[0]+"].getAttribute(\"loc\"));}"
                        + "})()"); 
            }
        }
        @Override  
        protected void onPostExecute(Void result) {  
            //确保所有图片都顺利的显示出来  
            mWeb.loadUrl("javascript:(function(){"
                    + "var images = document.getElementsByTagName(\"img\"); "
                    + "for(var i=0;i<images.length;i++){"
                    +   "var imgSrc = images[i].getAttribute(\"loc\"); "
                    +   "images[i].setAttribute(\"src\",imgSrc);"
                    + "}"
                    + "})()");
        }  
        
    }
    class ACJSObject{
        @android.webkit.JavascriptInterface
        public void viewcomment(){
            AcApp.showToast("查看评论: ac%d",mArticle.id);
        }
        @android.webkit.JavascriptInterface
        public void viewImage(String url){
            AcApp.showToast("查看图片: url=%s",url);
        }
    }
}
