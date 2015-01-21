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

import tv.acfun.a63.AcApp;
import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable{
    public int id;
    public String name;
    public String avatar;
    public String signature;
    public long savedTime;
    public boolean isExpired(){
        return System.currentTimeMillis() - savedTime >= 14 * AcApp._24_hour;
    }
    public User(){}
    public User(int id, String name, String avatar, String signature) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.signature = signature;
    }
    public User(Parcel in) {
        id = in.readInt();
        name = in.readString();
        avatar = in.readString();
        signature = in.readString();
        cookies = in.readString();
        savedTime = in.readLong();
    }
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(avatar);
        dest.writeString(signature);
        dest.writeString(cookies);
        dest.writeLong(savedTime);
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };
    
    public String cookies;
    
}
