package tv.acfun.a63;

import java.io.File;

import tv.acfun.a63.api.entity.User;
import tv.acfun.a63.db.DB;
import tv.acfun.a63.util.BitmapCache;
import tv.acfun.a63.util.Connectivity;
import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RequestQueue.RequestFilter;
import com.android.volley.toolbox.ImageLoader;

/**
 * 自定义Application
 * 
 * @author Yrom
 */
public class AcApp extends Application {

    private static Context mContext;
    private static Resources mResources;
    private static String mSDcardDir, mExternalFilesDir;
    private static AcApp instance;
    private static SharedPreferences sp;
    public static final String LOG = "Logs";
    public static final String IMAGE = "Images";
    public static final String PIC = "Pictures";
    public static float density = 1f;
    private static NotificationManager mNotiManager;
    private static RequestQueue mQueue;
    private static ImageLoader mImageLoader;
    private static BitmapCache mBitmapCache;
    /**
     * <b>NOTE:</b>在 <code>getApplicationContext()</code> 调用一次之后才能用这个方便的方法
     */
    public static AcApp instance() {
        return instance;
    }


    public void onCreate() {
        super.onCreate();
        mContext = instance = this;
        mResources = getResources();
        density = mResources.getDisplayMetrics().density;
        mQueue = Connectivity.newRequestQueue();
        mBitmapCache = new BitmapCache();
        mImageLoader = new ImageLoader(mQueue, mBitmapCache);
        sp = PreferenceManager.getDefaultSharedPreferences(mContext);
    }
    public static User getUser(){
        return new DB(mContext).getUser();
    }

    public static void logout() {
        new DB(mContext).logout();
    }
    
    // ====================================
    // bitmap
    // ====================================
    
    public static Bitmap getBitmpInCache(String url){
        String key = getCacheKey(url, 0, 0);
        return mBitmapCache.getBitmap(key);
    }
    
