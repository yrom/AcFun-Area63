/*
 * Copyright (C) 2013 YROM.NET
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tv.acfun.a63.service;

import java.nio.charset.Charset;

import org.apache.commons.httpclient.Cookie;

import tv.acfun.a63.AcApp;
import tv.acfun.a63.BuildConfig;
import tv.acfun.a63.MentionActivity;
import tv.acfun.a63.MentionActivity.MentionsRequest;
import tv.acfun.a63.R;
import tv.acfun.a63.api.entity.Comment;
import tv.acfun.a63.api.entity.Mentions;
import tv.acfun.a63.util.Connectivity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

/**
 * @author Yrom
 * 
 */
public class PushService extends Service {
    public static final String ACTION_REFRESH = "tv.acfun.action.REFRESH";
    private NotificationManager mManager;
    private int lastTotalCount;
    private BroadcastReceiver receiver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public static void start(Context context){
        Intent service = new Intent(context, PushService.class);
        context.startService(service);
    }
    @Override
    public void onCreate() {
        mManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        receiver = new RefreshReceiver();
        IntentFilter filter = new IntentFilter(ACTION_REFRESH);
        registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy() {
        if(mAManager != null){
            Intent i = new Intent(ACTION_REFRESH);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, i,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            mAManager.cancel(pendingIntent);
            mAManager = null;
        }
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(AcApp.isMentionEnabled() && AcApp.getUser() != null){
            mAManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            Intent i = new Intent(ACTION_REFRESH);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, i,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            long triggerAtTime = SystemClock.elapsedRealtime();
            long interval = AcApp.getPreferenceRefreshingInterval();
            mAManager.setRepeating(AlarmManager.ELAPSED_REALTIME, triggerAtTime+AcApp._1_min, interval, pendingIntent);
            if(BuildConfig.DEBUG)
                Log.i("PUSH", "start : id="+startId+", interval="+interval);
        }else{
            // 注销后，自我销毁
            stopSelf(); 
        }
        return super.onStartCommand(intent, flags, startId);
    }

    class RefreshReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean canRefresh;
            if(AcApp.isMentionWifiOnly())
                canRefresh = Connectivity.isWifiConnected(context);
            else
                canRefresh = Connectivity.isNetworkAvailable(context);
            if(canRefresh)
                try {
                    if (ACTION_REFRESH.equals(intent.getAction())) {
                        if(BuildConfig.DEBUG)
                            Log.i("RefreshReceiver", "request refresh");
                        Cookie[] cookies = JSON.parseObject(AcApp.getUser().cookies, Cookie[].class);
                        MentionsRequest mentionsRequest = new MentionActivity.MentionsRequest(1,
                                cookies, mListener, mError);
                        // 缓存，使从消息栏打开 Activity 后可以直接读取
                        mentionsRequest.setShouldCache(true);
                        byte[] data = AcApp.getDataInDiskCache(mentionsRequest.getCacheKey());
                        if (data != null) {
                            String json = new String(data, Charset.defaultCharset());
                            JSONObject parseObject = JSON.parseObject(json);
                            Mentions comments = JSON.toJavaObject(parseObject, Mentions.class);
                            lastTotalCount = comments.totalCount;
                        }
                        AcApp.addRequest(mentionsRequest);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            else if(BuildConfig.DEBUG){
                Log.d("PUSH", "no net work conn");
            }
        }

    }

    Listener<Mentions> mListener = new Listener<Mentions>() {

        @Override
        public void onResponse(Mentions response) {
            boolean needNotify = response.totalCount >0 && lastTotalCount < response.totalCount ;
            if(BuildConfig.DEBUG)
                needNotify = true;
            if (needNotify) {
                Comment c = response.commentArr.get(response.commentList[0]);
                if(c != null)
                    showNotification(c);
            }
        }
    };
    ErrorListener mError = new ErrorListener() {

        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e("PUSH", "something error", error);
        }
    };
    private AlarmManager mAManager;

    private void showNotification(Comment c) {

        Intent i = new Intent(this, MentionActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i,
                Intent.FLAG_ACTIVITY_NEW_TASK);
        Notification noti = new NotificationCompat.Builder(this)
                .setContentTitle(c.userName + "提到了你")
                .setContentText(c.content)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.notify_chat).build();
        mManager.notify(0, noti);
    }
}
