
package tv.acfun.a63;

import java.text.SimpleDateFormat;
import java.util.List;

import tv.acfun.a63.base.BaseActivity;
import tv.acfun.a63.util.ActionBarUtil;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.umeng.fb.FeedbackAgent;
import com.umeng.fb.model.Conversation;
import com.umeng.fb.model.DevReply;
import com.umeng.fb.model.Reply;

/**
 * copy version of
 * {@code https://github.com/umeng/umeng-android-sdk-theme/blob/master/fb/v4.3/src/com/umeng/fb/ConversationActivity.java}
 * 
 */
public class ConversationActivity extends BaseActivity {

    private FeedbackAgent agent;
    private Conversation defaultConversation;
    private ReplyListAdapter adapter;
    private ListView replyListView;
    RelativeLayout header;
    int headerHeight;
    int headerPaddingOriginal;
    EditText userReplyContentEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.umeng_fb_activity_conversation);
        ActionBar bar = getSupportActionBar();
        ActionBarUtil.setXiaomiFilterDisplayOptions(bar, false);
        bar.setTitle("反(tu)馈(cao)");
        try {
            agent = new FeedbackAgent(this);
            defaultConversation = agent.getDefaultConversation();

            replyListView = (ListView) findViewById(R.id.umeng_fb_reply_list);

            // setListViewHeader();

            adapter = new ReplyListAdapter(this);
            replyListView.setAdapter(adapter);

            // sync up the conversations on Activity start up.
            sync();

            userReplyContentEdit = (EditText) findViewById(R.id.umeng_fb_reply_content);

            findViewById(R.id.umeng_fb_send).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    String content = userReplyContentEdit.getEditableText().toString().trim();
                    if (TextUtils.isEmpty(content))
                        return;

                    userReplyContentEdit.getEditableText().clear();

                    defaultConversation.addUserReply(content);
                    // adapter.notifyDataSetChanged();

                    // scoll to the end of listview after updating the
                    // conversation.
                    // replyList.setSelection(adapter.getCount()-1);

                    sync();

                    // hide soft input window after sending.
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null)
                        imm.hideSoftInputFromWindow(userReplyContentEdit.getWindowToken(), 0);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            this.finish();
        }

    }

    void sync() {
        Conversation.SyncListener listener = new Conversation.SyncListener() {

            @Override
            public void onSendUserReply(List<Reply> replyList) {
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onReceiveDevReply(List<DevReply> replyList) {}
        };
        defaultConversation.sync(listener);
    }

    class ReplyListAdapter extends BaseAdapter {

        Context mContext;
        LayoutInflater mInflater;

        public ReplyListAdapter(Context context) {
            this.mContext = context;
            mInflater = LayoutInflater.from(mContext);
        }

        @Override
        public int getCount() {
            List<Reply> replyList = defaultConversation.getReplyList();
            return (replyList == null) ? 0 : replyList.size();
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getView(int, android.view.View,
         * android.view.ViewGroup)
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.umeng_fb_list_item, null);

                holder = new ViewHolder();

                holder.replyDate = (TextView) convertView.findViewById(R.id.umeng_fb_reply_date);

                holder.replyContent = (TextView) convertView.findViewById(R.id.umeng_fb_reply_content);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Reply reply = defaultConversation.getReplyList().get(position);

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

            if (reply instanceof DevReply) {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT); // ALIGN_PARENT_RIGHT
                holder.replyContent.setLayoutParams(layoutParams);

                // set bg after layout
                holder.replyContent.setBackgroundResource(R.drawable.umeng_fb_reply_left_bg);
            } else {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT); // ALIGN_PARENT_RIGHT
                // layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
                holder.replyContent.setLayoutParams(layoutParams);
                holder.replyContent.setBackgroundResource(R.drawable.umeng_fb_reply_right_bg);
            }

            holder.replyDate.setText(SimpleDateFormat.getDateTimeInstance().format(reply.getDatetime()));
            holder.replyContent.setText(reply.getContent());
            return convertView;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getItem(int)
         */
        @Override
        public Object getItem(int position) {
            return defaultConversation.getReplyList().get(position);
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getItemId(int)
         */
        @Override
        public long getItemId(int position) {
            return position;
        }

        class ViewHolder {

            TextView replyDate;
            TextView replyContent;

        }
    }

}
