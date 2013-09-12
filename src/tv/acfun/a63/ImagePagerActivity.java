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
import java.io.IOException;
import java.util.ArrayList;

import tv.acfun.a63.util.ActionBarUtil;
import tv.acfun.a63.util.FileUtil;
import tv.acfun.a63.view.MyViewPager;
import uk.co.senab.photoview.PhotoView;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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


/**
 * @author Yrom
 *
 */
public class ImagePagerActivity extends SherlockFragmentActivity implements OnPageChangeListener {
    private static final String EXTRA_IMAGES = "images";
    private static final String EXTRA_INDEX = "index";
    private ViewPager pager;
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
        aid = extras.getInt("aid");
        mList = extras.getStringArrayList(EXTRA_IMAGES);
        int index = extras.getInt(EXTRA_INDEX,0);
        pager = new MyViewPager(this);
        pager.setId(R.id.pager);
        pager.setAdapter(new ImageAdapter(getSupportFragmentManager(),mList));
        pager.setOnPageChangeListener(this);
        setContentView(pager);
        pager.setCurrentItem(index);
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
            args.putIntArray(ImageFragment.ARG_INDEX_ARRAY, new int[]{position+1, getCount()});
            args.putString(ImageFragment.ARG_IMAGE_URL, list.get(position));
            fragment.setArguments(args);
            return fragment;
        }
        
    }
    public static class ImageFragment extends Fragment{
        public static final String ARG_IMAGE_URL = "image_url";
        public static final String ARG_INDEX_ARRAY = "index_arr";
        private Uri mUri;
        private ImageContainer imageContainer;
        private int[] index;
        ProgressBar progress;
        View timeOut;
        public ImageFragment(){}
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mUri = Uri.parse(getArguments().getString(ARG_IMAGE_URL));
            index = getArguments().getIntArray(ARG_INDEX_ARRAY);
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_image, container,false);
            
            final PhotoView image = (PhotoView) rootView.findViewById(R.id.image);
            TextView indexText = (TextView) rootView.findViewById(R.id.index);
            indexText.setText(String.format("%d/%d", index[0],index[1]));
            if(mUri.getScheme().equals("file")){
                File img = new File(mUri.getPath());
                if(img.exists()){
                    image.setImageURI(mUri);
                }else{
                    try {
                        Drawable drawable = Drawable.createFromStream(getActivity().getAssets().open("emotion/21.gif"), "21.gif");
                        drawable.setBounds(0, 0, (int)(drawable.getIntrinsicWidth()*AcApp.density), (int)(drawable.getIntrinsicHeight()*AcApp.density));
                        image.setImageDrawable(drawable);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                
            }else{
                File cache = FileUtil.generateImageCacheFile(mUri.toString());
                if(cache.exists()){
                    image.setImageURI(Uri.fromFile(cache));
                }else{
                    progress = (ProgressBar) rootView.findViewById(R.id.loading);
                    timeOut = rootView.findViewById(R.id.time_out_text);
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
            // TODO
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
        // TODO Auto-generated method stub
        mCurrentImage = arg0;
    }
}