    public static Bitmap decodeBitmap(String imagePath, Bitmap.Config config){
        
        Options opts = new Options();
        opts.inPreferredConfig = config;
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, opts);
        return bitmap;
    }
    /**
     * copy from Volley - Image loader
     * @param url
     * @param maxWidth
     * @param maxHeight
     * @return
     */
    public static String getCacheKey(String url, int maxWidth, int maxHeight) {
        return new StringBuilder(url.length() + 12).append("#W").append(maxWidth)
                .append("#H").append(maxHeight).append(url).toString();
    }
    
    public static void putBitmapInCache(String url, Bitmap value){
        String key = getCacheKey(url, 0, 0);
        mBitmapCache.putBitmap(key, value);
    }
    
    // ====================================
    // volley
    // ====================================
    public static void addRequest(Request<?> request){
        mQueue.add(request);
    }
    
    public static void cancelAllRequest(RequestFilter filter){
        mQueue.cancelAll(filter);
    }
    
    public static void cancelAllRequest(Object tag){
        if (BuildConfig.DEBUG) {
            Log.i("AC", "cancel all by tag: "+tag);
        }
        mQueue.cancelAll(tag);
    }
    
    public static RequestQueue getGloableQueue(){
        if(mQueue == null)
            mQueue = Connectivity.newRequestQueue();
        return mQueue;
    }
    
    public static ImageLoader getGloableLoader(){
        if(mImageLoader == null){
            mImageLoader = new ImageLoader(getGloableQueue(),new BitmapCache());
        }
        return mImageLoader;
    }
    
    public static byte[] getDataInDiskCache(String key){
        Cache.Entry entry = getGloableQueue().getCache().get(key);
        return entry ==null? null : entry.data;
    }
    
    @Override
    public void onLowMemory() {
        System.gc();
        super.onLowMemory();
    }

    private String versionName = "";

    public String getVersionName() {
        if (TextUtils.isEmpty(versionName)) {
            PackageInfo info = null;
            try {
                info = getPackageManager().getPackageInfo(getPackageName(), 0);
                versionName = info.versionName;
                return versionName;
            } catch (Exception e) {
            }
            return "";
        } else
            return versionName;
    }

    // ====================================
    // config SharedPreferences
    // ====================================

    public static SharedPreferences getConfig() {
        return sp;
    }

    public static void putString(String key, String value) {
        sp.edit().putString(key, value).commit();
    }

    public static void putBoolean(String key, boolean value) {
        sp.edit().putBoolean(key, value).commit();
    }

    public static void putInt(String key, int value) {
        sp.edit().putInt(key, value).commit();
    }

    public static void putFloat(String key, float value) {
        sp.edit().putFloat(key, value).commit();
    }
    // FIXME : hard code
    public static int getViewMode() {
        return sp.getInt("view_mode", 0);
    }
    
    public static int getNumOfFloors(){
        return sp.getInt("num_of_floor", 50);
    }
    public static boolean isViratorEnabled(){
        return sp.getBoolean("enable_vibrator", true);
    }
    
    static int lastSize;
    public static int getPreferenceFontSize() {
        int preference = sp.getInt("text_size", 0);
        if(lastSize == preference && lastSize> 0){
            return lastSize;
        }
        lastSize = (7 + preference*(preference + 1)) << 1;
        return lastSize;
        
    }
    public static long getPreferenceRefreshingInterval(){
        int mins = Integer.parseInt(sp.getString("mention_interval", "15"));
        return mins * _1_min;
    }
    
    public static long getOnlineInterval(){
        return 5 * _1_min;
    }
    public static boolean isMentionEnabled(){
        return sp.getBoolean("mention_enable", true);
    }
    public static boolean isMentionWifiOnly(){
        return sp.getBoolean("mention_enable_wifi_only", true);
    }
    // ====================================
    // statics
    // ====================================

    public static Context context() {
        return mContext;
    }

    public static Resources getR() {
        return mResources;
    }

    /**
     * 外部存储是否可用
     */
    public static boolean isExternalStorageAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState());
    }

    /**
     * 获得外部存储的files目录 <br/>
     * <b>NOTE:</b>请先调用 {@link #isExternalStorageAvailable()} 判断是否可用
     * 
     * @return
     */
    public static String getExternalFilesDir() {
        if (mExternalFilesDir == null)
            mExternalFilesDir = mContext.getExternalFilesDir(null)
                    .getAbsolutePath();
        return mExternalFilesDir;
    }

    /**
     * 获得缓存目录 <br>
     * <b>NOTE:</b>请先调用 {@link #isExternalStorageAvailable()} 判断是否可用
     * 
     * @param type
     *            {@link #IMAGE} {@link #VIDEO} and so on.
     * @return
     */
    public static File getExternalCacheDir(String type) {
        File cacheDir = new File(mContext.getExternalCacheDir(), type);
        cacheDir.mkdirs();
        return cacheDir;
    }
    /**
     * 获得图片保存路径
     * @return 默认值 {@code getDefaultImageSaveDir();}
     */
    public static String getPreferenceImageSaveDir(){
        return sp.getString("image_cache", getDefaultImageSaveDir());
        
    }
    public static String getDefaultImageSaveDir(){
        return getSDcardDir(PIC);
    }
    /**
     * 获得SDcard根目录 <br>
     * <b>NOTE:</b>请先调用 {@link #isExternalStorageAvailable()} 判断是否可用
     * 
     * @return SDcard Dir
     */
    public static String getSDcardDir() {
        if (mSDcardDir == null)
            mSDcardDir = Environment.getExternalStorageDirectory()
                    .getAbsolutePath();
        return mSDcardDir;
    }

    public static String getSDcardDir(String type){
        return new File(getSDcardDir(),type).getAbsolutePath();
    }
    /**
     * 获取当前默认的日期时间显示 eg. 20130411-110445
     * 
     * @return
     */
    public static String getCurDateTime() {
        return getCurDateTime("yyyyMMdd-kkmmss", System.currentTimeMillis());
    }
    
    public static String getDateTime(long msec){
        return getCurDateTime("MM月dd日 kk:mm", msec);
    }
    
    /**
     * 获取当前日期时间
     * 
     * @param format
     *            {@link android.text.format.DateFormat}
     * @return
     */
    public static String getCurDateTime(CharSequence format, long msec) {
        return DateFormat.format(format, msec).toString();
    }

    public static void showToast(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(String msg, Object... args) {
        Toast.makeText(mContext, String.format(msg, args), Toast.LENGTH_SHORT)
                .show();
    }

    public static void addSearchView(Activity activity,
            Menu menu) {
        android.support.v7.widget.SearchView searchView = new android.support.v7.widget.SearchView(activity);
        searchView.setSubmitButtonEnabled(true);
        SearchManager searchManager = (SearchManager) activity
                .getSystemService(Context.SEARCH_SERVICE);
        SearchableInfo info = searchManager.getSearchableInfo(activity
                .getComponentName());
        searchView.setSearchableInfo(info);
        
        MenuItem item = menu.add("Search")
                .setIcon(R.drawable.action_search);
        MenuItemCompat.setActionView(item, searchView);
        MenuItemCompat.setShowAsAction(item,
                        MenuItemCompat.SHOW_AS_ACTION_IF_ROOM
                                | MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
    }

    public static void showNotification(Intent mIntent, int notificationId,
            String text, int icon, CharSequence title) {
        showNotification(mIntent, notificationId, text, icon, title,
                Notification.FLAG_AUTO_CANCEL);
    }

    @SuppressWarnings("deprecation")
    public static void showNotification(Intent mIntent, int notificationId,
            String text, int icon, CharSequence title, int flag) {
        Notification notification = new Notification(icon, text,
                System.currentTimeMillis());
        mIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
                mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setLatestEventInfo(mContext, title, text, contentIntent);
        notification.flags |= flag;
        if (mNotiManager == null)
            mNotiManager = (NotificationManager) mContext
                    .getSystemService(NOTIFICATION_SERVICE);
        mNotiManager.notify(notificationId, notification);
    }
    
    public static final long _1_min = 60 * 1000;
    public static final long _1_hour = 60 * _1_min;
    public static final long _24_hour = 24 * _1_hour;
    public static String getPubDate(long postTime) {
        long delta = System.currentTimeMillis() - postTime;
        if( delta <  _24_hour && delta >= _1_hour){
            int time = (int) (delta / _1_hour);
            return time+"小时前 ";
        } else if( delta < _1_hour && delta >= _1_min){
            int time = (int) (delta / _1_min);
            return time+"分钟前 ";
        } else if( delta < _1_min){
            return "刚刚 ";
        } else {
            int time = (int) (delta / _24_hour);
            if(time <= 6){
                return time+"天前 " ;
            }else{
                return getDateTime(postTime);
            }
        }
    }


}
