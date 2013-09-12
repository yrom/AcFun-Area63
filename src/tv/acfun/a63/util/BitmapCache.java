package tv.acfun.a63.util;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.toolbox.ImageLoader.ImageCache;

public class BitmapCache implements ImageCache {

    private LruCache<String, Bitmap> mCache;

    private static final int DEF_MAX_SIZE = 10 << 20;
    private static int MAX_SIZE;
    static {
        MAX_SIZE = (int) (Runtime.getRuntime().maxMemory() / 8);
        if (MAX_SIZE > DEF_MAX_SIZE) {
            MAX_SIZE = DEF_MAX_SIZE;
        }
    }

    public BitmapCache() {
        this(MAX_SIZE);

    }

    public BitmapCache(int maxSize) {
        if(maxSize > MAX_SIZE){
            maxSize = MAX_SIZE;
        }
        mCache = new LruCache<String, Bitmap>(maxSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
            
        };
    }

    @Override
    public Bitmap getBitmap(String url) {
        return mCache.get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        mCache.put(url, bitmap);
    }
}
