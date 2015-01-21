package tv.acfun.a63.api;

public interface Constants {
    @Deprecated
    String HOME = "www.acfun.tv";
    @Deprecated
    String API_HOME = "api.acfun.tv";
    @Deprecated
    String URL_HOME = "http://www.acfun.tv/";
    @Deprecated
    String URL_BASE = URL_HOME + "api/getlistbyorder.aspx?orderby=%d&channelIds=%d&count=%d&first=%d";
    @Deprecated
    String URL_CONTENT = URL_HOME + "api/content.aspx?query=%d";
    @Deprecated
    String URL_COMMENT = URL_HOME + "comment_list_json.aspx?contentId=%d&currentPage=%d";
    @Deprecated
    String URL_PROFILE = URL_HOME + "api/member.aspx?name=profile";
    @Deprecated
    String URL_SPLAH = URL_HOME + "api/member.aspx?name=splash";
//    String URL_RANK = "http://trend.acfun.tv/api.ntr?page=bd_article&mode=rank&cid=63&ttype=1&stype=1&tdetail=2&start=%d&end=%d&timestamp=%d";
    @Deprecated
    String URL_RANK = "http://api.acfun.tv/apiserver/content/rank?channelIds=110,73,74,75&pageSize=20";
    /**
     * 我关注的
     */
    @Deprecated
    String URL_FOLLOWING = URL_HOME + "api/friend.aspx?name=getFollowingList&isGroup=0&groupId=-1&pageNo=%d&pageSize=20";
    /**
     * 关注我的
     */
    @Deprecated
    String URL_FOLLOWER = URL_HOME + "api/friend.aspx?name=getFollowedList&pageNo=%d&pageSize=20";
    @Deprecated
    String URL_MENTIONS = URL_HOME +"api/member.aspx?name=mentions&pageNo=%d&pageSize=%d";
    @Deprecated
    String URL_SEARCH = URL_HOME + "api/search.aspx?query=%s&exact=1&channelIds=63&orderId=%d&orderBy=%d&pageNo=%d&pageSize=%d";
    int CAT_ARTICLE = 63;
    int CAT_COLLECTION = 110;
    int CAT_WORK_EMOTION = 73;
    int CAT_AN_CULTURE = 74;
    int CAT_COMIC_LIGHT_NOVEL = 75;
    int[] CAT_IDS = {CAT_COLLECTION,CAT_WORK_EMOTION,CAT_AN_CULTURE,CAT_COMIC_LIGHT_NOVEL,CAT_ARTICLE};
    int TYPE_DEFAULT = 0;
    int TYPE_HOT = 1;
    int TYPE_LATEST_REPLY = 3;
    
    int COUNT_HOT = 20;
    int COUNT_LAST_REPLY = 20;
    
    int MODE_MIX = 0;
    int MODE_NO_PIC = 1;
    int MODE_COMMIC = 2;
    
    public static final class id {

        public static final int ANIMATION = 1;
        public static final int MUSIC     = 58;
        public static final int GAME      = 59;
        public static final int FUN       = 60;
        public static final int BANGUMI   = 67;
        public static final int VIDEO     = 68;
        public static final int SPORT     = 69;
        public static final int SCIENCE   = 70;
        public static final int FLASH     = 71;
        public static final int MUGEN     = 72;

        public static final class ARTICLE {
            
            public static final int ARTICLE           = 63;
            public static final int COLLECTION        = 110;
            public static final int WORK_EMOTION      = 73;
            public static final int AN_CULTURE        = 74;
            public static final int COMIC_LIGHT_NOVEL = 75;
        }
        public static final int BEST_GAME   = 83;
        public static final int LIVE_OB     = 84;
        public static final int LOL         = 85;
        public static final int FUNY        = 86;
        public static final int KICHIKU     = 87;
        public static final int PET         = 88;
        public static final int EAT         = 89;
        public static final int MOVIE       = 96;
        public static final int TV          = 97;
        public static final int VARIETY     = 98;
        
        public static final int PILI        = 99;
        public static final int DOCUMENTARY = 100;
        public static final int SING        = 101;
        public static final int DANCE       = 102;
        public static final int VOCALOID    = 103;
        public static final int ACG         = 104;
        public static final int POP         = 105;
        public static final int AN_LITE     = 106;
        public static final int MAD_AMV     = 107;
        public static final int MMD_3D      = 108;
        public static final int AN_COMP     = 109;
    }
}
