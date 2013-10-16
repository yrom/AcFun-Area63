
package tv.acfun.a63.api;

import java.util.List;

import tv.acfun.a63.AcApp;
import tv.acfun.a63.api.entity.Content;
import android.util.SparseArray;

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
        if (page < 1)
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
    public static String getHotListUrl(int channelId,int page) {
        return getUrl(Constants.TYPE_HOT, channelId, Constants.COUNT_HOT, page);
    }

    /**
     * 获得最新回复列表url
     */
    public static String getLatestRepliedUrl(int channelId,int page) {
        return getUrl(Constants.TYPE_LATEST_REPLY, channelId, Constants.COUNT_LAST_REPLY, page);
    }

    public static String getContentUrl(int aid) {
        if (aid <= 0)
            return null;
        return String.format(Constants.URL_CONTENT, aid);
    }

    public static List<Content> getChannelContents(String json) {
        return JSON.parseArray(json, Content.class);
    }

    public static String getCommentUrl(int aid, int page) {
        return String.format(Constants.URL_COMMENT, aid, page);
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
            isHot = art.comments >= 50 || art.views >= 3500;
        } else if (e <= AcApp._1_hour * 5) {
            isHot = art.comments >= 65 || art.views >= 5000;
        } else if (e <= AcApp._1_hour * 8) {
            isHot = art.comments >= 80 || art.views >= 7000;
        } else if (e <= AcApp._1_hour * 12) {
            isHot = art.comments >= 95 || art.views >= 9000;
        } else if (e <= AcApp._1_hour * 18) {
            isHot = art.comments >= 110 || art.views >= 12500;
        } else {
            isHot = art.comments >= 120 || art.views >= 14000;
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

    public static String getRankListUrl(int page) {
        return String.format(Constants.URL_RANK, (page-1)*20+1,page*20,System.currentTimeMillis()/300000*300000);
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
        channels.put(Constants.id.ARTICLE.COMIC_LIGHT_NOVEL, "漫画·轻小说");
    }
    public static String getChannelName(int channelId){
        return channels.get(channelId);
    }
    /**
     * http://www.acfun.tv/api/member.aspx?name=mentions&pageNo=1&pageSize=10
     */
    public static String getMentionsUrl(int size, int page) {
        return String.format(Constants.URL_MENTIONS, page, size);
    }
}
