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
import tv.acfun.a63.api.entity.Comment;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.text.util.Linkify;
import android.widget.TextView;

public class TextViewUtils {
	
	public static void setCommentContent(final TextView comment, Comment c) {
        String text = c.content;
        text = replace(text);
        comment.setText(Html.fromHtml(text,new ImageGetter() {
            
            @Override
            public Drawable getDrawable(String source) {
                try {
                    Drawable drawable = Drawable.createFromStream(comment.getContext().getAssets().open(source),source);
                    if(drawable!=null)
                        drawable.setBounds(0, 0, (int)(drawable.getIntrinsicWidth()*AcApp.density+1), (int)(drawable.getIntrinsicHeight()*AcApp.density+1));
                    return drawable;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
                
            }
        },new Html.TagHandler() {
            
            @Override
            public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
                int len = output.length();
                if(opening){
                    if(tag.equalsIgnoreCase("strike")){
                        output.setSpan(new StrikethroughSpan(), len, len, Spannable.SPAN_MARK_MARK);
                    }
                }else{
                    if(tag.equalsIgnoreCase("strike")){
                        end((SpannableStringBuilder) output,StrikethroughSpan.class,new StrikethroughSpan());
                    }
                }
            }
        }));
        comment.setTextColor(Color.BLACK);
        Pattern http = Pattern.compile("http://[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?^=%&amp;:/~\\+#]*[\\w\\-\\@?^=%&amp;/~\\+#])?",
                Pattern.CASE_INSENSITIVE);
        Linkify.addLinks(comment, http, "http://");
        Linkify.addLinks(comment, Pattern.compile("(ac\\d{5,})", Pattern.CASE_INSENSITIVE), "av://");
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
    static Object getLast(Spanned text, Class<?> kind) {
        /*
         * This knows that the last returned object from getSpans()
         * will be the most recently added.
         */
        Object[] objs = text.getSpans(0, text.length(), kind);

        if (objs.length == 0) {
            return null;
        } else {
            return objs[objs.length - 1];
        }
    }
    private static String replace(String text) {
        String reg = "\\[emot=(.*?),(.*?)\\/\\]";
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(text);
        while(m.find()){
            String id =  m.group(2);
            // FIXME: id 50 以上的表情
            if(Integer.parseInt(id)>50)
                id = "50";
            text = text.replace(m.group(),String.format("<img src='emotion/%02d.png' />", Integer.parseInt(id)));
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
        text = text.replaceAll("\\[back[^\\]]*?\\]", "").replace("[/back]", "");
        text = text.replace("[s]", "<strike>").replace("[/s]", "</strike>");
        text = text.replace("[b]", "<b>").replace("[/b]", "</b>");
        return text;
    }
}
