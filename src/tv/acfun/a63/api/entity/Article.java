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

import org.json.JSONException;
import org.json.JSONObject;

public class Article {
    public int id;
    public String title;
    public long postTime;
    public int views;
    public int comments;
    public int stows;
    public ArrayList<String> imgUrls;
    public ArrayList<SubContent> contents;
    public User poster;
    public static class SubContent{
        public String subTitle;
        public String content;
    }
    
    
    
    public static Article newArticle(JSONObject articleJson){
        if(!articleJson.optBoolean("success")){
            return null;
        }
        Article article = null;
        try {
            article = new Article();
            JSONObject info = articleJson.getJSONObject("info");
            article.title = info.getString("title");
            article.postTime = info.getLong("posttime");
            article.id = info.getInt("id");
            article.poster = parseUser(info);
            //TODO : parse contents
            
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
        
        
        
        return article;
    }



    private static User parseUser(JSONObject info)
            throws JSONException {
        JSONObject postuser = info.getJSONObject("postuser");
        User poster = new User();
        poster.name = postuser.getString("name");
        poster.id = postuser.getInt("uid");
        poster.signature = postuser.optString("signature");
        poster.avatar = postuser.optString("avastar");
        return poster;
    }
    
}
