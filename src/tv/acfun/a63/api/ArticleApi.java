
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
    public static String getHotListUrl(int channelId) {
        return getUrl(Constants.TYPE_HOT, channelId, Constants.COUNT_HOT, 1);
    }

    /**
     * 获得最新回复列表url
     */
    public static String getLatestRepliedUrl(int channelId) {
        return getUrl(Constants.TYPE_LATEST_REPLY, channelId, Constants.COUNT_LAST_REPLY, 1);
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
            isHot = art.comments >= 15 || art.views >= 2000;
        } else if (e <= AcApp._1_hour * 5) {
            isHot = art.comments >= 35 || art.views >= 3500;
        } else if (e <= AcApp._1_hour * 8) {
            isHot = art.comments >= 60 || art.views >= 7500;
        } else if (e <= AcApp._1_hour * 12) {
            isHot = art.comments >= 85 || art.views >= 9000;
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
}
