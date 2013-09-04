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

import tv.acfun.a63.adapter.CommentsAdaper;
import tv.acfun.a63.adapter.CommentsAdaper.OnQuoteClickListener;
import tv.acfun.a63.api.ArticleApi;
import tv.acfun.a63.api.entity.Comment;
import tv.acfun.a63.api.entity.Comments;
import tv.acfun.a63.util.ActionBarUtil;
import tv.acfun.a63.util.ArrayUtil;
import tv.acfun.a63.util.CustomUARequest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
public class CommentsActivity extends SherlockActivity implements OnClickListener, OnQuoteClickListener,Listener<Comments>,ErrorListener, OnItemClickListener{
    private static final String TAG = CommentsActivity.class.getSimpleName();
    private int aid;
    private InputMethodManager mKeyboard;
    private ListView mList;
    private ProgressBar mLoadingBar;
    private TextView mTimeOutText;
    private View mFootview;
    private CommentsAdaper mAdapter;
    private int pageIndex = 1;
    private int totalPage = 1;
    private boolean hasNextPage;
    private ImageButton mBtnSend;
    private EditText mCommentText;
    
    public static void start(Context context, int aid){
        Intent intent = new Intent(context, CommentsActivity.class);
        intent.putExtra("aid", aid);
        context.startActivity(intent);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        aid = getIntent().getIntExtra("aid",0);
        if(aid == 0) return;
        setContentView(R.layout.activity_comments);
        ActionBar ab = getSupportActionBar();
        ab.setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_bg_trans));
        mKeyboard = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        ActionBarUtil.setXiaomiFilterDisplayOptions(ab, false);
        ab.setTitle("ac"+aid+" / 评论");
        mBtnSend = (ImageButton) findViewById(R.id.comments_send_btn);
        mCommentText = (EditText) findViewById(R.id.comments_edit);
        mBtnSend.setOnClickListener(this);
        mList = (ListView) findViewById(android.R.id.list);
        mLoadingBar = (ProgressBar)findViewById(R.id.time_progress);
        mTimeOutText = (TextView)findViewById(R.id.time_out_text);
        mTimeOutText.setOnClickListener(this);
        mList.setVisibility(View.INVISIBLE);
        mList.setDivider(getResources().getDrawable(R.drawable.listview_divider));
        mList.setDividerHeight(2);
        mFootview = LayoutInflater.from(this).inflate(R.layout.list_footerview, mList, false);
        mFootview.setOnClickListener(this);
        mList.addFooterView(mFootview);
        mFootview.setClickable(false);
        mList.setFooterDividersEnabled(false);
        mList.setOnScrollListener(mScrollListener);
        mList.setOnItemClickListener(this);
        mAdapter = new CommentsAdaper(this, data, commentIdList);
        mAdapter.setOnClickListener(this);
        mList.setAdapter(mAdapter);
        requestData(1,true);
    }
    private void requestData(int page, boolean requestNewData) {
        // TODO Auto-generated method stub
        if(requestNewData){
            mTimeOutText.setVisibility(View.GONE);
            mLoadingBar.setVisibility(View.VISIBLE);
        }
        isloading = true;
        AcApp.addRequest(new CommentsRequest(aid, page, this, this));
    }
    OnScrollListener mScrollListener = new OnScrollListener(){

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                int totalItemCount) {
            if (view.getLastVisiblePosition() == (view.getCount() - 1)&&!isloading){
                if (!hasNextPage || pageIndex+1 > totalPage) {
                    mFootview.findViewById(R.id.list_footview_progress).setVisibility(View.GONE);
                    TextView textview = (TextView) mFootview.findViewById(R.id.list_footview_text);
                    textview.setText(R.string.no_more);
                } else {
                    pageIndex += 1;
                    requestData(pageIndex, false);
                }
            }
            
        }
        
    };
    static class CommentsRequest extends CustomUARequest<Comments> {

        public CommentsRequest(int aid, int page, Listener<Comments> listener, ErrorListener errListener) {
            super(ArticleApi.getCommentUrl(aid, page), Comments.class, listener, errListener);
        }

        @Override
        protected Response<Comments> parseNetworkResponse(NetworkResponse response) {
            try {
                String json = new String(response.data,
                        HttpHeaderParser.parseCharset(response.headers));
                JSONObject parseObject = JSON.parseObject(json);
                Comments comments = JSON.toJavaObject(parseObject, Comments.class);
                JSONObject commentContentArr = parseObject.getJSONObject("commentContentArr");
                comments.commentArr = parseContentAttr(commentContentArr);
                return Response.success(comments, HttpHeaderParser.parseCacheHeaders(response));
            } catch (Exception e) {
                Log.e(TAG, "parse article error", e);
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
    }
    
    SparseArray<Comment> data = new SparseArray<Comment>();
    List<Integer> commentIdList = new ArrayList<Integer>();
    private boolean isloading;
    private boolean isreload;

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
        case R.id.time_out_text:
            pageIndex = 1;
            requestData(pageIndex, true);
            break;
        }
    }

    @Override
    public void onClick(View v, int position) {
        mList.performItemClick(v, position, mAdapter.getItemId(position));
    }
    @Override
    public void onErrorResponse(VolleyError error) {
        if(pageIndex > 1){
            isreload = true;
            mFootview.findViewById(R.id.list_footview_progress).setVisibility(View.GONE);
            TextView textview = (TextView) mFootview.findViewById(R.id.list_footview_text);
            textview.setText(R.string.reloading);
        }else{
            mLoadingBar.setVisibility(View.GONE);
            mTimeOutText.setVisibility(View.VISIBLE);
            mList.setVisibility(View.INVISIBLE);
        }
    }
    @Override
    public void onResponse(Comments response) {
        Log.i(TAG, "on response");
        isloading = false;
        if(response.totalCount == 0){
            mLoadingBar.setVisibility(View.GONE);
            mTimeOutText.setVisibility(View.VISIBLE);
            mList.setVisibility(View.INVISIBLE);
            mTimeOutText.setText(R.string.no_comment_yet);
            return;
        }
        
        
        if(response.page == 1){
            data.clear();
            commentIdList.clear();
            mLoadingBar.setVisibility(View.GONE);
            mList.setVisibility(View.VISIBLE);
        }
        ArrayUtil.putAll(response.commentArr, data);
        commentIdList.addAll(ArrayUtil.asList(response.commentList));
        totalPage = response.totalPage;
        hasNextPage = response.nextPage > response.page;
        if(data!=null && data.size()>0){
            mAdapter.setData(data,commentIdList);
            mAdapter.notifyDataSetChanged();
            isreload = false;
        }
        
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(position == parent.getCount()-1){
            if(isreload){
                mFootview.findViewById(R.id.list_footview_progress).setVisibility(View.VISIBLE);
                TextView textview = (TextView) mFootview.findViewById(R.id.list_footview_text);
                textview.setText(R.string.loading);
                requestData(pageIndex, false);
            }
        }
            else{
            
            Comment c = (Comment) parent.getItemAtPosition(position);
            String pre = "re: #"+c.count+" ";
            mCommentText.setText(pre);
            mCommentText.setSelection(pre.length());
            view.postDelayed(new Runnable() {
                
                @Override
                public void run() {
                    mKeyboard.showSoftInput(mCommentText, 0);                    
                }
            }, 200);
            
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            this.finish();
            break;
        }
        return super.onOptionsItemSelected(item);
    }
}
