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

import tv.acfun.a63.api.entity.Article;
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
//        db.execSQL("INSERT INTO "+DBOpenHelper.TABLE_FAV + "aid, title, article_json,", new Object[]{article.id, article.title, article.});
        
        db.close();
    }
}
