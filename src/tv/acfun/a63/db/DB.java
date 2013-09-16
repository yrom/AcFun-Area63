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
package tv.acfun.a63.db;

import java.util.ArrayList;
import java.util.List;

import tv.acfun.a63.api.entity.Article;
import tv.acfun.a63.api.entity.Content;
import tv.acfun.a63.api.entity.User;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * @author Yrom
 *
 */
public final class DB {
    private DBOpenHelper helper;
    public DB(Context context){
        
        helper = new DBOpenHelper(context);
    }
    @Override
    protected void finalize() throws Throwable {
        helper.close();
        super.finalize();
        
    }
    public void saveUser(User user){
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("INSERT INTO "+DBOpenHelper.TABLE_USER + " (uid, name, avatar, signature, cookies) VALUES(?,?,?,?,?)",
                new Object[] { user.id, user.name, user.avatar,user.signature, user.cookies});
        db.close();
    }
    
    public User getUser(){
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor query = db.rawQuery("SELECT * FROM "+DBOpenHelper.TABLE_USER, null);
        User user = null;
        if (query.moveToFirst()) {
            user = new User();
            user.id = query.getInt(query.getColumnIndex("uid"));
            user.name = query.getString(query.getColumnIndex("name"));
            user.avatar = query.getString(query.getColumnIndex("avatar"));
            user.signature = query.getString(query.getColumnIndex("signature"));
            user.cookies = query.getString(query.getColumnIndex("cookies"));
        }
        query.close();
        db.close();
        return user;
    }
    public void logout() {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("DELETE FROM " + DBOpenHelper.TABLE_USER);
        db.close();
    }
    // TODO : fav
    public void addFav(Article article){
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("INSERT INTO "+DBOpenHelper.TABLE_FAV + " (aid, title, description,channelId,stows,time) VALUES(?,?,?,?,?,?)", new Object[]{article.id, article.title, article.description,article.channelId,article.stows,System.currentTimeMillis()});
        db.close();
    }
    
    public boolean deleteFav(int aid){
        SQLiteDatabase db = helper.getWritableDatabase();
        int delete = db.delete(DBOpenHelper.TABLE_FAV, "aid=?", new String[]{String.valueOf(aid)});
        db.close();
        return delete >0;
        
    }
    
    public boolean isFav(int aid){
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor query = db.rawQuery("SELECT time FROM "+DBOpenHelper.TABLE_FAV+" where aid=?", new String[]{String.valueOf(aid)});
        boolean isFav = query.getCount() >0;
        query.close();
        db.close();
        return isFav;
    }
    
    public List<Content> getFavList(){
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor query = db.rawQuery("SELECT * FROM "+ DBOpenHelper.TABLE_FAV,null);
        if(query.getCount() <=0){
            query.close();
            db.close();
            return null;
        }
        List<Content> cs = new ArrayList<Content>(query.getCount());
        while(query.moveToNext()){
            Content c = new Content();
            c.aid = query.getInt(query.getColumnIndex("aid"));
            c.title = query.getString(query.getColumnIndex("title"));
            c.description = query.getString(query.getColumnIndex("description"));
            c.channelId = query.getInt(query.getColumnIndex("channelId"));
            c.stows = query.getInt(query.getColumnIndex("stows"));
            c.releaseDate = query.getLong(query.getColumnIndex("time"));
            cs.add(c);
        }
        query.close();
        db.close();
        return cs;
    }
}
