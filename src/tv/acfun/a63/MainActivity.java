package tv.acfun.a63;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import tv.acfun.a63.api.ArticleApi;
import tv.acfun.a63.api.Constants;
import tv.acfun.a63.api.entity.Content;
import tv.acfun.a63.api.entity.Contents;
import tv.acfun.a63.api.entity.User;
import tv.acfun.a63.db.DB;
import tv.acfun.a63.util.ActionBarUtil;
import tv.acfun.a63.util.DensityUtil;
import tv.acfun.a63.util.FastJsonRequest;
import tv.acfun.a63.util.TextViewUtils;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
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
import android.widget.ListAdapter;
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
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.Cache;
import com.android.volley.Cache.Entry;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.ImageLoader;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.FeedbackAgent;
import com.umeng.update.UmengUpdateAgent;

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

    private Fragment mContentFragment;

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
        // umeng
        initUmeng();
    }

    private void initUmeng() {
        MobclickAgent.setDebugMode(BuildConfig.DEBUG);
        UmengUpdateAgent.update(this);
        MobclickAgent.onError(this);
        new FeedbackAgent(this).sync();
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
        mDrawerLayout.setScrimColor(Color.argb(100, 0, 0, 0));
        int[] iconIds = { R.drawable.ic_home, /*TODO R.drawable.ic_bell,*/
                 R.drawable.ic_hot, R.drawable.ic_heart};
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
        mFragments = new ArrayList<Fragment>(mTitles.length);
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
//            logout
//            View signout = mAvatarFrame.findViewById(R.id.signout);
//            signout.setVisibility(View.VISIBLE);
//            signout.setOnClickListener(this);
//            avatar.setOnClickListener(this);
        }
    }
    /**
     * @see <a href="http://www.yrom.net/blog/2013/03/10/fragment-switch-not-restart/" >fragment switch</a>
     * @param from
     * @param to
     */
    private void switchContent(Fragment from, Fragment to) {
        if(to == null) 
            throw new IllegalStateException("content fragment还没有初始化！");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction().setCustomAnimations(
                android.R.anim.fade_in, R.anim.slide_out);
        if(from == null){
            mContentFragment = to;
            transaction.replace(R.id.content_frame, to);
        } else if (mContentFragment != to) {
            mContentFragment = to;
            transaction.hide(from);
            if (!to.isAdded()) {  
                transaction.add(R.id.content_frame, to);
            } else {
                transaction.show(to);
            }
        }
        transaction.commit();
        
    }
    private List<Fragment> mFragments;

    private ArrayAdapter<String> mNavAdapter;
    /**
     * @see <a href="http://www.yrom.net/blog/2013/07/17/viewpager-cant-change-tag-of-fragment/">can't change tag of fragment</a>
     * @param position
     * @return
     */
    private Fragment getFragment(int position){
        Fragment f = null;
        if(position <mFragments.size()){
            f = mFragments.get(position);
            if(f != null)
                return f;
        }
        while(position>=mFragments.size()){
            mFragments.add(null);
        }
        Bundle args = new Bundle();
        if(position == 0){
            MobclickAgent.onEvent(this, "main");
            f = new HomeFragment();
            args.putInt(HomeFragment.ARG_PLANET_NUMBER, position);
            args.putStringArray(HomeFragment.ARG_TITLES, mTitles);
        }else if( position == 1){
            MobclickAgent.onEvent(this, "rank");
            f = new RankListFragment();
            args.putInt(RankListFragment.ARG_LIST_MODE, 3);
            args.putInt(RankListFragment.ARG_SECTION_NUMBER, 4);
            f.setHasOptionsMenu(false);
        }else if(position == 2){
            MobclickAgent.onEvent(this, "fav");
            f = new FavListFragment();
        }
        f.setArguments(args);
        mFragments.set(position,f);
        return f;
    }
    private void selectItem(int position) {
        if (position == 0) {
            mBar.setDisplayShowTitleEnabled(false);
            mBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            if(mNavAdapter == null){
                mNavAdapter = new ArrayAdapter<String>(
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
                mNavAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
            }
            mBar.setListNavigationCallbacks(mNavAdapter, this);
        }
        
        Fragment f = getFragment(position);
        switchContent(mContentFragment, f);
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
        case R.id.action_settings:
            SettingsActivity.start(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    /**
     * 四个频道列表
     *
     */
    public static class SectionsPagerAdapter extends FragmentPagerAdapter {
        String[] titles;
        int contentListMode = 0;
        public SectionsPagerAdapter(FragmentManager fragmentManager,
                String[] titles) {
            super(fragmentManager);
            this.titles = titles;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = new ArticleListFragment();
            Bundle args = new Bundle();
            args.putInt(ArticleListFragment.ARG_SECTION_NUMBER, position);
            args.putInt(ArticleListFragment.ARG_LIST_MODE, contentListMode);
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
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ArticleListFragment fragment = (ArticleListFragment) super.instantiateItem(container, position);
            fragment.setContentListMode(contentListMode);
            return fragment;
            
        }
        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }
        public void changeContentListMode(int itemPosition) {
            if(contentListMode != itemPosition){
                contentListMode = itemPosition;
                AcApp.putInt("nav_item",itemPosition);
                notifyDataSetChanged();
            }
        }
    }

    /**
     * 主页，四个频道列表，三种阅读模式
     */
    public static class HomeFragment extends SherlockFragment {
        public static final String ARG_TITLES = "titles";

        public static final String ARG_PLANET_NUMBER = "planet_number";

        private static int VIEW_MODE_CODE;
        private MenuItem mModeMenu;

        private SectionsPagerAdapter mSectionsPagerAdapter;

        public HomeFragment() {
            VIEW_MODE_CODE = AcApp.getViewMode();
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
            case R.id.mode_mix:
                VIEW_MODE_CODE = Constants.MODE_MIX;
                break;
            case R.id.mode_no_image:
                VIEW_MODE_CODE = Constants.MODE_NO_PIC;
                break;
            case R.id.mode_comic:
                VIEW_MODE_CODE = Constants.MODE_COMMIC;
                break;
            }
            AcApp.putInt("view_mode", VIEW_MODE_CODE);
            setMenuIcon();
            return super.onOptionsItemSelected(item);
        }

        private void setMenuIcon() {
            switch (VIEW_MODE_CODE) {
            case Constants.MODE_NO_PIC:
                mModeMenu.setIcon(R.drawable.mode_no_pic);
                mModeMenu.setTitle(R.string.view_mode_no_image);
                break;
            case Constants.MODE_COMMIC:
                mModeMenu.setTitle(R.string.view_mode_comic);
                mModeMenu.setIcon(R.drawable.mode_comic);
                break;
            case Constants.MODE_MIX:
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
                rootView = createHomeView(inflater, container);
            } else {
                setHasOptionsMenu(false);
                rootView = inflater.inflate(R.layout.fragment_planet,
                        container, false);
                ((TextView) rootView).setText(mPlanetTitles[i]);
            }
            return rootView;
        }

        private View createHomeView(LayoutInflater inflater, ViewGroup container) {
            View rootView = inflater.inflate(R.layout.fragment_home, container,
                    false);
            if(mSectionsPagerAdapter == null)
            mSectionsPagerAdapter = new SectionsPagerAdapter(
                    getChildFragmentManager(), getArguments().getStringArray(ARG_TITLES));
            PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) rootView.findViewById(R.id.tabs);
            ViewPager viewPager = (ViewPager) rootView.findViewById(R.id.pager);
            viewPager.setOffscreenPageLimit(3);
            viewPager.setAdapter(mSectionsPagerAdapter);
            tabs.setViewPager(viewPager);
            return rootView;
        }
        
        public void changeContentListMode(int itemPosition) {
            Log.d(TAG, "on adapter change Content ListMode = " +itemPosition );
            mSectionsPagerAdapter.changeContentListMode(itemPosition);
        }
    }
    public static class FavListFragment extends Fragment{

        @Override
        public void onResume() {
            super.onResume();
            mEmptyView.setVisibility(View.GONE);
            loadData();
        }
        LayoutInflater inflater;
        private ListView mList;
        private View mProgress;
        private View mEmptyView;
        private OnItemClickListener mOnItemclick = new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Content c = (Content) parent.getItemAtPosition(position);
                ArticleActivity.start(getActivity(), c.aid, c.title);
            }
            
        };
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            this.inflater = inflater;
            View root = inflater.inflate(R.layout.fragment_list,container,false);
            mList = (ListView) root.findViewById(R.id.list);
            mList.setOnItemClickListener(mOnItemclick);
            mProgress = root.findViewById(R.id.loading);
            mEmptyView = root.findViewById(R.id.time_out_text);
            return root;
        }

        private void loadData() {
            new Thread(){
                public void run() {
                    final List<Content> favList = new DB(getActivity()).getFavList();
                    mList.post(new Runnable() {
                        
                        @Override
                        public void run() {
                            mProgress.setVisibility(View.GONE);
                            setListAdapter(favList);
                        }
                    });
                }
            }.start();
        }
        private void setListAdapter(final List<Content> favList) {
            if(favList == null || favList.isEmpty()){
                mEmptyView.setVisibility(View.VISIBLE);
            }else
                setListAdapter(new ArticleListAdapter(inflater, favList, 0){

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    ListViewHolder holder;
                    if(convertView == null){
                        convertView = inflater.inflate(R.layout.fav_list_item,parent,false);
                        holder = new ListViewHolder();
                        holder.title = (TextView) convertView.findViewById(R.id.article_item_title);
                        holder.postTime = (TextView) convertView.findViewById(R.id.article_item_post_time);
                        holder.comments = (TextView) convertView.findViewById(R.id.article_desc);
                        holder.channel = (TextView) convertView.findViewById(R.id.item_tag_channel);
                        convertView.setTag(holder);
                    }else{
                        holder = (ListViewHolder) convertView.getTag();
                    }
                    Content content = getItem(position);
                    holder.title.setText(TextViewUtils.getSource(content.title));
                    if(!TextUtils.isEmpty(content.description))
                        holder.comments.setText(Html.fromHtml(TextViewUtils.getSource(content.description)));
                    else{
                        holder.comments.setText("无简介...");
                    }
                    String tip = String.format(" 于%s收藏，有%d人同好", AcApp.getPubDate(content.releaseDate),content.stows);
                    holder.postTime.setText(tip);
                    holder.channel.setText(ArticleApi.getChannelName(content.channelId));
                    return convertView;
                }
                
            });
        }


        private void setListAdapter(ListAdapter adapter) {
            if(adapter == null) return;
            mList.setVisibility(View.VISIBLE);
            mList.setAdapter(adapter);
            
        }
    }
    
    /**
     * 排行榜
     *
     */
    public static class RankListFragment extends ArticleListFragment{

        @Override
        protected Request<?> getRequest(String url) {
            
            return new RankListRequest(url, listener, errorListner);
        }

        @Override
        protected Contents loadDataFromCache(Entry entry) {
            return parseJson(new String(entry.data));
        }

        
    }
    static Contents parseJson(String rankJson){
        JSONObject rankList = JSON.parseObject(rankJson);
        
        if(!rankList.getBooleanValue("success")){
            return null;
        }
        JSONArray jsonArr = rankList.getJSONObject("content").getJSONArray("json");
        
        List<Content> contents = new ArrayList<Content>();
        for(int i=0;i<jsonArr.size();i++){
            JSONArray carr = jsonArr.getJSONArray(i);
            Content c = new Content();
            c.aid = carr.getIntValue(0);
            c.title =carr.getString(1);
            c.description = carr.getString(2);
            c.releaseDate = carr.getLongValue(5)*1000;
            c.views = carr.getIntValue(10);
            c.comments = carr.getIntValue(11);
            c.stows = carr.getIntValue(12);
            c.channelId = carr.getIntValue(17);
            contents.add(c);
        }
        
        Contents cs = new Contents();
        cs.setContents(contents);
        return cs;
        
    }
    static class RankListRequest extends FastJsonRequest<Contents>{

        public RankListRequest(String url, Listener<Contents> listener,
                ErrorListener errorListner) {
            super(url, Contents.class, listener, errorListner);
        }
        
        @Override
        protected Response<Contents> parseNetworkResponse(NetworkResponse response) {
            try {
                String json = new String(
                        response.data, HttpHeaderParser.parseCharset(response.headers));
                Contents contents = parseJson(json);
                if(contents == null)
                    throw new NullPointerException();
                return Response.success(contents,HttpHeaderParser.parseCacheHeaders(response));
            }catch (UnsupportedEncodingException e) {
                String json = new String(response.data);
                Contents contents = parseJson(json);
                if(contents == null)
                    throw new NullPointerException();
                return Response.success(contents,HttpHeaderParser.parseCacheHeaders(response));
            } catch(Exception e){
                return Response.error(new ParseError(e));
            }
            
        }
    }
    /**
     * 文章列表
     *
     */
    public static class ArticleListFragment extends Fragment implements OnItemClickListener {

        public static final String ARG_LIST_MODE = "list_mode";
        public static final String ARG_SECTION_NUMBER = "section_number";
        int DEFAULT_COUT = 20;
        int page;
        PullToRefreshListView list;
        LayoutInflater inflater;
        boolean isLoading;
        View footView;
        int listMode;
        private Request<?> request;
        private ILoadingLayout loadingLayout;
        private View loadding;
        public ArticleListFragment() {
        }
        
        public void setContentListMode(int contentListMode) {
            if(listMode != contentListMode){
                listMode = contentListMode;
                Log.d(TAG, String.format("[%d] framgent change Content ListMode =%d",section,listMode));
                list.setRefreshing();
            }
        }
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            listMode = getArguments().getInt(ARG_LIST_MODE);
            section = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            this.inflater = inflater;
            View rootView = inflater.inflate(R.layout.fragment_main_dummy,
                    container, false);
            list = (PullToRefreshListView) rootView.findViewById(R.id.list);
            timeOut = rootView.findViewById(R.id.time_out_text);
            loadding = rootView.findViewById(R.id.loading);
            footView = inflater.inflate(R.layout.list_footerview, list.getRefreshableView(),false);
            list.getRefreshableView().addFooterView(footView, null, false);
            loadingLayout = list.getLoadingLayoutProxy(true, false);
            list.setOnRefreshListener(new OnRefreshListener<ListView>() {
                @Override
                public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                    // Do work to refresh the list here.
                    Log.i(TAG, String.format("[%d] on refresh ",section));
                    isLoading = true;
                    String label = DateUtils.formatDateTime(getActivity() == null?getActivity():AcApp.context(), System.currentTimeMillis(),
                            DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
                    loadingLayout.setLastUpdatedLabel(label);
                    loadData(true,false);
                }
            });
            list.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

                @Override
                public void onLastItemVisible() {
                    if(!isLoading){
                        Log.i(TAG, String.format("[%d] 加载下一页, mode=%d",section,listMode));
                        loadData(false,false);
                    }
                }
            });
            list.setOnItemClickListener(this);
            return rootView;
        }

        boolean isShowing;
        @Override
        public void onResume() {
            super.onResume();
            Log.d(TAG, String.format("[%d] on fragment resume, showing = %b",section,isShowing));
            if(!isShowing){
                loadding.setVisibility(View.VISIBLE);
                loadData(true, true);
                isShowing = true;
            }
        }
        private ArticleListAdapter adapter;
        Response.Listener<Contents> listener = new Response.Listener<Contents>() {
            @Override
            public void onResponse(Contents response) {
                if(page <= 1){
                    if(adapter == null){
                        adapter = new ArticleListAdapter(inflater,response.getContents(),listMode);
                    } else
                        adapter.contents = response.getContents();
                    list.setAdapter(adapter);
                }else{
                    adapter.addData(response.getContents());
                }
                loadding.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
                list.onRefreshComplete();
                needReload = false;
                isLoading = false;
            }

        };
        OnClickListener onReload = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(needReload){
                    if(page >1) {
                        page -= 1;
                        loadData(false, false);
                    }else{
                        loadData(true, false);
                    }
                }
            }
        };
        boolean needReload;
        Response.ErrorListener errorListner = new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, String.format("[%d] load list error",section), error);
                TextView text = (TextView) footView.findViewById(R.id.list_footview_text);
                text.setText(R.string.reloading);
                footView.findViewById(R.id.list_footview_progress).setVisibility(View.GONE);
                footView.setOnClickListener(onReload);
                needReload = true;
                if(list.getRefreshableView().getAdapter()==null)
                    timeOut.setVisibility(View.VISIBLE);
                list.onRefreshComplete();
                isLoading = false;
            }
        };
        protected int section;
        protected View timeOut;
        final void loadData(boolean newData, boolean loadCache) {
            initPage(newData);
            String url = getContentListUrl();
           
            // 缓存数据
            final Cache.Entry entry = mQueue.getCache().get(url);
            if (entry != null && entry.data != null && entry.data.length > 0) {
                if (newData && !entry.isExpired()) {
//                    entry.ttl = entry.softTtl = System.currentTimeMillis() - 1000;
                    mQueue.getCache().invalidate(url, true);
                    Log.i(TAG, String.format("[%d] 刷新数据",section));
                }
                if (loadCache) {
                    new Thread() {
                        @Override
                        public void run() {
                            Contents contens = loadDataFromCache(entry);
                            if (contens != null && contens.getContents() != null) {
                                adapter = new ArticleListAdapter(inflater, contens.getContents(),listMode);
                                list.post(new Runnable() {

                                    @Override
                                    public void run() {
                                        list.setAdapter(adapter);
                                        isLoading = false;
                                        list.setRefreshing();
                                        loadding.setVisibility(View.GONE);
                                    }
                                });
                            }
                        }
                    }.start();
                    
                    return;
                }
            }
            
            request = getRequest(url);
            request.setShouldCache(true);
            if(BuildConfig.DEBUG)
                Log.d(TAG, String.format("[%d] new request: %s",section,request.getUrl()));
            mQueue.add(request);
        }

        protected Request<?> getRequest(String url) {
            return new FastJsonRequest<Contents>(url, Contents.class, listener, errorListner);
        }
        
        protected Contents loadDataFromCache(final Cache.Entry entry) {
            Contents contens = JSON.parseObject(new String(entry.data),
                    Contents.class);
            return contens;
        }
        protected void initPage(boolean newData) {
            timeOut.setVisibility(View.GONE);
            TextView text = (TextView) footView.findViewById(R.id.list_footview_text);
            text.setText(R.string.loading);
            footView.findViewById(R.id.list_footview_progress).setVisibility(View.VISIBLE);
            footView.setOnClickListener(null);
            
            page = newData ? 1 : page + 1;
        }
        //将默认列表调整为热门
        private String getContentListUrl() {
            
            switch (listMode) {
            case 1:
                return ArticleApi.getLatestRepliedUrl(Constants.CAT_IDS[section],page);
            case 2:
                return ArticleApi.getDefaultUrl(Constants.CAT_IDS[section], DEFAULT_COUT, page);
            case 3:
                return ArticleApi.getRankListUrl(page);
            case 0:
            default:
                return ArticleApi.getHotListUrl(Constants.CAT_IDS[section],page);
            }
        }
        @Override
        public void onDestroyView() {
            super.onDestroyView();
            isShowing = false;
            if(request != null && !request.hasHadResponseDelivered() && !request.isCanceled()){
                request.cancel();
                Log.w(TAG, String.format("[%d]request canceled : %s",section,request.getUrl()));
                request = null;
            }
        }
        @Override
        public void onDestroy() {
            super.onDestroy();
            if(adapter != null && adapter.contents != null){
                Log.d(TAG, String.format("[%d] destory adapter,size=%d", section,adapter.getCount()));
                adapter.contents.clear();
                adapter = null;
            }
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Object obj = parent.getItemAtPosition(position);
            if(obj != null && obj instanceof Content){
                Content c = (Content)obj;
                ArticleActivity.start(getActivity(), c.aid,c.title);
            }
        }
    }
    static class ArticleListAdapter extends BaseAdapter {
        List<Content> contents;
        LayoutInflater inflater;
        int mode;
        public ArticleListAdapter(LayoutInflater inflater, List<Content> contents, int listMode) {
            this.contents = contents;
            this.inflater = inflater;
            this.mode = listMode;
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
                if(mode <3){
                    convertView = inflater.inflate(R.layout.article_list_item, parent,false);
                }else{
                    convertView = inflater.inflate(R.layout.rank_list_item, parent,false);
                }
                holder = new ListViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.article_item_title);
                holder.postTime = (TextView) convertView.findViewById(R.id.article_item_post_time);
                holder.comments = (TextView) convertView.findViewById(R.id.article_desc);
                convertView.setTag(holder);
            }
            holder = (ListViewHolder) convertView.getTag();
            Content art = getItem(position);
            holder.title.setText(TextViewUtils.getSource(art.getTitle()));
            if(!TextUtils.isEmpty(art.description))
                holder.comments.setText(Html.fromHtml(TextViewUtils.getSource(art.description)));
            else{
                holder.comments.setText("无简介...");
            }
            
            if (mode < 3) {
                View tagHot = convertView.findViewById(R.id.item_tag);
                if (ArticleApi.isRecommendedArticle(art)) {
                    tagHot.setVisibility(View.VISIBLE);
                    ((ImageView) tagHot).setImageResource(R.drawable.ic_recommended);
                } else if (ArticleApi.isHotArticle(art)) {
                    tagHot.setVisibility(View.VISIBLE);
                    ((ImageView) tagHot).setImageResource(R.drawable.ic_whats_hot);
                }

                else
                    tagHot.setVisibility(View.GONE);
            } else {
                TextView rank = (TextView) convertView.findViewById(R.id.rank);
                if (position < 10) {
                    rank.setVisibility(View.VISIBLE);
                    rank.setText(String.valueOf(position + 1));
                    int rankColorIndex = position > 3? 3:position;
                    rank.setBackgroundColor(rankColors[rankColorIndex]);
                } else
                    rank.setVisibility(View.GONE);
            }
            String tip = String.format(" %s / %d条评论，%d人围观", AcApp.getPubDate(art.releaseDate),art.comments,art.views);
