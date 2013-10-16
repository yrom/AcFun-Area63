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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.httpclient.Cookie;

import tv.acfun.a63.adapter.MentionsAdapter;
import tv.acfun.a63.api.ArticleApi;
import tv.acfun.a63.api.entity.Comment;
import tv.acfun.a63.api.entity.Content;
import tv.acfun.a63.api.entity.Mentions;
import tv.acfun.a63.base.BaseActivity;
import tv.acfun.a63.util.ActionBarUtil;
import tv.acfun.a63.util.ArrayUtil;
import tv.acfun.a63.util.Connectivity;
import tv.acfun.a63.util.UsingCookiesRequest;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.umeng.analytics.MobclickAgent;

/**
 * 召唤
 * @author Yrom
 *
 */
public class MentionActivity extends BaseActivity implements OnClickListener, OnItemClickListener {
    
    private static final String TAG = "Mentions";
    private ListView mList;
    private ProgressBar mLoadingBar;
    private TextView mTimeOutText;
    private View mFootview;
    private MentionsAdapter mAdapter;
    private SparseArray<Comment> data = new SparseArray<Comment>();
    private List<Content> contentList = new ArrayList<Content>();
    private List<Integer> commentIdList = new ArrayList<Integer>();
    private boolean isloading;
    private boolean shouldReload;
    private Cookie[] mCookies;
    private boolean hasNextPage;
    private PullToRefreshListView mPtr;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.fragment_main_dummy);
        MobclickAgent.onEvent(this, "view_mentions");
        ActionBar ab = getSupportActionBar();
        ActionBarUtil.setXiaomiFilterDisplayOptions(ab, false);
        ab.setTitle(R.string.mentions);
        initList();
        if(AcApp.getUser()!=null){
            mCookies = JSON.parseObject(AcApp.getUser().cookies, Cookie[].class);
            
        }else{
            mLoadingBar.setVisibility(View.GONE);
            mTimeOutText.setVisibility(View.VISIBLE);
            mTimeOutText.setText(R.string.sign_in_first);
            mPtr.setVisibility(View.GONE);
        }
        MobclickAgent.onEvent(this, "view_mentions");
        requestData(1, true);
    }
    private void initList() {
        mPtr = (PullToRefreshListView)findViewById(R.id.list);
        mPtr.setOnRefreshListener(new OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                if(!isloading){
                    pageIndex = 1;
                    requestData(pageIndex,true);
                }
            }
        });
        mPtr.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

            @Override
            public void onLastItemVisible() {
                if(hasNextPage){
                    if(!isloading){
                        requestData(++pageIndex,false);
                    }
                } else{
                    mFootview.findViewById(R.id.list_footview_progress).setVisibility(View.GONE);
                    ((TextView)mFootview.findViewById(R.id.list_footview_text)).setText(R.string.no_more);
                }
            }
        });
        mList = mPtr.getRefreshableView();
        mLoadingBar = (ProgressBar) findViewById(R.id.loading);
        mTimeOutText = (TextView) findViewById(R.id.time_out_text);
        mTimeOutText.setText(R.string.time_out);
        mTimeOutText.setOnClickListener(this);
        mFootview = LayoutInflater.from(this).inflate(R.layout.list_footerview, mList, false);
        mList.setVisibility(View.INVISIBLE);
        mList.addFooterView(mFootview);
        mFootview.setClickable(false);
        mList.setFooterDividersEnabled(false);
        mList.setOnItemClickListener(this);
        mAdapter = new MentionsAdapter(this, contentList, data, commentIdList);
        mList.setAdapter(mAdapter);
    }
    @Override
    public void onClick(View v) {
        if(mFootview == v){
            if(shouldReload){
                mFootview.findViewById(R.id.list_footview_progress).setVisibility(View.VISIBLE);
                ((TextView)mFootview.findViewById(R.id.list_footview_text)).setText(R.string.loading);
                requestData(pageIndex, false);
            }
        }else if(mTimeOutText == v){
            if(AcApp.getUser()!=null){
                pageIndex = 1;
                requestData(pageIndex, true);
            }
        }
    }
    private void requestData(int page, boolean requestNewData) {
        if (requestNewData) {
            mTimeOutText.setVisibility(View.GONE);
            if(mAdapter == null || mAdapter.isEmpty()) 
                mLoadingBar.setVisibility(View.VISIBLE);
        }
        isloading = true;
        Request<?> request = new MentionsRequest(page, mCookies, listener, error);
        request.setShouldCache(true);
        request.setTag(TAG);
        AcApp.addRequest(request);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        AcApp.cancelAllRequest(TAG);
    }
    private Listener<Mentions> listener = new Listener<Mentions>() {

        @Override
        public void onResponse(Mentions response) {
            isloading = false;
            mPtr.onRefreshComplete();
            if (response.totalCount == 0) {
                mLoadingBar.setVisibility(View.GONE);
                mTimeOutText.setVisibility(View.VISIBLE);
                mList.setVisibility(View.GONE);
                Drawable drawable = getResources().getDrawable(R.drawable.ac_16);
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                mTimeOutText.setCompoundDrawables(drawable, null, null, null);
                mTimeOutText.setText(R.string.no_more);
                return;
            }

            if (response.page == 1) {
                if (mAdapter != null)
                    mAdapter.notifyDataSetInvalidated();
                data.clear();
                commentIdList.clear();
                contentList.clear();
                mLoadingBar.setVisibility(View.GONE);
                mList.setVisibility(View.VISIBLE);
            }
            ArrayUtil.putAll(response.commentArr, data);
            commentIdList.addAll(ArrayUtil.asList(response.commentList));
            contentList.addAll(response.contentList);
            hasNextPage = response.nextPage > response.page;
            if (data != null && data.size() > 0) {
                mAdapter.setData(contentList, data, commentIdList);
                shouldReload = false;
            }
        }
    };
    protected int pageIndex;
    private ErrorListener error = new ErrorListener() {

        @Override
        public void onErrorResponse(VolleyError error) {
            if (pageIndex > 1) {
                shouldReload = true;
                mFootview.findViewById(R.id.list_footview_progress).setVisibility(View.GONE);
                TextView textview = (TextView) mFootview.findViewById(R.id.list_footview_text);
                textview.setText(R.string.reloading);
            } else {
                mLoadingBar.setVisibility(View.GONE);
                mTimeOutText.setVisibility(View.VISIBLE);
                mList.setVisibility(View.VISIBLE);
            }
        }
        
    };

    public static class MentionsRequest extends UsingCookiesRequest<Mentions>{
      
        public MentionsRequest(int page, Cookie[] cookies, Listener<Mentions> listener,
                ErrorListener errorListner) {
            super(ArticleApi.getMentionsUrl(10,page), cookies, Mentions.class, listener, errorListner);
        }
        
        @Override
        protected Response<Mentions> parseNetworkResponse(NetworkResponse response) {
            try {
                String json = new String(response.data,
                        HttpHeaderParser.parseCharset(response.headers));
                JSONObject parseObject = JSON.parseObject(json);
                Mentions comments = JSON.toJavaObject(parseObject, Mentions.class);
                JSONObject commentContentArr = parseObject.getJSONObject("commentContentArr");
                comments.commentArr = parseContentAttr(commentContentArr);
                return Response.success(comments, cache(response));
            } catch (Exception e) {
                Log.e(TAG, "parse mentions error", e);
                return Response.error(new ParseError(e));
            }
        }
        
        private SparseArray<Comment> parseContentAttr(JSONObject commentContentArr) {
            SparseArray<Comment> attr = new SparseArray<Comment>();
            for (Iterator<String> iterator = commentContentArr.keySet().iterator(); iterator
                    .hasNext();) {
                String key = iterator.next();
                JSONObject content = commentContentArr.getJSONObject(key);
                Comment comment = JSON.toJavaObject(content, Comment.class);
                attr.put(comment.cid, comment);
            }
            return attr;

        }
        
        private Cache.Entry cache(NetworkResponse response){
            return Connectivity.newCache(response,120);
        }
    }
    public static void start(Context context) {
        Intent intent = new Intent(context, MentionActivity.class);
        context.startActivity(intent);
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try{
            CommentsActivity.start(this, (int) parent.getItemIdAtPosition(position));
        }catch(IndexOutOfBoundsException e){
            onClick(mFootview);
        }
    }

}
