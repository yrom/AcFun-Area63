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
import tv.acfun.a63.view.MyViewPager;
import uk.co.senab.photoview.PhotoView;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Window;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageContainer;


/**
 * @author Yrom
 *
 */
public class ImagePagerActivity extends SherlockFragmentActivity {
    private static final String EXTRA_IMAGES = "images";
    private static final String EXTRA_INDEX = "index";
    private ViewPager pager;
    public static void startCacheImage(Context context, ArrayList<File> flist, int index){
        ArrayList<String> list = new ArrayList<String>(flist.size());
        for(File file : flist){
            list.add(Uri.fromFile(file).toString());
        }
        startNetworkImage(context, list, index);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        ActionBarUtil.setXiaomiFilterDisplayOptions(getSupportActionBar(), false);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.argb(124, 0, 0, 0)));
        Bundle extras = getIntent().getExtras();
        ArrayList<String> list = extras.getStringArrayList(EXTRA_IMAGES);
        int index = extras.getInt(EXTRA_INDEX,0);
        pager = new MyViewPager(this);
        pager.setId(R.id.pager);
        pager.setAdapter(new ImageAdapter(getSupportFragmentManager(),list));
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
        public ImageFragment(){}
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mUri = Uri.parse(getArguments().getString(ARG_IMAGE_URL));
            
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            PhotoView image = new PhotoView(inflater.getContext());
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
                imageContainer = AcApp.getGloableLoader().get(mUri.toString(), ImageLoader.getImageListener(image, 0, 0));
                
            }
            return image;
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
        }
        return super.onOptionsItemSelected(item);
    }
    public static void startNetworkImage(Context context, ArrayList<String> list, int index) {
        Intent intent = new Intent(context, ImagePagerActivity.class);
        intent.putStringArrayListExtra(EXTRA_IMAGES, list);
        intent.putExtra(EXTRA_INDEX, index);
        context.startActivity(intent);
    }
}
