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
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;

import tv.acfun.a63.api.ArticleApi;
import tv.acfun.a63.base.BaseActivity;
import tv.acfun.a63.util.ActionBarUtil;
import tv.acfun.a63.util.BaseAnimationListener;
import tv.acfun.a63.util.Connectivity;
import tv.acfun.a63.util.FileUtil;
import tv.acfun.a63.view.MyViewPager;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.view.WindowCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.util.IOUtils;
import com.diegocarloslima.byakugallery.lib.TileBitmapDrawable;
import com.diegocarloslima.byakugallery.lib.TouchImageView;
import com.umeng.analytics.MobclickAgent;


/**
 * @author Yrom
 *
 */
public class ImagePagerActivity extends BaseActivity implements OnPageChangeListener {
    private static final String EXTRA_IMAGES = "images";
    private static final String EXTRA_INDEX = "index";
    private MyViewPager pager;
    private TextView indexText;

    public static void startCacheImage(Context context, ArrayList<File> flist, int index, int aid, String title){
        ArrayList<String> list = new ArrayList<String>(flist.size());
        for(File file : flist){
            list.add(Uri.fromFile(file).toString());
        }
        startNetworkImage(context, list, index, aid, title);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActionBarUtil.compatibleDeviceWithSB(this);
        requestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        ActionBarUtil.setXiaomiFilterDisplayOptions(getSupportActionBar(), false);
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_bg_trans));
        Bundle extras = getIntent().getExtras();
        title = extras.getString("title");
        getSupportActionBar().setTitle(title);
        aid = extras.getInt("aid");
        mList = extras.getStringArrayList(EXTRA_IMAGES);
        int index = extras.getInt(EXTRA_INDEX,0);
        MobclickAgent.onEvent(this, "view_big_pic");
        setContentView(R.layout.activity_images);
        
        indexText = (TextView) findViewById(R.id.index);
        if(ActionBarUtil.hasSB() && getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE){
            setInfoBottomMargin(getResources().getDimensionPixelSize(R.dimen.abc_action_bar_default_height));
        }
        pager = (MyViewPager) findViewById(R.id.pager);
        pager.setAdapter(new ImageAdapter(getSupportFragmentManager(),mList));
        pager.setOffscreenPageLimit(1);
        pager.setOnPageChangeListener(this);
        pager.setCurrentItem(index);
        onPageSelected(index);
    }
    private void setInfoBottomMargin(int height) {
        MarginLayoutParams params = (MarginLayoutParams) indexText.getLayoutParams();
        params.bottomMargin = height;
        indexText.setLayoutParams(params);
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(ActionBarUtil.hasSB() ){
            if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
                setInfoBottomMargin(0);
            }else if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
                setInfoBottomMargin(getResources().getDimensionPixelSize(R.dimen.abc_action_bar_default_height));
            }
        }
    }
    static class ImageAdapter extends FragmentStatePagerAdapter{
        ArrayList<String> list;
        
        ImageAdapter(FragmentManager fm, ArrayList<String> imgUrls) {
            super(fm);
            this.list = imgUrls;
        }
        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = new ImageFragment();
            Bundle args = new Bundle();
            args.putString(ImageFragment.ARG_IMAGE_URL, list.get(position));
            fragment.setArguments(args);
            return fragment;
        }
        
    }
    AnimationListener mHideListener = new BaseAnimationListener(){
        
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public void onAnimationEnd(Animation animation) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                indexText.setTranslationY(indexText.getHeight());
            indexText.setVisibility(View.GONE);
        }
    };
    AnimationListener mShowListener = new BaseAnimationListener(){
        
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public void onAnimationStart(Animation animation) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                indexText.setTranslationY(0);
            indexText.setVisibility(View.VISIBLE);
        }
    };
    void hideInfo(){
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_out);
        anim.setAnimationListener(mHideListener);
        indexText.startAnimation(anim);
    }
    void showInfo(){
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_in);
        anim.setAnimationListener(mShowListener);
        indexText.startAnimation(anim);
    }
    public static class ImageFragment extends Fragment{
        public static final String ARG_IMAGE_URL = "image_url";
        public static final String TAG = "ImagePaer.ImageFragment";
        private Uri mUri;
        private DownloadImageTask task;
        ProgressBar progress;
        TextView timeOut;
        ImagePagerActivity mContext;
        TouchImageView imageView;
        View.OnClickListener clicked = new View.OnClickListener() {
            
            @Override
            public void onClick(View view) {
                if(mContext.getSupportActionBar().isShowing()){
                    mContext.getSupportActionBar().hide();
                    mContext.hideInfo();
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                }else{
                    mContext.getSupportActionBar().show();
                    mContext.showInfo();
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                }
            }
        };
        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            mContext = (ImagePagerActivity) activity;
        }
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            String url = getArguments().getString(ARG_IMAGE_URL);
            Uri uri = Uri.parse(url);
            if(uri.getHost() == null){
                uri = Uri.parse("http://"+ArticleApi.getDomainRoot(mContext)+url);
            }
            mUri = uri;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_image, container,false);
            
            imageView = (TouchImageView)rootView.findViewById(R.id.image);
