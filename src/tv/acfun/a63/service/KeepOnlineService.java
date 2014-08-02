package tv.acfun.a63.service;

import java.util.Map;

import org.apache.commons.httpclient.Cookie;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

import tv.acfun.a63.AcApp;
import tv.acfun.a63.BuildConfig;
import tv.acfun.a63.api.ArticleApi;
import tv.acfun.a63.api.entity.User;
import tv.acfun.a63.util.Connectivity;
import tv.acfun.a63.util.UsingCookiesRequest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

public class KeepOnlineService extends Service {
    public static final String ACTION_ONLINE = "tv.acfun.action.ONLINE";
    public static final String ACTION_OFFLINE = "tv.acfun.action.OFFLINE";
    private AlarmManager mAManager;
    private BroadcastReceiver mReceiver;
    public static String ARGS_REFERER_URL = "refer";
    
    public static void requestOnline(Context context, int avid){
        Intent service = new Intent(context, KeepOnlineService.class);
        if(avid > 0)
            service.putExtra(ARGS_REFERER_URL, "http://"+ArticleApi.getDomainRoot(context)+"/a/ac"+avid);
        context.startService(service);
    }
    
    public static void requestOffline(Context context){
        Intent intent = new Intent(ACTION_OFFLINE);
        context.sendBroadcast(intent);
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        mAManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        mReceiver = new OnlineReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_OFFLINE);
        filter.addAction(ACTION_ONLINE);
        registerReceiver(mReceiver, filter);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(AcApp.getUser() != null){
            String referer = intent == null ? null : intent.getStringExtra(ARGS_REFERER_URL);
            if(TextUtils.isEmpty(referer)){
                // v/list110/index.htm
                referer = "http://"+ArticleApi.getDomainRoot(getApplicationContext())+"/v/list110/index.htm";
            }
            Intent i = new Intent(ACTION_ONLINE);
            i.putExtra(ARGS_REFERER_URL, referer);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, i,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            long interval = AcApp.getOnlineInterval();
            long triggerAtTime = SystemClock.elapsedRealtime()+1000;
            // cancel last time intent
            mAManager.cancel(pendingIntent);
            mAManager.setRepeating(AlarmManager.ELAPSED_REALTIME, triggerAtTime, interval, pendingIntent);
        }else{
            stopSelf(); 
            return START_NOT_STICKY;
        }
        return super.onStartCommand(intent, flags, startId);
    }
    
    @Override
    public void onDestroy() {
        if (BuildConfig.DEBUG) Log.i("OnlineService","onDestroy service");
        if(mAManager != null){
            Intent i = new Intent(ACTION_ONLINE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, i,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            mAManager.cancel(pendingIntent);
            mAManager = null;
        }
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    class OnlineReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if(Connectivity.isWifiConnected(context) && ACTION_ONLINE.equals(intent.getAction())){
                    if (BuildConfig.DEBUG) Log.i("OnlineReceiver", "request online");
                    User user = AcApp.getUser();
                    Cookie[] cookies = JSON.parseObject(user.cookies, Cookie[].class);
                    OnlineRequest request = new OnlineRequest(context, user.id, intent.getStringExtra(ARGS_REFERER_URL), cookies, null, null);
                    AcApp.addRequest(request);
                }else if(ACTION_OFFLINE.equals(intent.getAction())){
                    if (BuildConfig.DEBUG) Log.i("OnlineReceiver", "request offline");
                    KeepOnlineService.this.stopSelf(); 
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
    }
    // GET http://www.acfun.tv/online.aspx?uid=458354
    static class OnlineRequest extends UsingCookiesRequest<JSONObject>{
        String mReferer;
        public OnlineRequest(Context context, int uid, String referer, Cookie[] cookies, Listener<JSONObject> listener,
                ErrorListener errorListner) {
            super(ArticleApi.getOnlineUrl(context, uid), cookies, null, listener, errorListner);
            mReferer = referer;
        }

        @Override
        protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
            String json = new String(response.data);
            JSONObject o = JSON.parseObject(json);
            if (BuildConfig.DEBUG) Log.v("OnlineRequest", "response:"+json);
            return Response.success(o, null);
        }
        
        @Override
        public Map<String, String> getHeaders() {
             Map<String, String> headers = super.getHeaders();
             headers.put("Referer", mReferer);
             headers.put("X-Requested-With", "XMLHttpRequest");
             if (BuildConfig.DEBUG) Log.v("OnlineRequest", "headers:"+headers.toString());
             return headers;
        }
    }
}
