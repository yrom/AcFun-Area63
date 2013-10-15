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

import java.util.List;

import android.util.SparseArray;

/**
 * @author Yrom
 *
 */
public class Mentions {
    public int totalCount;
    public int totalPage;
    public int pageSize;
    public int nextPage;
    public int page;
    public int[] commentList;
    public List<Content> contentList;
    public SparseArray<Comment> commentArr;
    
}
