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

import android.util.SparseArray;

/**
 * {
  "page": 1,
  "nextPage": 1,
  "prePage": 1,
  "totalCount": 11,
  "totalPage": 1,
  "pageSize": 50,
  "desc": true,
  "commentList": [
    16250230,
    16249699,
    16249388,
    16249066,
    16249021,
    16248998,
    16248990,
    16248884,
    16248841,
    16248741,
    16248715
  ],
  "commentContentArr": {
    "c16248741": {
      "cid": 16248741,
      "content": "看成泷泽萝拉了[emot\u003dac,16/]",
      "userName": "爱护萝莉的怪蜀黍",
      "userImg": "http://w5cdn.ranktv.cn/dotnet/artemis/u/cms/www/201206/24205534dvh2.jpg",
      "quoteId": 0,
      "count": 2,
    },
    "c16248998": {
      "cid": 16248998,
      "content": "[emot\u003dac,06/]你不是一个人~",
      "userName": "骑猪去看海",
      "userImg": "http://w5cdn.ranktv.cn/dotnet/artemis/u/cms/www/201204/052228438o0r.jpg",
      "quoteId": 16248741,
      "count": 6,
    },
    "c16249066": {
      "cid": 16249066,
      "content": "[emot\u003dac,17/]我们是三个人",
      "userName": "推倒幼女同好會",
      "userID": 483895,
      "postDate": "2013-09-04 09:50:56",
      "userImg": "http://w5cdn.ranktv.cn/dotnet/artemis/u/cms/www/201306/28011055zi6g.jpg",
      "userClass": "",
      "quoteId": 16248998,
      "count": 8,
      "ups": 0,
      "downs": 0
    },
    "c16249699": {
    }
  }
}
 * @author Yrom
 *
 */
public class Comments {
    public int totalCount;
    public int totalPage;
    public int pageSize;
    public int nextPage;
    public int page;
    public int[] commentList;
    public SparseArray<Comment> commentArr;
    
}
