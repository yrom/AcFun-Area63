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
package tv.acfun.a63.view;

import java.util.List;

import tv.acfun.a63.R;
import tv.acfun.a63.util.DensityUtil;
import tv.acfun.a63.util.Theme;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
/**
 * 盖楼部分评论视图
 * @author Yrom
 *
 */
public class FloorsView extends LinearLayout {
	private Drawable mBorder;
    private int mMaxNum;
	@Override
	public boolean isDuplicateParentStateEnabled() {
	    return true;
	}
	public FloorsView(Context context) {
		this(context, null);
	}

	public FloorsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	private void initView(Context context) {
		setOrientation(VERTICAL);
		mMaxNum = getResources().getInteger(R.integer.max_floors_w);
		setBackgroundResource(R.drawable.item_background);
	}

	public void setQuoteList(List<View> quoteList) {
		if(quoteList == null || quoteList.isEmpty()) {
			removeAllViewsInLayout();
			return;
		}
		int spacing = DensityUtil.dip2px(getContext(), 4);
		int j = 0;
		for(int i=quoteList.size()-1;i>=0;i--){
			LinearLayout.LayoutParams params = generateDefaultLayoutParams();
			int k = spacing * i;
			if(quoteList.size()>mMaxNum+2 && i>mMaxNum){
			    k = spacing*mMaxNum;
			}
			params.leftMargin = k;
			params.rightMargin = k;
			params.topMargin = j==0?k:0;
			View v = quoteList.get(i);
			TextView floor = (TextView) v.findViewById(R.id.floor);
			floor.setText(String.valueOf(j+1));
			addViewInLayout(v, j++, params);
		}
	}

	public void setFloorBorder(Drawable border) {
		this.mBorder = border;
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
	    if(!isPressed()){
    		final int i = getChildCount();
    		if(this.mBorder == null){
    		    // stroke border if above v14
    		    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    		        this.mBorder = getResources().getDrawable(Theme.isNightMode() ? R.drawable.floors_border_dark : R.drawable.floors_border);
    		    else
    		        this.mBorder = getResources().getDrawable(Theme.isNightMode() ? R.drawable.comment_floor_bg_dark : R.drawable.comment_floor_bg);
    		}
    		if ((this.mBorder != null) && (i > 0))
    			for (int j = i - 1; j >=0; j--) {
    				View child = getChildAt(j);
    				this.mBorder.setBounds(child.getLeft(), child.getLeft(),
    						child.getRight(), child.getBottom());
    				// draw background color only once
    				if (j == i - 1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
    				    ColorDrawable drawable = new ColorDrawable(Theme.isNightMode()? 0xFF545454 : 0xFFFFFEEE);
    				    drawable.setBounds(this.mBorder.getBounds());
    				    drawable.draw(canvas);
    				}
    				this.mBorder.draw(canvas);
    			}
	    }
		super.dispatchDraw(canvas);
	}
}
