package tv.acfun.a63.api;

public interface Constants {

    String URL_HOME = "http://www.acfun.tv/";
    String URL_BASE = URL_HOME + "api/getlistbyorder.aspx?orderby=%d&channelIds=%d&count=%d&first=%d";
    String URL_CONTENT = URL_HOME + "api/content.aspx?query=%d";
    String URL_COMMENT = URL_HOME + "comment_list_json.aspx?contentId=%d&currentPage=%d";
    String URL_PROFILE = URL_HOME + "api/member.aspx?name=profile";
    String URL_SPLAH = URL_HOME + "api/member.aspx?name=splash";
    /**
     * 我关注的
     */
    String URL_FOLLOWING = URL_HOME + "api/friend.aspx?name=getFollowingList&isGroup=0&groupId=-1&pageNo=%d&pageSize=20";
    /**
     * 关注我的
     */
    String URL_FOLLOWER = URL_HOME + "api/friend.aspx?name=getFollowedList&pageNo=%d&pageSize=20";
    int CAT_ARTICLE = 63;
    int CAT_COLLECTION = 110;
    int CAT_WORK_EMOTION = 73;
    int CAT_AN_CULTURE = 74;
    int CAT_COMIC_LIGHT_NOVEL = 75;
    int[] CAT_IDS = {CAT_COLLECTION,CAT_WORK_EMOTION,CAT_AN_CULTURE,CAT_COMIC_LIGHT_NOVEL};
    int TYPE_DEFAULT = 0;
    int TYPE_HOT = 6;
    int TYPE_LATEST_REPLY = 22;
    
    int COUNT_HOT = 15;
    int COUNT_LAST_REPLY = 15;
    
    int MODE_MIX = 0;
    int MODE_NO_PIC = 1;
    int MODE_COMMIC = 2;
}
