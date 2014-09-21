
package tv.acfun.a63.api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

import org.json.JSONObject;

import tv.acfun.a63.AcApp;
import tv.acfun.a63.BuildConfig;
import tv.acfun.a63.api.entity.Content;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.alibaba.fastjson.JSON;
import com.umeng.analytics.MobclickAgent;
import com.umeng.analytics.onlineconfig.UmengOnlineConfigureListener;

public final class ArticleApi {
    private static final String DOMAIN_ROOT = "domain_root";
    private static final String DOMAIN_API = "domain_api";
    
    public static String HOME = Constants.HOME;
    public static String API_HOME = Constants.API_HOME;
    
    public static void updateConfig(Context context){
        HOME = MobclickAgent.getConfigParams(context, DOMAIN_ROOT);
        API_HOME = MobclickAgent.getConfigParams(context, DOMAIN_API);
        MobclickAgent.updateOnlineConfig(context);
        MobclickAgent.setOnlineConfigureListener(new UmengOnlineConfigureListener() {
            @Override
            public void onDataReceived(JSONObject json) {
                if(BuildConfig.DEBUG){
                    Log.v("api", "received online config:"+json);
                }
                if(json == null) return;
                String root = json.optString(DOMAIN_ROOT, Constants.HOME);
                String api = json.optString(DOMAIN_API, Constants.API_HOME);
                HOME = root;
                API_HOME = api;
            }
        });
    }
    public static String getDomainRoot(Context context) {
        if(context == null) return HOME;
        String params = MobclickAgent.getConfigParams(context, DOMAIN_ROOT);
        return TextUtils.isEmpty(params) ? HOME : params;
    }
    
    public static String getDomainApi(Context context) {
        if(context == null) return API_HOME;
        String params = MobclickAgent.getConfigParams(context, DOMAIN_API);
        return TextUtils.isEmpty(params) ? API_HOME : params;
    }
    
    /**
     * @param type
     *            {@code TYPE_*}
     * @param catId
     *            {@code CAT_*}
     * @param count
     *            default 20
     * @param page
     *            default 1
     * @param context 
     * @return
     */
    public static String getUrl(Context context, int type, int catId, int count, int page) {
        if (count <= 0)
            count = 20;
        if (page < 1)
            page = 1;
        return getBaseUrl(context, type, catId, count, page);
    }
    
    public static String getBaseUrl(Context context, int order, int channelId, int count, int page){
        String root = getDomainApi(context);
        return String.format(Locale.US, "http://%s/apiserver/content/channel?orderBy=%d&channelId=%d&pageSize=%d&pageNo=%d", root, order, channelId, count, page);
    }
    
    /**
     * 获得默认形式(最新发布)列表的url
     */
    public static String getDefaultUrl(Context context, int channelId, int count, int page) {
        return getUrl(context, Constants.TYPE_DEFAULT, channelId, count, page);
    }

    /**
     * 获得周热门列表url
     */
    public static String getHotListUrl(Context context, int channelId,int page) {
        return getUrl(context, Constants.TYPE_HOT, channelId, Constants.COUNT_HOT, page);
    }

    /**
     * 获得最新回复列表url
     */
    public static String getLatestRepliedUrl(Context context, int channelId,int page) {
        return getUrl(context, Constants.TYPE_LATEST_REPLY, channelId, Constants.COUNT_LAST_REPLY, page);
    }
    
    public static String getContentUrl(Context context, int aid){
        if (aid <= 0)
            return null;
        String root = getDomainApi(context);
        return String.format(Locale.US, "http://%s/apiserver/content/article?contentId=%d", root, aid);
    }
    
    public static String getCommentUrl(Context context, int id, int page){
        String root = getDomainRoot(context);
        return String.format(Locale.US, "http://%s/comment_list_json.aspx?contentId=%d&currentPage=%d", root, id, page);
    }
    
    public static String getProfileUrl(Context context){
        String root = getDomainRoot(context);
        return "http://"+root+"/api/member.aspx?name=profile";
    }
    
    public static String getSplashUrl(Context context){
        String root = getDomainRoot(context);
        return "http://"+root+"/member/splash.aspx";
    }
    
    public static String getRankListUrl(Context context){
        String api = getDomainApi(context);
        return "http://"+api+"/apiserver/content/rank?channelIds=110,73,74,75&pageSize=20";
    }

    public static List<Content> getChannelContents(String json) {
        return JSON.parseArray(json, Content.class);
    }

    public static boolean isHotArticle(Content art) {
        boolean isHot;
        long e = System.currentTimeMillis() - art.releaseDate;
        if (art.channelId == Constants.CAT_COMIC_LIGHT_NOVEL) {
            isHot = art.comments >= 15 || art.views >= 1200;
        } else if (art.channelId == Constants.CAT_WORK_EMOTION) {
            isHot = art.comments >= 70 || art.views >= 5500;
        } else if (art.channelId == Constants.CAT_AN_CULTURE) {
            isHot = art.comments >= 35 || art.views >= 3500;
        } else if (e <= AcApp._1_hour * 3) {
            isHot = art.comments >= 50 || art.views >= 4500;
        } else if (e <= AcApp._1_hour * 5) {
            isHot = art.comments >= 65 || art.views >= 6000;
        } else if (e <= AcApp._1_hour * 8) {
            isHot = art.comments >= 80 || art.views >= 10000;
        } else if (e <= AcApp._1_hour * 12) {
            isHot = art.comments >= 95 || art.views >= 12000;
        } else if (e <= AcApp._1_hour * 18) {
            isHot = art.comments >= 110 || art.views >= 14500;
        } else {
            isHot = art.comments >= 120 || art.views >= 15000;
        }

        return isHot;
    }

