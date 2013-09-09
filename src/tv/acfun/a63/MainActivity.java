package tv.acfun.a63;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import tv.acfun.a63.api.ArticleApi;
import tv.acfun.a63.api.Constants;
import tv.acfun.a63.api.entity.Content;
import tv.acfun.a63.api.entity.Contents;
import tv.acfun.a63.api.entity.User;
import tv.acfun.a63.util.ActionBarUtil;
import tv.acfun.a63.util.DensityUtil;
import tv.acfun.a63.util.FastJsonRequest;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.alibaba.fastjson.JSON;
import com.android.volley.Cache;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class MainActivity extends SherlockFragmentActivity implements
        OnItemClickListener, OnNavigationListener, OnClickListener {
    private static final String TAG = "MainActivity";

    private static String[] mPlanetTitles;

    private DrawerLayout mDrawerLayout;

    private ListView mDrawerList;

    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mTitle;

    private String[] mTitles;

    private ActionBar mBar;

    private View mDrawer;

    private View mAvatarFrame;

    private int mCurrentNavPosition;

    private static RequestQueue mQueue;

    private ImageView avatar;

    private TextView nameText;

    private TextView signatureText;

    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUser = AcApp.getUser();
        setContentView(R.layout.activity_main);
        ActionBarUtil.forceShowActionBarOverflowMenu(this);
        mBar = getSupportActionBar();
        ActionBarUtil.setXiaomiFilterDisplayOptions(getSupportActionBar(), true);
        mTitle = getTitle();
        mPlanetTitles = getResources().getStringArray(R.array.planets);
        initDrawerLayout(savedInstanceState);
        mQueue = AcApp.getGloableQueue();
    }

    private void initDrawerLayout(Bundle savedInstanceState) {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawer = findViewById(R.id.left_drawer);
        mAvatarFrame = findViewById(R.id.avatar_frame);
        mAvatarFrame.setOnClickListener(this);
        avatar = (ImageView) mAvatarFrame.findViewById(R.id.avatar);
        nameText = (TextView) mAvatarFrame.findViewById(R.id.user_name);
        signatureText = (TextView) mAvatarFrame.findViewById(R.id.signature);
        setUserInfo();
        
        
        mDrawerList = (ListView) findViewById(R.id.list);

        // set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);

        int[] iconIds = { R.drawable.ic_home, R.drawable.ic_bell,
                R.drawable.ic_heart };
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new NavigationAdapter(mPlanetTitles, iconIds));
        mDrawerList.setOnItemClickListener(this);

        mDrawerToggle = new ActionBarDrawerToggle(this, 
            mDrawerLayout, 
            R.drawable.ic_navigation_drawer, 
            R.string.app_name_open, 
            R.string.app_name 
            ) {
            public void onDrawerClosed(View view) {
                if (mCurrentNavPosition == 0) {
                    mBar.setDisplayShowTitleEnabled(false);
                    mBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
                } else
                    mBar.setTitle(mTitle);
                supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                if (mCurrentNavPosition == 0) {
                    mBar.setDisplayShowTitleEnabled(true);
                    mBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                }
                mBar.setTitle(R.string.app_name_open);
                supportInvalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mTitles = getResources().getStringArray(R.array.titles);
        if (savedInstanceState == null) {
            selectItem(0);
        }
        if (AcApp.getConfig().getBoolean("is_first_open", true)) {
            mDrawerLayout.openDrawer(mDrawer);
            AcApp.putBoolean("is_first_open", false);
        }
    }

    private void setUserInfo() {
        if(mUser != null){
            AcApp.getGloableLoader().get(mUser.avatar, ImageLoader.getImageListener(avatar, R.drawable.account_avatar, R.drawable.account_avatar));
            nameText.setText(mUser.name);
            if(!TextUtils.isEmpty(mUser.signature)){
                LayoutParams params = (LayoutParams) nameText.getLayoutParams();
                params.addRule(RelativeLayout.CENTER_VERTICAL,0);
                params.topMargin = DensityUtil.dip2px(getApplicationContext(), 8);
                nameText.setLayoutParams(params);
                signatureText.setText(mUser.signature);
                signatureText.setVisibility(View.VISIBLE);
            }
            View signout = mAvatarFrame.findViewById(R.id.signout);
            signout.setVisibility(View.VISIBLE);
            signout.setOnClickListener(this);
            avatar.setOnClickListener(this);
        }
    }

    private void selectItem(int position) {
        Fragment fragment = new PlanetFragment();
        Bundle args = new Bundle();
        args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
        if (position == 0) {
            mBar.setDisplayShowTitleEnabled(false);
            mBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            args.putStringArray(PlanetFragment.ARG_TITLES, mTitles);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    mBar.getThemedContext(), R.layout.list_item_2,
                    android.R.id.text2, getResources().getStringArray(
                            R.array.modes)) {

                @Override
                public View getView(int position, View convertView,
                        ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView text = (TextView) view
                            .findViewById(android.R.id.text1);
                    text.setText(mPlanetTitles[0]);
                    return view;
                }

            };
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
            mBar.setListNavigationCallbacks(adapter, this);

        }

        fragment.setArguments(args);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment).commit();

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mPlanetTitles[position]);
        mDrawerLayout.closeDrawer(mDrawer);
        mCurrentNavPosition = position;
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content
        // view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawer);
        MenuItem findItem = menu.findItem(R.id.action_view_mode);
        if (findItem != null)
            findItem.setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        mBar.setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:

            if (mDrawerLayout.isDrawerOpen(mDrawer)) {
                mDrawerLayout.closeDrawer(mDrawer);
            } else {
                mDrawerLayout.openDrawer(mDrawer);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public static class SectionsPagerAdapter extends FragmentPagerAdapter {
        String[] titles;

        public SectionsPagerAdapter(FragmentManager fragmentManager,
                String[] titles) {
            super(fragmentManager);
            this.titles = titles;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = new DummyCardFragment();
            Bundle args = new Bundle();
            args.putInt(DummyCardFragment.ARG_SECTION_NUMBER, position);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            if (position < titles.length)
                return titles[position];
            return null;
        }
    }

    /**
     * Fragment that appears in the "content_frame", shows a planet
     */
    public static class PlanetFragment extends SherlockFragment {
        public static final String ARG_TITLES = "titles";

        public static final String ARG_PLANET_NUMBER = "planet_number";

        private static int VIEW_MODE_CODE;
        private MenuItem mModeMenu;

        private SectionsPagerAdapter mSectionsPagerAdapter;

        public PlanetFragment() {
            VIEW_MODE_CODE = AcApp.getViewMode();
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
            case R.id.mode_mix:
                VIEW_MODE_CODE = 0;
                break;
            case R.id.mode_no_image:
                VIEW_MODE_CODE = 1;
                break;
            case R.id.mode_comic:
                VIEW_MODE_CODE = 2;
                break;
            }
            AcApp.putInt("view_mode", VIEW_MODE_CODE);
            setMenuIcon();
            return super.onOptionsItemSelected(item);
        }

        private void setMenuIcon() {
            switch (VIEW_MODE_CODE) {
            case 1:
                mModeMenu.setIcon(R.drawable.mode_no_pic);
                mModeMenu.setTitle(R.string.view_mode_no_image);
                break;
            case 2:
                mModeMenu.setTitle(R.string.view_mode_comic);
                mModeMenu.setIcon(R.drawable.mode_comic);
                break;
            case 0:
            default:
                mModeMenu.setTitle(R.string.view_mode_mix);
                mModeMenu.setIcon(R.drawable.mode_mix);
                break;
            }
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.view_mode, menu);
            mModeMenu = menu.findItem(R.id.action_view_mode);
            setMenuIcon();
            super.onCreateOptionsMenu(menu, inflater);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            int i = getArguments().getInt(ARG_PLANET_NUMBER);
            View rootView = null;
            if (i == 0) {
                setHasOptionsMenu(true);
                rootView = inflater.inflate(R.layout.fragment_home, container,
                        false);
                if(mSectionsPagerAdapter == null)
                mSectionsPagerAdapter = new SectionsPagerAdapter(
                        getChildFragmentManager(), getArguments().getStringArray(ARG_TITLES));
                PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) rootView.findViewById(R.id.tabs);
                ViewPager viewPager = (ViewPager) rootView.findViewById(R.id.pager);
                viewPager.setOffscreenPageLimit(1);
                viewPager.setAdapter(mSectionsPagerAdapter);
                tabs.setViewPager(viewPager);
            } else {
                setHasOptionsMenu(false);
                rootView = inflater.inflate(R.layout.fragment_planet,
                        container, false);
                ((TextView) rootView).setText(mPlanetTitles[i]);
            }
            return rootView;
        }
    }

    public static class DummyCardFragment extends Fragment implements OnItemClickListener {

        public static final String ARG_SECTION_NUMBER = "section_number";
        int DEFAULT_COUT = 20;
        int page;
        PullToRefreshListView list;
        LayoutInflater inflater;
        
        private FastJsonRequest<Contents> request;
        private ILoadingLayout loadingLayout;
        public DummyCardFragment() {
        }
        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            this.inflater = inflater;
            View rootView = inflater.inflate(R.layout.fragment_main_dummy,
                    container, false);
            list = (PullToRefreshListView) rootView.findViewById(R.id.list);
            loadingLayout = list.getLoadingLayoutProxy(true, false);
            list.setOnRefreshListener(new OnRefreshListener<ListView>() {
                @Override
                public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                    // Do work to refresh the list here.
                    String label = DateUtils.formatDateTime(getActivity(), System.currentTimeMillis(),
                            DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
                    loadingLayout.setLastUpdatedLabel(label);
                    loadData(true,false);
                }
            });
            list.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

                @Override
                public void onLastItemVisible() {
                    loadData(false,false);
                }
            });
            list.setOnItemClickListener(this);
            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            loadData(true, true);
        }
        
        private ArticleListAdapter adapter;

        private void loadData(boolean newData, boolean loadCache) {
            page = newData ? 1 : page + 1;
            String url = ArticleApi.getDefaultUrl(Constants.CAT_IDS[getArguments().getInt(ARG_SECTION_NUMBER)], DEFAULT_COUT, page);
           
            // 缓存数据
            final Cache.Entry entry = mQueue.getCache().get(url);
            if (entry != null && entry.data != null && entry.data.length > 0) {
                if (newData) {
//                    entry.ttl = entry.softTtl = System.currentTimeMillis() - 1000;
                    mQueue.getCache().invalidate(url, true);
                    Log.i(TAG, "强制刷新");
                }
                if (loadCache) {
                    new Thread() {
                        @Override
                        public void run() {
                            Contents contens = JSON.parseObject(new String(entry.data),
                                    Contents.class);
                            if (contens != null && contens.getContents() != null) {
                                adapter = new ArticleListAdapter(inflater, contens.getContents());
                                list.post(new Runnable() {

                                    @Override
                                    public void run() {
                                        list.setAdapter(adapter);
                                        list.setRefreshing();
                                    }
                                });
                            }
                        }

                    }.start();
                    return;
                }
            }
            request = new FastJsonRequest<Contents>(
                    url, Contents.class, new Response.Listener<Contents>() {
                        @Override
                        public void onResponse(Contents response) {
                            if(page <= 1){
                                if(adapter == null){
                                    adapter = new ArticleListAdapter(inflater,response.getContents());
                                    list.setAdapter(adapter);
                                } else
                                    adapter.contents = response.getContents();
                            }else{
                                adapter.addData(response.getContents());
                            }
                            adapter.notifyDataSetChanged();
                            list.onRefreshComplete();
                        }

                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "load list error", error);
                            AcApp.showToast("加载失败");
                            list.onRefreshComplete();
                        }
                    });
            request.setShouldCache(true);
            if(BuildConfig.DEBUG)
                Log.d(TAG, "new request:"+request.getUrl());
            mQueue.add(request);
        }
        @Override
        public void onDestroyView() {
            super.onDestroyView();
            if(request != null && !request.hasHadResponseDelivered() && !request.isCanceled()){
                request.cancel();
                Log.w(TAG, "request canceled");
                request = null;
            }
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Object obj = parent.getItemAtPosition(position);
            if(obj != null && obj instanceof Content){
                Content c = (Content)obj;
                // TODO 根据模式跳转
                ArticleActivity.start(getActivity(), c.aid,c.title);
            }
        }
    }
    static class ArticleListAdapter extends BaseAdapter {
        List<Content> contents;
        LayoutInflater inflater;
        public ArticleListAdapter(LayoutInflater inflater, List<Content> contents) {
            this.contents = contents;
            this.inflater = inflater;
        }
        @Override
        public int getCount() {
            return contents.size();
        }
        public void addData(List<Content> contents){
            this.contents.addAll(contents);
        }
        @Override
        public Content getItem(int position) {
            return contents.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ListViewHolder holder;
            if(convertView == null){
                convertView = inflater.inflate(R.layout.article_list_item, parent,false);
                holder = new ListViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.article_item_title);
                holder.postTime = (TextView) convertView.findViewById(R.id.article_item_post_time);
                holder.comments = (TextView) convertView.findViewById(R.id.article_desc);
                convertView.setTag(holder);
            }
            holder = (ListViewHolder) convertView.getTag();
            Content art = getItem(position);
            holder.title.setText(art .getTitle());
            if(!TextUtils.isEmpty(art.description))
                holder.comments.setText(Html.fromHtml(art.description));
            else{
                holder.comments.setText("无简介...");
            }
            View tagHot = convertView.findViewById(R.id.item_tag);
            if(ArticleApi.isRecommendedArticle(art)){
                tagHot.setVisibility(View.VISIBLE);
                ((ImageView)tagHot).setImageResource(R.drawable.ic_recommended);
            }
            else if(ArticleApi.isHotArticle(art)){
                tagHot.setVisibility(View.VISIBLE);
                ((ImageView)tagHot).setImageResource(R.drawable.ic_whats_hot);
            }
            
            else
                tagHot.setVisibility(View.GONE);
            String tip = String.format(" %s / %d条评论，%d人围观", AcApp.getPubDate(art.releaseDate),art.comments,art.views);
//            holder.postTime.setText(AcApp.getPubDate(art.releaseDate));
            holder.postTime.setText(tip);
            return convertView;
        }


    }
    
    static class ArticleListAdapter2 extends BaseAdapter {
        List<Content> contents;
        LayoutInflater inflater;
        public ArticleListAdapter2(LayoutInflater inflater, List<Content> contents) {
            this.contents = contents;
            this.inflater = inflater;
        }
        SimpleDateFormat mDateFormatter= new SimpleDateFormat("MM月dd日 HH:mm");
        @Override
        public int getCount() {
            return contents.size();
        }
        public void addData(List<Content> contents){
            this.contents.addAll(contents);
        }
        @Override
        public Content getItem(int position) {
            return contents.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ListViewHolder holder;
            if(convertView == null){
                convertView = inflater.inflate(R.layout.article_list_item2, parent,false);
                holder = new ListViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.article_item_title);
                holder.titleImage = (ImageView) convertView.findViewById(R.id.article_title_img);
                holder.views = (TextView) convertView.findViewById(R.id.article_item_views);
                holder.upman = (TextView) convertView.findViewById(R.id.article_item_upman);
                holder.postTime = (TextView) convertView.findViewById(R.id.article_item_post_time);
                holder.comments = (TextView) convertView.findViewById(R.id.article_item_comments);
                convertView.setTag(holder);
            }
            holder = (ListViewHolder) convertView.getTag();
            Content art = getItem(position);
            holder.title.setText(art .getTitle());
            holder.views.setText(String.valueOf(art.getViews()));
            String up = art.getUsername(); 
            up = TextUtils.isEmpty(up)?"无名氏 /":up+" / ";
            String time = mDateFormatter.format(new Date(art.getReleaseDate()));
            holder.upman.setText(up+time+" / "+String.valueOf(art.getComments()));
//            holder.comments.setText(String.valueOf(art.getComments()));
//            holder.postTime.setText(time);
            holder.postTime.setText(art.description);
            if(!art.titleImg.contains("style/image/cover")){
                AcApp.getGloableLoader().get(art.titleImg, ImageLoader.getImageListener(holder.titleImage, 0, 0));
            }
//            holder.titleImage.setImageUrl(art.titleImage, AcApp.getGloableLoader());
            return convertView;
        }

    }
    static class ListViewHolder{
        TextView comments,
        postTime,
        upman,
        views,
        title;
        ImageView titleImage;
        
    }
    static class NavigationItem {
        String title;
        Drawable icon;
    }

    public class NavigationAdapter extends BaseAdapter {
        NavigationItem[] navs;

        public NavigationAdapter(String[] titles, int[] iconIds) {
            navs = new NavigationItem[titles.length];
            for (int i = 0; i < titles.length && i < iconIds.length; i++) {
                navs[i] = new NavigationItem();
                navs[i].title = titles[i];
                navs[i].icon = getResources().getDrawable(iconIds[i]);
            }
        }

        @Override
        public int getCount() {
            return navs.length;
        }

        @Override
        public NavigationItem getItem(int position) {
            return navs[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            NavigationItem item = getItem(position);
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(
                        R.layout.navigation_list_item, parent, false);
            }
            ImageView iconView = (ImageView) convertView
                    .findViewById(R.id.icon);
            TextView titleView = (TextView) convertView.findViewById(R.id.text);
            iconView.setImageDrawable(item.icon);
            titleView.setText(item.title);
            return convertView;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position,
            long arg3) {
        if (mCurrentNavPosition != position)
            selectItem(position);
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        //TODO change content list type
        Log.i(TAG, "click position = " + itemPosition);
        
        return false;
    }

    @Override
    public void onClick(View v) {
        if (v == mAvatarFrame) {
            if(mUser != null)
                startActivity(new Intent(this,ProfileActivity.class));
            else
                startActivityForResult(
                    SigninActivity.createIntent(getApplicationContext()),
                    SigninActivity.REQUEST_SIGN_IN);
        }else if(v.getId() == R.id.signout || v.getId() == R.id.avatar && mUser != null){
            AcApp.logout();
            mUser = null;
            // Remove current avatar frame..
            ((LinearLayout)mDrawer).removeViewAt(0);
            mAvatarFrame = getLayoutInflater().inflate(R.layout.avatar_frame, (LinearLayout)mDrawer,false);
            mAvatarFrame.setOnClickListener(this);
            ((LinearLayout)mDrawer).addView(mAvatarFrame, 0);
            AcApp.showToast("注销");
        }

    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        if(result == RESULT_OK){
            mUser = data.getExtras().getParcelable("user");
            setUserInfo();
        }
    }
}
