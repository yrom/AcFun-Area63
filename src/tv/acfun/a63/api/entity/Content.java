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

package tv.acfun.a63.api.entity;


/**
 * {
      "username": "biubiubiu默",
      "userId": 414920,
      "userImg": "http://w5cdn.ranktv.cn/dotnet/artemis/u/cms/www/201306/190909487vus.jpg",
      "avatar": "http://w5cdn.ranktv.cn/dotnet/artemis/u/cms/www/201306/190909487vus.jpg",
      "sign": "我一直认为自己是个心里长着JJ的汉子。",
      "userClass": "",
      "aid": 798950,
      "cid": 798950,
      "title": "欠两千块房费的女文青跑了，至于吗？",
      "titleImg": "http://w5cdn.ranktv.cn/dotnet/20120923/style/image/cover.png",
      "url": "/a/ac798950",
      "releaseDate": 1377587806682,
      "description": "随处逛逛的时候看到的很有意思的帖子",
      "channelId": 110,
      "tags": "文艺女青年",
      "contentClass": "",
      "allowDanmaku": 0,
      "views": 51146,
      "stows": 88,
      "comments": 662,
      "score": 0,
      "success": true,
      "errorlog": ""
    }
 * @author Yrom
 * 
 */
public class Content {

    public String title;
    public String titleImg;
    public String description;
    public String username;
    public long userId;
    public long views;
    public int aid;
    public int channelId;
    public int comments;
    public long releaseDate;
    
    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public String getTitleImg() {
        return titleImg;
    }


    public void setTitleImg(String titleImg) {
        this.titleImg = titleImg;
    }


    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
    }


    public String getUsername() {
        return username;
    }


    public void setUsername(String username) {
        this.username = username;
    }


    public long getUserId() {
        return userId;
    }


    public void setUserId(long userId) {
        this.userId = userId;
    }


    public long getViews() {
        return views;
    }


    public void setViews(long views) {
        this.views = views;
    }


    public int getAid() {
        return aid;
    }


    public void setAid(int aid) {
        this.aid = aid;
    }


    public int getChannelId() {
        return channelId;
    }


    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }


    public int getComments() {
        return comments;
    }


    public void setComments(int comments) {
        this.comments = comments;
    }


    public long getReleaseDate() {
        return releaseDate;
    }


    public void setReleaseDate(long releaseDate) {
        this.releaseDate = releaseDate;
    }


//    public static Content newContent(JSONObject contentJson) throws JSONException{
//        Content c = new Content();
//        c.title = contentJson.getString("title");
//        c.description = contentJson.getString("description");
//        c.views = contentJson.getLong("views");
//        c.titleImage = contentJson.getString("titleImg");
//        c.aid = contentJson.getInt("aid");
//        c.channelId = contentJson.getInt("channelId");
//        c.comments = contentJson.getInt("comments");
//        c.releaseDate = contentJson.getLong("releaseDate");
//        
//        return c;
//    }

}
