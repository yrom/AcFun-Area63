package tv.acfun.a63.api;

import tv.acfun.a63.api.entity.Article;


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
        return getUrl(Constants.TYPE_HOT, channelId, 15, 1);
    }

    /**
     * 获得最新回复列表url
     */
    public static String getLatestRepliedUrl(int channelId) {
        return getUrl(Constants.TYPE_LATEST_REPLY, channelId, 15, 1);
    }

    public static String getContentUrl(int aid){
        if(aid <=0)
            return null;
        return String.format(Constants.URL_CONTENT, aid);
    }
    
    public static Article getArticle(int aid) throws Exception {
        Article article = new Article();
        //TODO : article parse
        String url = getContentUrl(aid);
        return article;
    }
}