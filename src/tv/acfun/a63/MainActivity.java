package tv.acfun.a63;

import tv.acfun.a63.util.ActionBarUtil;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;

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

    private static int mode_code;

    private MenuItem mModeMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBarUtil.forceShowActionBarOverflowMenu(this);
        mBar = getSupportActionBar();
        ActionBarUtil.setXiaomiFilterDisplayOptions(getSupportActionBar(), true);
        mTitle = getTitle();
        mPlanetTitles = getResources().getStringArray(R.array.planets);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawer = findViewById(R.id.left_drawer);
        mAvatarFrame = findViewById(R.id.avatar_frame);
        mAvatarFrame.setOnClickListener(this);
        mDrawerList = (ListView) findViewById(R.id.list);

        // set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        
        int[] iconIds = {R.drawable.ic_home, R.drawable.ic_bell,R.drawable.ic_heart};
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new NavigationAdapter(mPlanetTitles, iconIds ));
        mDrawerList.setOnItemClickListener(this);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
            mDrawerLayout, /* DrawerLayout object */
            R.drawable.ic_navigation_drawer, /* nav drawer image to replace 'Up' caret */
            R.string.app_name_open, /* "open drawer" description for accessibility */
            R.string.app_name /* "close drawer" description for accessibility */
            ) {
            public void onDrawerClosed(View view) {
                mBar.setTitle(mTitle);
                supportInvalidateOptionsMenu(); // creates call to
                                                // onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                mBar.setTitle(R.string.app_name_open);
                supportInvalidateOptionsMenu(); // creates call to
                                                // onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mTitles = getResources().getStringArray(R.array.titles);
        if (savedInstanceState == null) {
            selectItem(0);
        }
    }

    private void selectItem(int position) {
        Fragment fragment = new PlanetFragment();
        Bundle args = new Bundle();
        args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
        if (position == 0){
            mBar.setDisplayShowTitleEnabled(false);
            args.putStringArray(PlanetFragment.ARG_TITLES, mTitles);
            mBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(mBar.getThemedContext(),
                R.layout.list_item_2,
                android.R.id.text2, getResources().getStringArray(R.array.modes)){
    
                    @Override
                    public View getView(int position, View convertView,
                            ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        TextView text = (TextView) view.findViewById(android.R.id.text1);
                        text.setText(mPlanetTitles[0]);
                        return view;
                    }
            
                };
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
            mBar.setListNavigationCallbacks(adapter, this);
            
        }else{
            mBar.setDisplayShowTitleEnabled(true);
            mBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        }
        fragment.setArguments(args);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment).commit();

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mPlanetTitles[position]);
        mDrawerLayout.closeDrawer(mDrawer);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content
        // view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawer);
        // menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
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
        case R.id.mode_mix:
            mode_code = 0;
            Toast.makeText(this, "图文模式", Toast.LENGTH_SHORT).show();
            break;
        case R.id.mode_no_image:
            mode_code = 1;
            Toast.makeText(this, "文本模式", Toast.LENGTH_SHORT).show();
            break;
        case R.id.mode_comic:
            mode_code = 2;
            Toast.makeText(this, "漫画模式", Toast.LENGTH_SHORT).show();
            break;
        } 
        setMenuIcon();
        return super.onOptionsItemSelected(item);
    }
    private void setMenuIcon(){
        switch (mode_code) {
        case 1:
            mModeMenu.setIcon(R.drawable.mode_no_pic);
            break;
        case 2:
            mModeMenu.setIcon(R.drawable.mode_comic);
            break;
        case 0:
        default:
            mModeMenu.setIcon(R.drawable.mode_mix);
            break;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.main, menu);
        mModeMenu = menu.findItem(R.id.action_view_mode);
        setMenuIcon();
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
            // getItem is called to instantiate the fragment for the given page.
            // Return a DummySectionFragment (defined as a static inner class
            // below) with the page number as its lone argument.
            Fragment fragment = new DummyCardFragment();
            Bundle args = new Bundle();
            args.putInt(DummyCardFragment.ARG_SECTION_NUMBER, position + 1);
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
    public static class PlanetFragment extends Fragment {
        public static final String ARG_TITLES = "titles";

        public static final String ARG_PLANET_NUMBER = "planet_number";

        private SectionsPagerAdapter mSectionsPagerAdapter;

        private ViewPager mViewPager;

        private PagerSlidingTabStrip mTabs;

        public PlanetFragment() {
            // Empty constructor required for fragment subclasses
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            int i = getArguments().getInt(ARG_PLANET_NUMBER);
            View rootView = null;
            if (i == 0) {
                rootView = inflater.inflate(R.layout.fragment_home, container,
                        false);
                mSectionsPagerAdapter = new SectionsPagerAdapter(
                        getChildFragmentManager(), getArguments()
                                .getStringArray(ARG_TITLES));
                mTabs = (PagerSlidingTabStrip) rootView.findViewById(R.id.tabs);
                // Set up the ViewPager with the sections adapter.
                mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
                mViewPager.setAdapter(mSectionsPagerAdapter);
//                mTabs.setIndicatorColorResource(R.color.main_color);
//                mTabs.setTextColorResource(R.color.primary_text_color);
                mTabs.setViewPager(mViewPager);
            } else {
                rootView = inflater.inflate(R.layout.fragment_planet,
                        container, false);
                ((TextView) rootView).setText(mPlanetTitles[i]);
            }
            return rootView;
        }
    }

    /**
     * A dummy fragment representing a section of the app, but that simply
     * displays dummy text.
     */
    public static class DummyCardFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        public DummyCardFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_dummy,
                    container, false);
            TextView dummyTextView = (TextView) rootView
                    .findViewById(R.id.section_label);
            dummyTextView.setText(Integer.toString(getArguments().getInt(
                    ARG_SECTION_NUMBER)));

            return rootView;
        }
    }
    static class NavigationItem{
        String title;
        Drawable icon;
    }
    public class NavigationAdapter extends BaseAdapter{
        NavigationItem[] navs;
        public NavigationAdapter(String[] titles, int[] iconIds) {
            navs = new NavigationItem[titles.length];
            for(int i=0;i<titles.length && i< iconIds.length;i++){
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
            if(convertView == null){
                convertView = getLayoutInflater().inflate(R.layout.navigation_list_item, parent, false);
            }
            ImageView iconView = (ImageView) convertView.findViewById(R.id.icon);
            TextView titleView = (TextView) convertView.findViewById(R.id.text);
            iconView.setImageDrawable(item.icon);
            titleView.setText(item.title);
            return convertView;
        }
        
    }
    
    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position,
            long arg3) {
        selectItem(position);
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        
        Log.i(TAG, "click position = "+itemPosition);
        
        return false;
    }

    @Override
    public void onClick(View v) {
        if(v == mAvatarFrame){
            startActivityForResult(SigninActivity.createIntent(getApplicationContext()),SigninActivity.REQUEST_SIGN_IN);
        }
        
    }
    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        Log.i(TAG, String.format("request=%d,result=%d", request,result));
        
        
    }
}
