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

import java.util.ArrayList;
import java.util.List;

import tv.acfun.a63.AcApp;
import tv.acfun.a63.R;
import tv.acfun.a63.api.entity.Comment;
import tv.acfun.a63.util.DensityUtil;
import tv.acfun.a63.util.TextViewUtils;
import tv.acfun.a63.view.FloorsView;
import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

/**
 * @author Yrom
 * 
 */
public class CommentsAdapter extends BaseAdapter {

    protected LayoutInflater mInflater;
    private SparseArray<Comment> data;
    private List<Integer> commentIdList;
    private Context mContext;
    private int maxNumOfFloor;

    public CommentsAdapter(Context context, SparseArray<Comment> data, List<Integer> commentIdList) {
        this.mInflater = LayoutInflater.from(context);
        this.mContext = context;
        this.data = data;
        this.commentIdList = commentIdList;
        maxNumOfFloor = AcApp.getNumOfFloors();
        if (maxNumOfFloor == 0)
            maxNumOfFloor = 10;
    }

    public void setData(SparseArray<Comment> data, List<Integer> commentIdList) {
        this.data = data;
        this.commentIdList = commentIdList;
    }

    @Override
    public int getCount() {
        if(commentIdList == null) return 0;
        return commentIdList.size();
    }

    @Override
    public Comment getItem(int position) {
        try {
            Integer id = commentIdList.get(position);
            if (id != null)
                return data.get(id);
        } catch (IndexOutOfBoundsException e) {}
        return null;
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    private int frameId = R.id.floor;
//    private View.OnClickListener mListener = new View.OnClickListener() {
//
//        @Override
//        public void onClick(View v) {
//
//            if (mOnClickListener != null) {
//                int position = (Integer) v.getTag();
//                mOnClickListener.onClick(v, position);
//            }
//        }
//
//    };

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Comment c = getItem(position);

        CommentViewHolder holder = null;
        if (convertView == null) {
            holder = new CommentViewHolder();
            convertView = mInflater.inflate(R.layout.comments_listitem, parent,false);
            holder.user = (TextView) convertView.findViewById(R.id.user_name);
            holder.content = (TextView) convertView.findViewById(R.id.comments_content);
            holder.quoteImage = convertView.findViewById(R.id.quote_img);
            convertView.setTag(holder);
        } else {
            holder = (CommentViewHolder) convertView.getTag();
            if (holder.hasQuote && holder.quoteFrame != null) {
                holder.quoteFrame.removeAllViews();
            }
            convertView.findViewById(R.id.requote).setVisibility(View.GONE);
        }
        holder.user.setText("#" + c.count + " " + c.userName);
//        holder.quoteImage.setTag(position);
//        holder.quoteImage.setOnClickListener(mListener);
        TextViewUtils.setCommentContent(holder.content, c);
        int quoteId = c.quoteId;
        holder.hasQuote = quoteId > 0;
        List<View> quoteList = new ArrayList<View>();
        handleQuoteList(position, convertView, holder, quoteId, quoteList);
        holder.quoteFrame.setQuoteList(quoteList);
        if (!quoteList.isEmpty()) {
            RelativeLayout.LayoutParams floorsLayoutParams = new LayoutParams(-1, -2);
            int margin = DensityUtil.dip2px(mContext, 4);
            floorsLayoutParams.setMargins(margin, 0, margin, margin);
            ((ViewGroup) convertView).addView(holder.quoteFrame, floorsLayoutParams);
        }
        RelativeLayout.LayoutParams userLayoutParams = (LayoutParams) holder.user.getLayoutParams();
        userLayoutParams.addRule(RelativeLayout.BELOW, holder.quoteFrame.getChildCount() > 0 ? frameId : R.id.requote);
        holder.user.setLayoutParams(userLayoutParams);
        handlePadding(position, convertView);
        return convertView;
    }

    private void handlePadding(int position, View convertView) {
        int padding = DensityUtil.dip2px(mContext, 8);
        if (position == 0) {
            int paddingTop = mInflater.getContext().getResources()
                    .getDimensionPixelSize(R.dimen.abs__action_bar_default_height);
            convertView.setPadding(padding, paddingTop + padding, padding, padding*2);
        } else
            convertView.setPadding(padding, padding*2, padding, padding*2);
    }

    private void handleQuoteList(int position, View convertView, CommentViewHolder holder, int quoteId,
            List<View> quoteList) {
        if (holder.hasQuote || holder.quoteFrame == null) {
            FloorsView floors = new FloorsView(mContext);
            floors.setId(frameId);
            holder.quoteFrame = floors;
        }

        int num = 0;
        for (Comment quote = data.get(quoteId); quote != null && num < maxNumOfFloor;
                num++, quoteId = quote.quoteId, quote = data.get(quoteId)) {

            if (quote.isQuoted) {
                if (quote.beQuotedPosition == position) {
                    quoteList.add(generateQuoteFrame(quote));
                } else {
                    convertView.findViewById(R.id.requote).setVisibility(View.VISIBLE);
                }
            } else {
                quote.isQuoted = true;
                quote.beQuotedPosition = position;
                quoteList.add(generateQuoteFrame(quote));
            }
        }
    }

    private RelativeLayout generateQuoteFrame(Comment quote) {
        RelativeLayout quoteFrame = (RelativeLayout) mInflater.inflate(R.layout.comments_quote_item, null);
        TextView username = (TextView) quoteFrame.findViewById(R.id.user_name);
        username.setText("#" + quote.count + " " + quote.userName);
        TextView content = (TextView) quoteFrame.findViewById(R.id.comments_content);
        TextViewUtils.setCommentContent(content, quote);

        return quoteFrame;
    }

//    private OnQuoteClickListener mOnClickListener;
//
//    public void setOnClickListener(OnQuoteClickListener l) {
//        mOnClickListener = l;
//    }
//
//    public interface OnQuoteClickListener {
//
//        void onClick(View v, int position);
//    }

    static class CommentViewHolder {

        TextView user;
        TextView content;
        View quoteImage;
        boolean hasQuote;
        FloorsView quoteFrame;

    }
}