    public static boolean isRecommendedArticle(Content art) {
        boolean isRecommended;
        long e = System.currentTimeMillis() - art.releaseDate;
        if (e <= AcApp._1_hour) {
            isRecommended = art.views >= 800 && art.comments >= 5 && art.stows >= 2;
        } else if (e <= AcApp._1_hour * 4) {
            isRecommended = art.views >= 2000 && art.comments >= 15 && art.stows >= 6;
        } else if (e <= AcApp._1_hour * 8) {
            isRecommended = art.views >= 4000 && art.comments >= 25 && art.stows >= 10;
        } else if (e <= AcApp._1_hour * 14) {
            isRecommended = art.views >= 7500 && art.comments >= 45 && art.stows >= 16;
        } else if (e <= AcApp._1_hour * 22) {
            isRecommended = art.views >= 9000 && art.comments >= 55 && art.stows >= 24;
        } else
            isRecommended = art.views >= 11000 && art.comments >= 65 && art.stows >= 40;

        return isRecommended;
    }
    
    static SparseArray<String> channels = new SparseArray<String>();
    
    static{
        channels.put(Constants.id.ANIMATION, "动画");
        channels.put(Constants.id.AN_LITE, "动画短片");
        channels.put(Constants.id.MAD_AMV, "MAD·AMV");
        channels.put(Constants.id.MMD_3D, "MMD·3D");
        channels.put(Constants.id.AN_COMP, "动画合集");
        
        channels.put(Constants.id.MUSIC, "音乐");
        channels.put(Constants.id.SING, "演唱");
        channels.put(Constants.id.DANCE, "宅舞");
        channels.put(Constants.id.VOCALOID, "Vocaloid");
        channels.put(Constants.id.ACG, "ACG音乐");
        channels.put(Constants.id.POP, "流行音乐");
        
        channels.put(Constants.id.FUN, "娱乐");
        channels.put(Constants.id.FUNY, "生活娱乐");
        channels.put(Constants.id.KICHIKU, "鬼畜调教");
        channels.put(Constants.id.PET, "萌宠");
        channels.put(Constants.id.EAT, "美食");
        channels.put(Constants.id.SCIENCE, "科技");
        channels.put(Constants.id.SPORT, "体育");
        
        channels.put(Constants.id.VIDEO, "影视");
        channels.put(Constants.id.MOVIE, "电影");
        channels.put(Constants.id.TV, "剧集");
        channels.put(Constants.id.VARIETY, "综艺");
        channels.put(Constants.id.DOCUMENTARY, "纪录片");
        channels.put(Constants.id.PILI, "特摄·霹雳");
        
        channels.put(Constants.id.GAME, "游戏");
        channels.put(Constants.id.BEST_GAME, "游戏集锦");
        channels.put(Constants.id.LIVE_OB, "实况解说");
        channels.put(Constants.id.FLASH, "FLASH");
        channels.put(Constants.id.MUGEN, "MUGEN");
        channels.put(Constants.id.LOL, "撸啊撸");
        
        
        channels.put(Constants.id.BANGUMI, "新番连载");
        
        channels.put(Constants.id.ARTICLE.ARTICLE, "文章");
        channels.put(Constants.id.ARTICLE.COLLECTION, "综合");
        channels.put(Constants.id.ARTICLE.WORK_EMOTION, "工作·情感");
        channels.put(Constants.id.ARTICLE.AN_CULTURE, "动漫文化");
        channels.put(Constants.id.ARTICLE.COMIC_LIGHT_NOVEL, "漫画·小说");
    }
    public static String getChannelName(int channelId){
        return channels.get(channelId);
    }
    
    /**
     * http://www.acfun.tv/api/member.aspx?name=mentions&pageNo=1&pageSize=10
     */
    public static String getMentionsUrl(Context context, int size, int page) {
        String root = getDomainRoot(context);
        return String.format(Locale.US, "http://%s/api/member.aspx?name=mentions&pageNo=%d&pageSize=%d", root, page, size);
    }
    
    /**
     * @param query key word
     * @param orderId 相关、日期、点击、评论、收藏，0~4
     * @param orderBy 按标题标签、用户、内容简介查找，1~3
     * @param pageNo 
     * @param pageSize http://www.acfun.tv/api/search.aspx?query={query}&exact=1&channelIds=63&orderId=2&orderBy=1&pageNo=1&pageSize=10&_=1387786184949
     * @return
     */
    public static String getSearchUrl(Context context, String query, int orderId, int orderBy, int pageNo, int pageSize){
        String url = null;
        try {
            String key = URLEncoder.encode(query, "UTF-8");
            String format = "http://%s/api/search.aspx?query=%s&exact=1&channelIds=63&orderId=%d&orderBy=%d&pageNo=%d&pageSize=%d";
            url = String.format(Locale.US, format, getDomainRoot(context), key, orderId, orderBy, pageNo, pageSize);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return url;
    }
    
    public static String getOnlineUrl(Context context, long uid) {
        return "http://"+getDomainRoot(context)+"/online.aspx?uid="+uid;
    }
    
}
