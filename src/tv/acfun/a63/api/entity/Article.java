/*
 * Copyright (C) 2013 Yrom Wang
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

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

/***
{
  "content": [
    {
      "content": "<span style=\"font-size:16px;font-family:微软雅黑, sans-serif;\"> 作者：兄贵解说老王&larr;他是A站UP：今天我路过<\/span>",
      "subtitle": "【DOTA】随着中国战队的落败，一股风暴加地震正在袭来"
    }
  ],
  "tags": [
    {
      "id": 1503,
      "name": "DOTA"
    },
    {
      "id": 228485,
      "name": "兄贵解说"
    },
    {
      "id": 533,
      "name": "原创"
    }
  ],
  "success": true,
  "info": {
    "id": 797651,
    "title": "【DOTA】随着中国战队的落败，一股风暴加地震正在袭来",
    "posttime": 1377511408000,
    "description": "折戟了，而随着中国战队的落败，一股风暴加地震正在袭来。一个普通看客眼中的发展史",
    "postuser": {
      "uid": 319714,
      "name": "续-R",
      "avastar": "http://w5cdn.ranktv.cn/dotnet/artemis/u/cms/www/201308/29154301mql6.jpg",
      "signature": "要开学了混蛋！\n衝動を解き放て！\n駆け巡り積もる意志 \nその眼を忘れはしない"
    },
    "titleimage": "http://w5cdn.ranktv.cn/dotnet/20120923/style/image/cover.png",
    "channel": {
      "channelName": "综合",
      "channelID": 110,
      "channelURL": "/a/list110/index.htm"
    },
    "statistics": [
      29260,
      451,
      0,
      0,
      0,
      11
    ]
  }
}
 * @author Yrom
 * 
 */
public class Article {
    public int id;
    public String title;
    public long postTime;
    public int views;
    public int comments;
    public int stows;
    public int channelId;
    public String channelName;
    public ArrayList<String> imgUrls;
    public ArrayList<SubContent> contents;
    public User poster;
    public String description;

    public static class SubContent {
        public String subTitle;
        public String content;
    }

    private static String TAG = "Article";
    private static Pattern imageReg = Pattern.compile("<img.+?src=[\"|'](.+?)[\"|']");

    public static Article newArticle(JSONObject articleJson) {
        if (Boolean.TRUE != articleJson.getBoolean("success")) {
            return null;
        }
        Article article = new Article();
        article.imgUrls = new ArrayList<String>();
        // parse info
        JSONObject info = articleJson.getJSONObject("info");
        article.title = info.getString("title");
        article.postTime = info.getLong("posttime");
        article.id = info.getIntValue("id");
        article.description = info.getString("description");
        article.poster = parseUser(info);
        // statistics
        JSONArray statistics = info.getJSONArray("statistics");
        article.views = statistics.getIntValue(0);
        article.comments = statistics.getIntValue(1);
        article.stows = statistics.getIntValue(5);
        // sub contents and images
        JSONArray contentArray = articleJson.getJSONArray("content");
        article.contents = new ArrayList<Article.SubContent>(contentArray.size());

        for (int i = 0; i < contentArray.size(); i++) {
            SubContent content = new SubContent();
            JSONObject sub = contentArray.getJSONObject(i);
            content.content = sub.getString("content");
            content.subTitle = sub.getString("subtitle");
            Matcher matcher = imageReg.matcher(content.content);
            while (matcher.find()) {
                article.imgUrls.add(matcher.group(1));
            }
            article.contents.add(content);
        }
        // channel
        JSONObject channel = info.getJSONObject("channel");
        article.channelId = channel.getIntValue("channelID");
        article.channelName = channel.getString("channelName");

        return article;
    }

    private static User parseUser(JSONObject info) throws JSONException {
        JSONObject postuser = info.getJSONObject("postuser");
        User poster = new User();
        poster.name = postuser.getString("name");
        poster.id = postuser.getIntValue("uid");
        poster.signature = postuser.getString("signature");
        poster.avatar = postuser.getString("avastar");
        return poster;
    }
    
}
