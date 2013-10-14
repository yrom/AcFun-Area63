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

package tv.acfun.a63.adapter;

import java.util.List;

import tv.acfun.a63.R;
import tv.acfun.a63.api.ArticleApi;
import tv.acfun.a63.api.entity.Comment;
import tv.acfun.a63.api.entity.Content;
import tv.acfun.a63.util.TextViewUtils;
import android.content.Context;
import android.text.Html;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * @author Yrom
 * 
 */
public class MentionsAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private SparseArray<Comment> data;
    private List<Content> contentList;
    private List<Integer> commentIdList;

    public MentionsAdapter(Context context, List<Content> contentList, SparseArray<Comment> data,
            List<Integer> commentIdList) {
        this.mInflater = LayoutInflater.from(context);
        this.data = data;
        this.contentList = contentList;
        this.commentIdList = commentIdList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Comment c = getCommentItem(position);
        Content article = getItem(position);
        MentionsHolder holder = null;
        if (convertView == null) {
            holder = new MentionsHolder();
            convertView = mInflater.inflate(R.layout.mentions_list_item, parent, false);
            holder.contentTitle = (TextView) convertView.findViewById(R.id.article_item_title);
            holder.user = (TextView) convertView.findViewById(R.id.user_name);
            holder.channel = (TextView) convertView.findViewById(R.id.item_tag_channel);
            holder.quotedUser = (TextView) convertView.findViewById(R.id.quoted_user_name);
            holder.comments = (TextView) convertView.findViewById(R.id.comments_content);
            holder.quotedComments = (TextView) convertView.findViewById(R.id.quoted_comments_content);
            convertView.setTag(holder);
        } else {
            holder = (MentionsHolder) convertView.getTag();
        }
        holder.channel.setText(ArticleApi.getChannelName(article.channelId));
        holder.contentTitle.setText(Html.fromHtml("<font color=\"#33B5E5\">"+article.title+"</font> <font color=\"#cccccc\">(ac"+article.aid+")</font>"));
        holder.user.setText("#" + c.count + " " + c.userName);
        TextViewUtils.setCommentContent(holder.comments, c);
        int quoteId = c.quoteId;
        Comment quote = data.get(quoteId);
        holder.quotedUser.setText("#" + quote.count + " " + quote.userName);
        TextViewUtils.setCommentContent( holder.quotedComments,quote);
        return convertView;
    }

    static class MentionsHolder {
        TextView contentTitle, channel, user, quotedUser, comments, quotedComments;
    }
    private Comment getCommentItem(int position){
        try {
            Integer id = commentIdList.get(position);
            if (id != null)
                return data.get(id);
        } catch (IndexOutOfBoundsException e) {}
        return null;
        
    }
    @Override
    public int getCount() {
        return contentList.size();
    }

    @Override
    public Content getItem(int position) {
        return contentList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).aid;
    }

    public void setData(List<Content> contentList, SparseArray<Comment> data,
            List<Integer> commentIdList) {
        this.data = data;
        this.contentList = contentList;
        this.commentIdList = commentIdList;
        notifyDataSetChanged();
    }
}
