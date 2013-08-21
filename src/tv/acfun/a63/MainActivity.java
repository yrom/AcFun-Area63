package tv.acfun.a63;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;

@SuppressLint("NewApi")
public class MainActivity extends SherlockFragmentActivity implements OnItemClickListener {
    private static String[] mPlanetTitles;

    private DrawerLayout mDrawerLayout;

    private ListView mDrawerList;

    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mTitle;


    private String[] mTitles;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE 
                | ActionBar.DISPLAY_SHOW_HOME 
                | ActionBar.DISPLAY_HOME_AS_UP);
        mTitle  = getTitle();
        //xxx 
        
        mPlanetTitles = getResources().getStringArray(R.array.planets);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mPlanetTitles));
        mDrawerList.setOnItemClickListener(this);

        
        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.app_name_open,  /* "open drawer" description for accessibility */
                R.string.app_name  /* "close drawer" description for accessibility */
                ) {


            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(R.string.app_name_open);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
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
        if(position == 0) args.putStringArray(PlanetFragment.ARG_TITLES, mTitles);
        fragment.setArguments(args);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mPlanetTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }
    
    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
//        menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (item.getItemId() == android.R.id.home) {
            
            if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                mDrawerLayout.closeDrawer(mDrawerList);
            } else {
                mDrawerLayout.openDrawer(mDrawerList);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public static class SectionsPagerAdapter extends FragmentPagerAdapter {
        String[] titles;
        public SectionsPagerAdapter(FragmentManager fragmentManager,String[] titles) {
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
            
            if(position<titles.length)
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
            if(i == 0){
                rootView = inflater.inflate(R.layout.fragment_home, container, false);
                mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager(),
                        getArguments().getStringArray(ARG_TITLES));
                mTabs = (PagerSlidingTabStrip) rootView.findViewById(R.id.tabs);
                // Set up the ViewPager with the sections adapter.
                mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
                mViewPager.setAdapter(mSectionsPagerAdapter);
                mTabs.setIndicatorColorResource(R.color.main_color);
                mTabs.setTextColorResource(R.color.primary_text_color);
                mTabs.setViewPager(mViewPager);
            }else{
                rootView = inflater.inflate(R.layout.fragment_planet, container, false);
                ((TextView)rootView).setText(mPlanetTitles[i]);
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
    
    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        selectItem(position);
    }

}
