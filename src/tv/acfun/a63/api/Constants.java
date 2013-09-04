package tv.acfun.a63.api;

public interface Constants {

    String URL_HOME = "http://www.acfun.tv/";
    String URL_BASE = URL_HOME + "api/getlistbyorder.aspx?orderby=%d&channelIds=%d&count=%d&first=%d";
    String URL_CONTENT = URL_HOME + "api/content.aspx?query=%d";
    String URL_COMMENT = URL_HOME + "comment_list_json.aspx?contentId=%d&currentPage=%d";
    int CAT_ARTICLE = 63;
    int CAT_COLLECTION = 110;
    int CAT_WORK_EMOTION = 73;
    int CAT_AN_CULTURE = 74;
    int CAT_COMIC_LIGHT_NOVEL = 75;
    int[] CAT_IDS = {CAT_COLLECTION,CAT_WORK_EMOTION,CAT_AN_CULTURE,CAT_COMIC_LIGHT_NOVEL};
    int TYPE_DEFAULT = 0;
    int TYPE_HOT = 6;
    int TYPE_LATEST_REPLY = 22;
}
