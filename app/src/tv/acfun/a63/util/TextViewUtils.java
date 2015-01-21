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
package tv.acfun.a63.util;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.XMLReader;

import tv.acfun.a63.AcApp;
import tv.acfun.a63.R;
import tv.acfun.a63.api.entity.Comment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StrikethroughSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.TextView;

public class TextViewUtils {
    
	public static void setCommentContent(final TextView comment, Comment c) {
	    if(comment.getMovementMethod() != null) // reset focus
	        comment.setMovementMethod(null);
        String text = c.content;
        if(TextUtils.isEmpty(text)){
            comment.setText("");
            return;
        }
        text = replace(text);
        try{
            comment.setText(Html.fromHtml(text, new ImageGetter() {

                @Override
                public Drawable getDrawable(String source) {
                    try {
                        Bitmap bm = AcApp.getBitmpInCache(source);
                        if (bm == null) {
                            bm = BitmapFactory.decodeStream(comment.getContext().getAssets()
                                    .open(source));
                            AcApp.putBitmapInCache(source, bm);
                        }
                        Drawable drawable = new BitmapDrawable(comment.getResources(), bm);
                        if (drawable != null) {
                            int w = comment.getResources().getDimensionPixelSize(
                                    R.dimen.emotions_column_width);
                            drawable.setBounds(0, 0, w, drawable.getIntrinsicHeight() * w
                                    / drawable.getIntrinsicWidth());
                        }

                        return drawable;

                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }

                }
            }, new Html.TagHandler() {

                @Override
                public void handleTag(boolean opening, String tag, Editable output,
                        XMLReader xmlReader) {
                    int len = output.length();
                    if (opening) {
                        if (tag.equalsIgnoreCase("strike")) {
                            output.setSpan(new StrikethroughSpan(), len, len,
                                    Spannable.SPAN_MARK_MARK);
                        }
                    } else {
                        if (tag.equalsIgnoreCase("strike")) {
                            end((SpannableStringBuilder) output, StrikethroughSpan.class,
                                    new StrikethroughSpan());
                        }
                    }
                }
            }));
        } catch (ArrayIndexOutOfBoundsException e) {
            // FIXME: text 的格式可能有问题
            comment.setText(text);
            Log.e("wtf", "set comment",e);
        }
//        comment.setTextColor(Color.BLACK);
        comment.setTextSize(AcApp.getPreferenceFontSize());
        Pattern http = Pattern.compile("http://[a-zA-Z0-9+&@#/%?=~_\\-|!:,\\.;]*[a-zA-Z0-9+&@#/%=~_|]",
                Pattern.CASE_INSENSITIVE);
        Linkify.addLinks(comment, http, "http://");
        Linkify.addLinks(comment, Pattern.compile("(ac\\d{5,})", Pattern.CASE_INSENSITIVE), "ac://");
    }
	static void end(SpannableStringBuilder text, Class<?> kind,
                Object repl) {
        int len = text.length();
        Object obj = getLast(text, kind);
        int where = text.getSpanStart(obj);
        
        text.removeSpan(obj);
        
        if (where != len) {
        text.setSpan(repl, where < 0?0:where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        
        return;
    }
    public static <T> T getLast(Spanned text, Class<T> kind) {
        /*
         * This knows that the last returned object from getSpans()
         * will be the most recently added.
         */
        Object[] objs = text.getSpans(0, text.length(), kind);

        if (objs.length == 0) {
            return null;
        } else {
            return (T) objs[objs.length - 1];
        }
    }
    private static String replace(String text) {
        String reg = "\\[emot=(.*?),(.*?)\\/\\]";
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(text);
        while(m.find()){
            String id = m.group(2);
            String cat = m.group(1);
            int parsedId;
            try {
                parsedId = Integer.parseInt(id);
            } catch (NumberFormatException e) {
                // Invalid format text
                continue;
            }
            if (parsedId > 54)
                id = "54";
            String replace = cat.equals("brd") || cat.equals("td") ? 
                    "<img src='emotion/"+cat+"/%02d.gif'/>" : "<img src='emotion/%02d.gif'/>";
            text = text.replace(m.group(), String.format(replace, parsedId));
        }
        reg = "\\[at\\](.*?)\\[\\/at\\]";
        m = Pattern.compile(reg).matcher(text);
        while(m.find()){
            text = text.replace(m.group(), "<font color=\"#FF9A03\" >@" + m.group(1)+"</font> ");
        }
        reg = "\\[color=(.*?)\\]";
        m = Pattern.compile(reg).matcher(text);
        while (m.find()){
            text = text.replace(m.group(), "<font color=\"" + m.group(1) + "\" >");
        }
        text = text.replace("[/color]", "</font>");
        text = text.replaceAll("\\[size=(.*?)\\]","").replace("[/size]", "");
        
        reg = "\\[img=(.*?)\\]";
        m = Pattern.compile(reg).matcher(text);
        while (m.find()){
            text = text.replace(m.group(), m.group(1));
        }
        text = text.replace("[img]","").replace("[/img]", "");
        text = text.replaceAll("\\[ac=\\d{5,}\\]", "").replace("[/ac]", "");
        text = text.replaceAll("\\[font[^\\]]*?\\]", "").replace("[/font]", "");
        text = text.replaceAll("\\[align[^\\]]*?\\]", "").replace("[/align]", "");
        text = text.replaceAll("\\[back[^\\]]*?\\]", "").replace("[/back]", "");
        text = text.replace("[s]", "<strike>").replace("[/s]", "</strike>");
        text = text.replace("[b]", "<b>").replace("[/b]", "</b>");
        text = text.replace("[u]", "<u>").replace("[/u]", "</u>");
        text = text.replace("[email]", "<font color=\"#FF9A03\"> ").replace("[/email]", "</font>");
        return text;
    }
    
    /**
     * 字符 转义字符
     *   “ &quot;
     *   & &amp; 
     *   < &lt;
     *   > &gt;
     *     &nbsp;
     */
    public static String getSource(String escapedHtml) {
        if(escapedHtml == null) return "";
        return escapedHtml.replaceAll("&quot;", "\"").replaceAll("&amp;", "&").replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">").replaceAll("&nbsp;", " ");
    }
    

    public static TextView createBubbleTextView(Context context, String text){
      //creating textview dynamically
      TextView tv = new TextView(context);
      tv.setText(text);
      tv.setTextSize(16);
      tv.setBackgroundResource(R.drawable.oval);
      tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.forward, 0, 0, 0);
      return tv;
    }

    public static Drawable convertViewToDrawable(View view) {
      int spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
      view.measure(spec, spec);
      view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
      Bitmap b = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                Bitmap.Config.ARGB_8888);
      Canvas c = new Canvas(b);
      c.translate(-view.getScrollX(), -view.getScrollY());
      view.draw(c);
      view.setDrawingCacheEnabled(true);
      Bitmap cacheBmp = view.getDrawingCache();
      Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
      view.destroyDrawingCache();
      return new BitmapDrawable(view.getResources(),viewBmp);

    }
}
