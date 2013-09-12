package tv.acfun.a63.util;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.android.volley.toolbox.ImageLoader.ImageCache;

public class BitmapCache implements ImageCache {
    
    private LruCache<String, Bitmap> mCache;
    
    private static final int DEF_MAX_SIZE = 10 * 1024 * 1024;
    public BitmapCache() {
        this(DEF_MAX_SIZE);
        
    }
    public BitmapCache(int maxSize){
        mCache = new LruCache<String, Bitmap>(maxSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };
    }

    @Override
    public Bitmap getBitmap(String url) {
        Log.d("BitmapCache", "get "+url);
        return mCache.get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        mCache.put(url, bitmap);
        Log.d("BitmapCache", "put="+url+";size="+mCache.size());
    }

}