//            imageView.setMaxScale(3.0f);
            timeOut = (TextView)rootView.findViewById(R.id.time_out_text);
            progress = (ProgressBar) rootView.findViewById(R.id.loading);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB &&
                    !mContext.getSupportActionBar().isShowing()){
                imageView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
            }
            return rootView;
        }
        
        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            getImage();
        }
        private void getImage() {
            if("file".equals(mUri.getScheme())){
                File img = new File(mUri.getPath());
                if(img.exists()){
                    loadImage(img.getAbsolutePath());
                }else{
                    timeOut.setVisibility(View.VISIBLE);
                    timeOut.setText(R.string.no_data_need_retry);
                }
                
            }else{
                task = new DownloadImageTask();
                task.execute(mUri);
            }
        }

        private void loadImage(String path) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1)
                usingTileBitmap(path);
            else
                imageView.setImageDrawable(Drawable.createFromPath(path));
            imageView.setOnClickListener(clicked);
        }
        private void usingTileBitmap(String path) {
            TileBitmapDrawable.attachTileBitmapDrawable(imageView, path, null, new TileBitmapDrawable.OnInitializeListener() {
                @Override
                public void onStartInitialization() {
                    progress.setVisibility(View.VISIBLE);
                }

                @Override
                public void onEndInitialization() {
                    progress.setVisibility(View.GONE);
                    task = null;
                }
            });
        }
        private void onError() {
            progress.setVisibility(View.GONE);
            timeOut.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    v.setVisibility(View.GONE);
                    getImage();
                }
            });
            timeOut.setVisibility(View.VISIBLE);
        }
        
        @Override
        public void onDestroyView() {
            super.onDestroyView();
            if(task != null && task.loading && !task.isCancelled()){
                task.cancel(true);
            }
        }
        
        class DownloadImageTask extends AsyncTask<Uri, Void, String>{
            boolean loading;
            @Override
            protected void onPreExecute() {
                progress.setVisibility(View.VISIBLE);
            }
            @Override
            protected String doInBackground(Uri... params) {
                loading = true;
                Uri uri = params[0];
                if(uri == null || mUri.getHost() == null) return null;
                File cache = FileUtil.generateImageCacheFile(uri.toString());
                if (cache.exists() && cache.canRead()) {
                    return cache.getAbsolutePath();
                } else {
                    cache.getParentFile().mkdirs();
                }
                File temp = new File(cache.getAbsolutePath()+".tmp");
                InputStream in = null;
                OutputStream out = null;

                try {
                    URL parsedUrl = new URL(uri.toString());
                    for (int i = 0; i < 3 && !isCancelled(); i++) {
                        
                        HttpURLConnection connection = Connectivity.openDefaultConnection(parsedUrl, 3000 + i * 1500, (3000 * (2 + i)));
                        if(temp.exists()){
                            connection.addRequestProperty("Range", "bytes="+temp.length()+"-");
                            out = new FileOutputStream(temp,true);
                        }else
                            out = new FileOutputStream(temp);
                        try {
                            int responseCode = connection.getResponseCode();
                            if (responseCode == 200 || responseCode == 206) {
                                in = connection.getInputStream();
                                FileUtil.copyStream(in, out);
                                cache.delete();
                                if(!temp.renameTo(cache)){
                                    Log.w(TAG, "重命名失败"+temp.getName());
                                }
                                return cache.getAbsolutePath();
                            }
                        } catch (SocketTimeoutException e) {
                            Log.w(TAG, "retry", e);
                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    IOUtils.close(in);
                    IOUtils.close(out);
                }
                return null;
            }
            @Override
            protected void onPostExecute(String result) {
                loading = false;
                if(result == null)
                    onError();
                else
                    loadImage(result);
            }
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        case R.id.menu_item_comment:
            CommentsActivity.start(this, aid);
            return true;
        case R.id.menu_item_save_image:
            saveImage(mCurrentImage);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void saveImage(int index) {
        String dest = AcApp.getPreferenceImageSaveDir();
        String path = mList.get(index);
        Uri uri = Uri.parse(path);
        File saveFile;
        if (uri.getScheme().equals("http")) {
            // FIXME: volley 的缓存任务还没有被执行的时候是会获取不到数据的
            byte[] diskCache = AcApp.getDataInDiskCache(path);
            if (diskCache != null) {
                saveFile = new File(dest + "/" + FileUtil.getHashName(path));
                if (!FileUtil.save(diskCache, saveFile.getAbsolutePath())) {
                    saveFile = null;
                }
            } else {
                File cache = FileUtil.generateImageCacheFile(path);
                saveFile = FileUtil.copy(cache, dest);
            }
        } else {
            File cache = new File(uri.getPath());
            saveFile = FileUtil.copy(cache, dest);
        }
        if (saveFile != null && saveFile.exists()) {
            MobclickAgent.onEvent(this, "save_pic");
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(saveFile));
            sendBroadcast(intent);
            AcApp.showToast(getString(R.string.save_success)+":"+saveFile.getAbsolutePath());
        } else
            AcApp.showToast(getString(R.string.save_failed));
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.image, menu);
        MenuItem actionItem = menu.findItem(R.id.menu_item_share_action_provider_action_bar);
        if(ActionBarUtil.hasSB()){
            MenuItemCompat.setShowAsAction(actionItem, MenuItemCompat.SHOW_AS_ACTION_NEVER);
        }
        ShareActionProvider actionProvider = (ShareActionProvider)MenuItemCompat.getActionProvider(actionItem);
        actionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
        actionProvider.setShareIntent(createShareIntent());
        return super.onCreateOptionsMenu(menu);
    }
    
    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        String path = mList.get(mCurrentImage);
        Uri uri = Uri.parse(path);
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_TEXT, String.format("#Acfun文章区# 分享图片，%s - http://%s/a/ac%d",ArticleApi.getDomainRoot(getApplicationContext()), title, aid));
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        return shareIntent;
    }
    public static void startNetworkImage(Context context, ArrayList<String> list, int index, int aid, String title) {
        Intent intent = new Intent(context, ImagePagerActivity.class);
        intent.putStringArrayListExtra(EXTRA_IMAGES, list);
        intent.putExtra(EXTRA_INDEX, index);
        intent.putExtra("aid", aid);
        intent.putExtra("title", title);
        context.startActivity(intent);
    }
    @Override
    public void onPageScrollStateChanged(int arg0) {
    }
    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        
    }
    private int mCurrentImage;
    private ArrayList<String> mList;
    private String title;
    private int aid;
    @Override
    public void onPageSelected(int arg0) {
        mCurrentImage = arg0;
        indexText.setText(String.format("%d/%d",mCurrentImage+1,mList.size()));
    }
}
