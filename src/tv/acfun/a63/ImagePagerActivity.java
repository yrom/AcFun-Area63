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
import java.util.ArrayList;

import tv.acfun.a63.util.ActionBarUtil;
import tv.acfun.a63.util.BaseAnimationListener;
import tv.acfun.a63.util.FileUtil;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.umeng.analytics.MobclickAgent;


/**
 * @author Yrom
 *
 */
public class ImagePagerActivity extends SherlockFragmentActivity implements OnPageChangeListener {
    private static final String EXTRA_IMAGES = "images";
    private static final String EXTRA_INDEX = "index";
    private ViewPager pager;
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
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        ActionBarUtil.setXiaomiFilterDisplayOptions(getSupportActionBar(), false);
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_bg_trans));
        Bundle extras = getIntent().getExtras();
        title = extras.getString("title");
        getSupportActionBar().setTitle(title);
        aid = extras.getInt("aid");
        mList = extras.getStringArrayList(EXTRA_IMAGES);
        int index = extras.getInt(EXTRA_INDEX,0);
        MobclickAgent.onEvent(this, "view_big_pic", "ac"+aid+"/"+index);
        setContentView(R.layout.activity_images);
        
        indexText = (TextView) findViewById(R.id.index);
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new ImageAdapter(getSupportFragmentManager(),mList));
        pager.setOnPageChangeListener(this);
        pager.setCurrentItem(index);
        onPageSelected(index);
    }
    
    static class ImageAdapter extends FragmentPagerAdapter{
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
        public long getItemId(int position) {
            return position;
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
        private Uri mUri;
        private ImageContainer imageContainer;
        ProgressBar progress;
        TextView timeOut;
        ImagePagerActivity mContext;
        
        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            mContext = (ImagePagerActivity) activity;
        }
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mUri = Uri.parse(getArguments().getString(ARG_IMAGE_URL));
        }
        OnPhotoTapListener onTap = new OnPhotoTapListener() {
            
            @Override
            public void onPhotoTap(View view, float x, float y) {
                if(mContext.getSupportActionBar().isShowing()){
                    mContext.getSupportActionBar().hide();
                    mContext.hideInfo();
                }else{
                    mContext.getSupportActionBar().show();
                    mContext.showInfo();
                }
            }
        };
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_image, container,false);
            
            final PhotoView image = (PhotoView) rootView.findViewById(R.id.image);
            image.setOnPhotoTapListener(onTap);
            timeOut = (TextView)rootView.findViewById(R.id.time_out_text);
            
            Bitmap bitmap = AcApp.getBitmpInCache(mUri.toString());
            if(bitmap != null && !bitmap.isRecycled()){
                image.setImageBitmap(bitmap);
                return rootView;
            }
            if("file".equals(mUri.getScheme())){
                File img = new File(mUri.getPath());
                if(img.exists()){
                    if(bitmap == null || bitmap.isRecycled()){
                        bitmap= AcApp.decodeBitmap(mUri.getPath(), Bitmap.Config.RGB_565);
                        if(bitmap == null) //could not be decoded
                            img.delete();
                        else
                            AcApp.putBitmapInCache(mUri.toString(), bitmap);
                    }
                    if(bitmap != null)
                        image.setImageBitmap(bitmap);
                }else{
                    timeOut.setVisibility(View.VISIBLE);
                    timeOut.setText("加载失败，可能还没下载到数据，请重试");
                }
                
            }else{
                File cache = FileUtil.generateImageCacheFile(mUri.toString());
                if(cache.exists()){
                    if(bitmap == null || bitmap.isRecycled()){
                        bitmap= AcApp.decodeBitmap(cache.getAbsolutePath(), Bitmap.Config.RGB_565);
                        AcApp.putBitmapInCache(mUri.toString(), bitmap);
                    }
                    image.setImageBitmap(bitmap);
                    
                }else{
                    progress = (ProgressBar) rootView.findViewById(R.id.loading);
                    get(image);
                }
            }
            return rootView;
        }
        private void get(final PhotoView image) {
            progress.setVisibility(View.VISIBLE);
            imageContainer = AcApp.getGloableLoader().get(mUri.toString(), new ImageListener() {
                
                @Override
                public void onErrorResponse(VolleyError error) {
                    progress.setVisibility(View.GONE);
                    timeOut.setOnClickListener(new OnClickListener() {
                        
                        @Override
                        public void onClick(View v) {
                            v.setVisibility(View.GONE);
                            get(image);
                        }
                    });
                    timeOut.setVisibility(View.VISIBLE);
                }
                
                @Override
                public void onResponse(ImageContainer response, boolean isImmediate) {
                    if (response.getBitmap() != null) {
                        image.setImageBitmap(response.getBitmap());
                        progress.setVisibility(View.GONE);
                    }
                    
                }
            });
        }
        @Override
        public void onDestroyView() {
            super.onDestroyView();
            if(imageContainer != null && imageContainer.getBitmap()==null){
                Log.w("image loader", "cancle request:"+imageContainer.getRequestUrl());
                imageContainer.cancelRequest();
            }
        }
    }
    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {

        switch (item.getItemId()) {
        case android.R.id.home:
            this.finish();
            return true;
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
        boolean success;
        if(uri.getScheme().equals("http")){
            byte[] diskCache = AcApp.getDataInDiskCache(path);
            if(diskCache != null)
                success = FileUtil.save(diskCache,dest+"/"+FileUtil.getHashName(path));
            else{
                File cache = FileUtil.generateImageCacheFile(path);
                success = FileUtil.copy(cache, dest);
            }
        } else {
            File cache = new File(uri.getPath());
            success = FileUtil.copy(cache, dest);
        }
        if(success){
            MobclickAgent.onEvent(this, "save_pic");
            AcApp.showToast("保存成功");
        }else
            AcApp.showToast("保存失败！");
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.image, menu);
        MenuItem actionItem = menu.findItem(R.id.menu_item_share_action_provider_action_bar);
        ShareActionProvider actionProvider = (ShareActionProvider) actionItem.getActionProvider();
        actionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
        actionProvider.setShareIntent(createShareIntent());
        return super.onCreateOptionsMenu(menu);
    }
    
    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        String path = mList.get(mCurrentImage);
        Uri uri = Uri.parse(path);
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_TEXT, String.format("分享图片，%s - http://www.acfun.tv/a/ac%d",title,aid));
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
    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }
    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
