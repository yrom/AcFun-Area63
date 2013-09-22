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

import java.io.IOException;

import tv.acfun.a63.AcApp;
import tv.acfun.a63.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * @author Yrom
 * 
 */
public class EmotionView extends View {
//    private static final String TAG = "EmotionView";
    private int mId;
    private int mWidth;
    private int mHeight;
    private Drawable mDrawable;
    private int mPadding;

    public EmotionView(Context context) {
        super(context);
        mWidth = mHeight = getResources().getDimensionPixelSize(R.dimen.emotions_column_width);
        mPadding = (int) (4 * AcApp.density + 0.5f);
    }

    public void setEmotionId(int id) {
        if (mId != id) {
            String name = getEmotionName(id);
            try {
                Bitmap bm = AcApp.getBitmpInCache(name);
                if (bm == null) {
                    Options opts = new Options();
                    opts.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(getContext().getAssets().open(name), null, opts);
                    if (opts.outWidth > mWidth || opts.outHeight > mHeight) {
                        int sample = Math.max(opts.outWidth / mWidth, opts.outHeight / mHeight);
                        
                        opts.inSampleSize = sample;
//                        Log.d(TAG, String.format("ow=%d,oh=%d, mw=%d,mh=%d, scale to sample=%d",opts.outWidth,opts.outHeight,mWidth,mHeight,sample));
                    }
                    opts.inJustDecodeBounds = false;
                    bm = BitmapFactory
                            .decodeStream(getContext().getAssets().open(name), null, opts);
                    AcApp.putBitmapInCache(name, bm);
//                    Log.d(TAG, "put emotion in cache : " + name);
                }
                mDrawable = new BitmapDrawable(getResources(), bm);
                
                mDrawable.setBounds(0, 0, mWidth, mDrawable.getIntrinsicHeight()*mHeight/mDrawable.getIntrinsicWidth());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = getDefaultSize(mWidth+mPadding, widthMeasureSpec);
        setMeasuredDimension(width, width);
    }

    public String getEmotionName(int id) {
        if (id > 54) {
            return String.format("emotion/ais/%02d.gif", id - 54);
        }
        return String.format("emotion/%02d.gif", id);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mDrawable == null) {
            return;
        }

        int saveCount = canvas.getSaveCount();
        canvas.save();
        mDrawable.draw(canvas);
        canvas.restoreToCount(saveCount);
    }
}
