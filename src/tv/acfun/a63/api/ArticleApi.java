package tv.acfun.a63.api;

import java.util.List;

import tv.acfun.a63.AcApp;
import tv.acfun.a63.api.entity.Content;

import com.alibaba.fastjson.JSON;

public final class ArticleApi {

    /**
     * @param type
     *            {@code TYPE_*}
     * @param catId
     *            {@code CAT_*}
     * @param count
     *            default 20
     * @param page
     *            default 1
     * @return
     */
    public static String getUrl(int type, int catId, int count, int page) {
        if (count <= 0)
            count = 20;
        if(page <1)
            page = 1;
        return String.format(Constants.URL_BASE, type, catId, count, 20 * (page - 1));
    }

    /**
     * 获得默认形式(最新发布)列表的url
     */
    public static String getDefaultUrl(int channelId, int count, int page) {
        return getUrl(Constants.TYPE_DEFAULT, channelId, count, page);
    }

    /**
     * 获得周热门列表url
     */
    public static String getHotListUrl(int channelId) {
        return getUrl(Constants.TYPE_HOT, channelId, Constants.COUNT_HOT, 1);
    }

    /**
     * 获得最新回复列表url
     */
    public static String getLatestRepliedUrl(int channelId) {
        return getUrl(Constants.TYPE_LATEST_REPLY, channelId, Constants.COUNT_LAST_REPLY, 1);
    }

    public static String getContentUrl(int aid){
        if(aid <=0)
            return null;
        return String.format(Constants.URL_CONTENT, aid);
    }
    
    
    public static List<Content> getChannelContents(String json){
        return JSON.parseArray(json, Content.class);
    }
    
    public static String getCommentUrl(int aid, int page){
        return String.format(Constants.URL_COMMENT, aid,page);
    }
    
    public static boolean isHotArticle(Content art) {
        boolean isHot;
        long e = System.currentTimeMillis() - art.releaseDate;
        if (e <= AcApp._1_hour * 3) {
            isHot = art.comments >= 35 || art.views >= 2500;
        } else if (e <= AcApp._1_hour * 6) {
            isHot = art.comments >= 90 || art.views >= 5000;
        } else if (art.channelId == Constants.CAT_COMIC_LIGHT_NOVEL) {
            isHot = art.comments >= 25 || art.views >= 1200;
        } else if (art.channelId == Constants.CAT_WORK_EMOTION) {
            isHot = art.comments >= 70 || art.views >= 3000;
        } else if (art.channelId == Constants.CAT_AN_CULTURE) {
            isHot = art.comments >= 40 || art.views >= 1400;
        } else {
            isHot = art.comments >= 100 || art.views >= 10000;
        }
        return isHot;
    }
    public static boolean isRecommendedArticle(Content art) {
        boolean isRecommended;
        long e = System.currentTimeMillis() - art.releaseDate;
        if(e<=AcApp._1_hour){
            isRecommended = art.comments >= 10 && art.views >= 300 && art.stows>=1;
        }else if(e<=AcApp._1_hour*3){
            isRecommended = art.comments >= 25 && art.views >= 700 && art.stows >=5;
        }else if(e<=AcApp._1_hour*5){
            isRecommended = art.comments >= 30 && art.views >= 1500 && art.stows>=10;
        }else if(e<=AcApp._1_hour*12){
            isRecommended = art.comments >= 35 && art.views >= 3000 && art.stows>=25;
        }else
            isRecommended = art.comments >= 40 && art.views >= 6000 && art.stows>=50;
            
            return isRecommended;
    }
}
