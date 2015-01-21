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

import java.io.Serializable;

/**
 * {
      "cid": 16249066,
      "content": "[emot\u003dac,17/]我们是三个人",
      "userName": "推倒幼女同好會",
      "userID": 483895,
      "postDate": "2013-09-04 09:50:56",
      "userImg": "http://w5cdn.ranktv.cn/dotnet/artemis/u/cms/www/201306/28011055zi6g.jpg",
      "userClass": "",
      "quoteId": 16249021,
      "count": 8,
      "ups": 0,
      "downs": 0
    }
 * @author Yrom
 *
 */
public class Comment implements Serializable {
    private static final long serialVersionUID = 1L;
    public String content;
    public String userName;
    public String postDate;
    public String userImg;
    /** 目前来看，应该不会越界...(TODO: long)*/
    public int cid;
    public int quoteId;
    public int count;
    public int ups;
    public int downs;
    public long userID;
    public void setContent(String content) {
        this.content = content;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public void setPostDate(String postDate) {
        this.postDate = postDate;
    }
    public void setUserImg(String userImg) {
        this.userImg = userImg;
    }
    public void setCid(int cid) {
        this.cid = cid;
    }
    public void setQuoteId(int quoteId) {
        this.quoteId = quoteId;
    }
    public void setCount(int count) {
        this.count = count;
    }
    public void setUps(int ups) {
        this.ups = ups;
    }
    public void setDowns(int downs) {
        this.downs = downs;
    }
    public void setUserID(long userID) {
        this.userID = userID;
    }
    public boolean isQuoted;
    public int beQuotedPosition;
}