//            holder.postTime.setText(AcApp.getPubDate(art.releaseDate));
            holder.postTime.setText(tip);
            return convertView;
        }
        int rankColors[] = {0xffcc0000,0xffff4444,0xffff8800,0xffffbb33};
        
    }
    
    static class ListViewHolder{
        TextView comments,
        postTime,
        channel,
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
        ((HomeFragment)mContentFragment).changeContentListMode(itemPosition);
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v == mAvatarFrame) {
            if(mUser != null)
                startActivityForResult(new Intent(this,ProfileActivity.class),2);
            else
                startActivityForResult(
                    SigninActivity.createIntent(getApplicationContext()),
                    SigninActivity.REQUEST_SIGN_IN);
        }
//        else if(v.getId() == R.id.signout || v.getId() == R.id.avatar && mUser != null){
//            AcApp.logout();
//            invalidateAvatarFrame();
//            AcApp.showToast("注销");
//        }

    }

    /**
     *  Remove current avatar frame..
     */
    private void invalidateAvatarFrame() {
        mUser = null;
        ((LinearLayout)mDrawer).removeViewAt(0);
        mAvatarFrame = getLayoutInflater().inflate(R.layout.avatar_frame, (LinearLayout)mDrawer,false);
        mAvatarFrame.setOnClickListener(this);
        ((LinearLayout)mDrawer).addView(mAvatarFrame, 0);
    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        if(result == RESULT_OK ){
            if(request == SigninActivity.REQUEST_SIGN_IN){
                mUser = data.getExtras().getParcelable("user");
                setUserInfo();
            }else{
                invalidateAvatarFrame();
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
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
