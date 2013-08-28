package tv.acfun.a63.util;

import java.io.File;

import org.json.JSONObject;

import tv.acfun.a63.AcApp;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.util.Log;

import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;

/**
 * 
 * @author Yrom
 * 
 */
public class Connectivity {
    private static final String DEFAULT_CACHE_DIR = "acfun";

    /**
     * Creates a default instance of the worker pool and calls
     * {@link RequestQueue#start()} on it.
     * 
     * @param stack
     *            An {@link HttpStack} to use for the network, or null for
     *            default.
     * @return A started {@link RequestQueue} instance.
     */
    public static RequestQueue newRequestQueue(HttpStack stack) {

        File cacheDir = AcApp.isExternalStorageAvailable() ? AcApp
                .getExternalCacheDir(DEFAULT_CACHE_DIR) : new File(AcApp
                .context().getCacheDir(), DEFAULT_CACHE_DIR);
        Log.i(DEFAULT_CACHE_DIR, cacheDir.getAbsolutePath());
        String userAgent = "acfun/1.0";

        if (stack == null) {
            if (Build.VERSION.SDK_INT >= 9) {
                stack = new HurlStack();
            } else {
                // Prior to Gingerbread, HttpUrlConnection was unreliable.
                // See:
                // http://android-developers.blogspot.com/2011/09/androids-http-clients.html
                stack = new HttpClientStack(
                        AndroidHttpClient.newInstance(userAgent));
            }
        }

        Network network = new BasicNetwork(stack);

        RequestQueue queue = new RequestQueue(new DiskBasedCache(cacheDir),
                network);
        queue.start();

        return queue;
    }

    /**
     * Creates a default instance of the worker pool and calls
     * {@link RequestQueue#start()} on it.
     * 
     * @return A started {@link RequestQueue} instance.
     */
    public static RequestQueue newRequestQueue() {
        return newRequestQueue(null);
    }

    public static JsonObjectRequest newJsonObjectRequest(String url,
            Listener<JSONObject> listener, ErrorListener errorListener) {
        return new JsonObjectRequest(url, null, listener, errorListener);
    }
    
    
    
}
