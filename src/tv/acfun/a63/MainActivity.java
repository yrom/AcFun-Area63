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
import tv.acfun.a63.service.PushService;
import tv.acfun.a63.util.ActionBarUtil;
import tv.acfun.a63.util.DensityUtil;
import tv.acfun.a63.util.FastJsonRequest;
import tv.acfun.a63.util.TextViewUtils;
import tv.acfun.a63.util.Theme;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

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
import com.astuetz.PagerSlidingTabStrip;
import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.FeedbackAgent;
import com.umeng.fb.model.Conversation;
import com.umeng.fb.model.Conversation.SyncListener;
import com.umeng.fb.model.DevReply;
import com.umeng.fb.model.Reply;
import com.umeng.update.UmengUpdateAgent;

public class MainActivity extends ActionBarActivity implements
        OnItemClickListener, OnNavigationListener, OnClickListener {
    private static final String KEY_CURRENT_ITEM = "current_item";
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
        ActionBarUtil.compatibleDeviceWithSB(this);
        Theme.onActivityCreate(this,savedInstanceState);
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
        
        PushService.start(this);
        // umeng
        initUmeng();
    }

    private void initUmeng() {
        MobclickAgent.setDebugMode(BuildConfig.DEBUG);
        UmengUpdateAgent.update(this);
        MobclickAgent.onError(this);
        SyncListener listener = new Conversation.SyncListener() {

            @Override
            public void onSendUserReply(List<Reply> replyList) {
            }

            @Override
            public void onReceiveDevReply(List<DevReply> replyList) {
                if(replyList == null || replyList.isEmpty()){
                    return;
                }
                Intent intent = new Intent(MainActivity.this, ConversationActivity.class);
                String text = replyList.get(0).getContent();
                AcApp.showNotification(intent, R.id.comments_content, text, R.drawable.notify_chat, getString(R.string.new_replay));
            }
        };
        new FeedbackAgent(this).getDefaultConversation().sync(listener);
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
        int[] iconIds = { R.drawable.ic_home,
                 R.drawable.ic_hot, R.drawable.ic_heart,  R.drawable.ic_at, R.drawable.ic_action_search};
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
        int position = 0;
        if (savedInstanceState != null){
            position = savedInstanceState.getInt(KEY_CURRENT_ITEM, 0);
        }
        if(position == 0)
            setActionbarNavigation();
        selectItem(position);
        if (AcApp.getConfig().getBoolean("is_first_open", true)) {
            mDrawerLayout.openDrawer(mDrawer);
            AcApp.putBoolean("is_first_open", false);
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_CURRENT_ITEM, mCurrentNavPosition);
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
            if(mUser.isExpired()){
                new AlertDialog.Builder(this)
                .setTitle(R.string.account_expired)
                .setMessage(R.string.msg_account_expired)
                .setPositiveButton(R.string.logout, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        AcApp.logout();
                        invalidateAvatarFrame();
                    }
                }).show();
            }
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
        switch (position) {
        case 1:
            MobclickAgent.onEvent(this, "rank");
            f = new RankListFragment();
            args.putInt(RankListFragment.ARG_LIST_MODE, 3);
            args.putInt(RankListFragment.ARG_SECTION_NUMBER, 4);
            f.setHasOptionsMenu(false);
            break;
        case 2:
            MobclickAgent.onEvent(this, "fav");
            f = new FavListFragment();
            break;
        case 4:
            MobclickAgent.onEvent(this, "search");
            f = new SearchFragment();
            break;
        case 0:
        default:
            MobclickAgent.onEvent(this, "main");
            f = new HomeFragment();
            args.putInt(HomeFragment.ARG_PLANET_NUMBER, position);
            args.putStringArray(HomeFragment.ARG_TITLES, mTitles);
            break;
        }

        f.setArguments(args);
        mFragments.set(position,f);
        return f;
    }
    private void selectItem(int position) {
        Fragment f = getFragment(position);
        switchContent(mContentFragment, f);
        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mPlanetTitles[position]);
        mDrawerLayout.closeDrawer(mDrawer);
        mCurrentNavPosition = position;
    }

    private void setActionbarNavigation() {
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
        mBar.setSelectedNavigationItem(AcApp.getConfig().getInt("nav_item", 0));
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
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    /**
     * 四个频道列表
     *
     */
    public static class SectionsPagerAdapter extends FragmentPagerAdapter {
        String[] titles;
        int contentListMode;
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
            contentListMode = AcApp.getConfig().getInt("nav_item", 0);
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
    public static class HomeFragment extends Fragment {
        public static final String ARG_TITLES = "titles";

        public static final String ARG_PLANET_NUMBER = "planet_number";

        private static int VIEW_MODE_CODE;
        private MenuItem mModeMenu;

        private SectionsPagerAdapter mSectionsPagerAdapter;

        private ViewPager mViewPager;

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
            mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
            mViewPager.setOffscreenPageLimit(2);
            mViewPager.setAdapter(mSectionsPagerAdapter);
            tabs.setViewPager(mViewPager);
            return rootView;
        }
        
        public void changeContentListMode(int itemPosition) {
            if(BuildConfig.DEBUG)
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
                        holder.comments.setText(R.string.no_desc);
                    }
                    String tip = String.format("于%s收藏，有%d人同好", AcApp.getPubDate(content.releaseDate),content.stows);
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
     * 搜索
     */
    public static class SearchFragment extends Fragment implements OnClickListener, OnEditorActionListener, 
            OnScrollListener,OnItemClickListener {
        private View mBtnClear;
        private EditText mSearchText;
        private View mProgress;
        private ListView mResultList;
        private LayoutInflater mInflater;
        private ArticleListAdapter mAdapter;
        
        private TextWatcher watcher = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    mBtnClear.setVisibility(View.VISIBLE);
                } else {
                    mBtnClear.setVisibility(View.GONE);
                    mProgress.setVisibility(View.GONE);
                }
                mResultList.setVisibility(View.GONE);
                if(mAdapter != null && mAdapter.contents != null)
                    mAdapter.contents.clear();
            }
        };
        protected int mPage;
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            this.mInflater = inflater;
            return inflater.inflate(R.layout.fragment_search, container,false);
        }
        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            mBtnClear = view.findViewById(R.id.btn_search_clear);
            mBtnClear.setOnClickListener(this);
            mSearchText = (EditText) view.findViewById(R.id.search_text);
            mSearchText.addTextChangedListener(watcher);
            mSearchText.setOnEditorActionListener(this);
            mProgress = view.findViewById(R.id.search_plate_progress);
            mResultList = (ListView) view.findViewById(android.R.id.list);
            mResultList.setOnScrollListener(this);
            mResultList.setOnItemClickListener(this);
        }
        @Override
        public void onClick(View v) {
            if(v == mBtnClear){
                mSearchText.setText("");
            }
        }
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if(actionId == EditorInfo.IME_ACTION_SEARCH 
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                startSearch(v.getText().toString(),1);
            }
            return false;
        }
        private int mTotalCount;
        Listener<Contents> listener = new Listener<Contents>() {
            @Override
            public void onResponse(Contents response) {
                mResultList.setVisibility(View.VISIBLE);
                if(mAdapter == null){
                    mAdapter = new ArticleListAdapter(mInflater,response.getContents(),0);
                }
                if(mPage <= 1){
                    mAdapter.contents = response.getContents();
                    mResultList.setAdapter(mAdapter);
                }else{
                    mAdapter.addData(response.getContents());
                }
                mProgress.setVisibility(View.GONE);
                mAdapter.notifyDataSetChanged();
                mTotalCount = response.totalcount;
                
            }};
        ErrorListener errorListner;
        private boolean mLastItemVisible;
        private void startSearch(String query, int page) {
            mProgress.setVisibility(View.VISIBLE);
            mPage = page;
            String url = ArticleApi.getSearchUrl(query, 2, 1, mPage, 20);
            if (BuildConfig.DEBUG) Log.d(TAG, "query url=" + url);
            Request<?> request = new FastJsonRequest<Contents>(url, Contents.class, listener, errorListner);
            AcApp.addRequest(request);
        }
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && mLastItemVisible) {
                startSearch(mSearchText.getText().toString(),mPage+1);
            }
        }
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            mLastItemVisible = (totalItemCount > 0)
                    && (firstVisibleItem + visibleItemCount >= totalItemCount - 1)
                    && totalItemCount < mTotalCount;
        }
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            startViewArticle(parent, position);
            
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
        
        if(rankList.getIntValue("status") != 200){
            return null;
        }
        JSONArray jsonArr = rankList.getJSONObject("data").getJSONObject("page").getJSONArray("list");
        
        List<Content> contents = new ArrayList<Content>();
        for(int i=0;i<jsonArr.size();i++){
            JSONObject carr = jsonArr.getJSONObject(i);
            Content c = new Content();
            c.aid = carr.getIntValue("contentId");
            c.title =carr.getString("title");
            c.description = carr.getString("description");
            c.releaseDate = carr.getLongValue("releaseDate");
            c.views = carr.getIntValue("views");
            c.comments = carr.getIntValue("comments");
            c.stows = carr.getIntValue("stows");
            c.channelId = carr.getIntValue("channelId");
            contents.add(c);
        }
        
        Contents cs = new Contents();
        cs.setContents(contents);
        cs.totalpage = 1;
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
                if(BuildConfig.DEBUG) Log.d(TAG, String.format("[%d] framgent change Content ListMode =%d",section,listMode));
                if(list != null) // ensure list
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
                    if(BuildConfig.DEBUG) 
                        Log.d(TAG, String.format("[%d] on refresh ",section));
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
                        if(hasNextPage()){
                            if(BuildConfig.DEBUG)
                                Log.d(TAG, String.format("[%d] 加载下一页, mode=%d",section,listMode));
                            loadData(false,false);
                        }else{
                            timeOut.setVisibility(View.GONE);
                            TextView text = (TextView) footView.findViewById(R.id.list_footview_text);
                            text.setText(R.string.no_more);
                            footView.findViewById(R.id.list_footview_progress).setVisibility(View.GONE);
                            footView.setOnClickListener(null);
                        }
                    }
                }
            });
            list.setOnItemClickListener(this);
            return rootView;
        }

        protected boolean hasNextPage() {
            // 排行版只加载一页
            return listMode != 3;
        }

        boolean isShowing;
        @Override
        public void onResume() {
            super.onResume();
            if(BuildConfig.DEBUG) 
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
                if(adapter == null){
                    adapter = new ArticleListAdapter(inflater,response.getContents(),listMode);
                }
                if(page <= 1){
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
                    if(BuildConfig.DEBUG) Log.d(TAG, String.format("[%d] 刷新数据",section));
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
                return ArticleApi.getRankListUrl();
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
                if(BuildConfig.DEBUG)
                    Log.d(TAG, String.format("[%d] destory adapter,size=%d", section,adapter.getCount()));
                adapter.contents.clear();
                adapter = null;
            }
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            startViewArticle(parent, position);
        }

    }
    private static void startViewArticle(AdapterView<?> list, int position) {
        Object obj = list.getItemAtPosition(position);
        if(obj != null && obj instanceof Content){
            Content c = (Content)obj;
            ArticleActivity.start(list.getContext(), c.aid,c.title);
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
            if(!TextUtils.isEmpty(art.description)){
                CharSequence text = null;
                try{
                    text = Html.fromHtml(TextViewUtils.getSource(art.description));
                    // catch java.io.IOException: Pushback buffer full
                    //  at android.text.HtmlToSpannedConverter.convert(Html.java:438)
                    //  at android.text.Html.fromHtml(Html.java:138)
                    //  at android.text.Html.fromHtml(Html.java:101)
                }catch(Exception e){
                    text = TextViewUtils.getSource(art.description);
                }
                holder.comments.setText(text);
            }
            else{
                holder.comments.setText(R.string.no_desc);
            }
            
            if (mode < 3) {
                View tagHot = convertView.findViewById(R.id.item_tag);
                if (ArticleApi.isRecommendedArticle(art)) {
                    tagHot.setVisibility(View.VISIBLE);
                    ((ImageView) tagHot).setImageResource(Theme.isNightMode() ? R.drawable.ic_recommended_dark : R.drawable.ic_recommended);
                } else if (ArticleApi.isHotArticle(art)) {
                    tagHot.setVisibility(View.VISIBLE);
                    ((ImageView) tagHot).setImageResource(Theme.isNightMode() ? R.drawable.ic_whats_hot_dark : R.drawable.ic_whats_hot);
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
        if(position == arg0.getCount() -2){
            MentionActivity.start(this);
            mDrawerList.setItemChecked(mCurrentNavPosition, true);
            return;
        }
        if (mCurrentNavPosition != position)
            selectItem(position);
    }
    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        if(mContentFragment == null){
            mContentFragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        }
        if(mContentFragment!= null && mContentFragment instanceof HomeFragment)
            ((HomeFragment)mContentFragment).changeContentListMode(itemPosition);
        else
            MobclickAgent.reportError(this.getApplicationContext(), 
                    String.format("onNavigationItemSelected: %d, but mContentFragment isn't HomeFragment: %s",
                            itemPosition, String.valueOf(mContentFragment)));
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
        avatar = (ImageView) mAvatarFrame.findViewById(R.id.avatar);
        nameText = (TextView) mAvatarFrame.findViewById(R.id.user_name);
        signatureText = (TextView) mAvatarFrame.findViewById(R.id.signature);
    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        if(result == RESULT_OK ){
            if(request == SigninActivity.REQUEST_SIGN_IN){
                mUser = data.getExtras().getParcelable("user");
                setUserInfo();
                startActivity(new Intent(this,ProfileActivity.class));
            }else{
                invalidateAvatarFrame();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(mDrawerLayout.isDrawerOpen(mDrawer)){
            mDrawerLayout.closeDrawer(mDrawer);
        }else if(mCurrentNavPosition != 0 ){
            selectItem(0);
            mDrawerToggle.onDrawerClosed(mDrawer);
        }else
            super.onBackPressed();
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
