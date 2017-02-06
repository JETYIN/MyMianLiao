package com.tjut.mianliao.component.forum;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tjut.mianliao.R;
import com.tjut.mianliao.component.MLLinkMovementMethod;
import com.tjut.mianliao.component.RichEmotionTextView;
import com.tjut.mianliao.contact.UserRemarkManager;
import com.tjut.mianliao.forum.CfReply;
import com.tjut.mianliao.forum.nova.ReplyViewManager;
import com.tjut.mianliao.forum.nova.ReplyViewManager.ReplyViewListener;
import com.tjut.mianliao.util.Utils;

public class ReplyView extends LinearLayout implements View.OnClickListener {

    private ArrayList<CfReply> mReplies = new ArrayList<>();
    public int maxCount;
    private Context mContext;
    private UserRemarkManager mRemarkManager;
    private View mViewMore;
    protected CfReply mCurrentReply;
    private final int mNameColorNormal, mNameColorNight;
    private ReplyViewManager mViewManager;
    private ArrayList<ReplyViewListener> viewListeners;
    private CfReply mParentReply;

    public ReplyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mRemarkManager = UserRemarkManager.getInstance(context);
        mViewManager = ReplyViewManager.getInstance(context);
        setOrientation(LinearLayout.VERTICAL);
        setPadding(10, 0, 10, 0);
        mNameColorNormal = getResources().getColor(R.color.channel_post_reply_name);
        mNameColorNight = getResources().getColor(R.color.txt_tab_item_purple);
        viewListeners = mViewManager.getListeners();
        setViewBg();
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public void setReplies(CfReply parentReply, ArrayList<CfReply> replies) {
        mParentReply = parentReply;
        this.mReplies.clear();
        removeAllViews();
        setViewBg();
        this.mReplies.addAll(replies);
        int i = 0;
        if (replies.size() > maxCount) {
            while (i < maxCount) {
                addView(getView(replies.get(i)));
                i++;
            }
            addView(getView(null));
        } else {
            for (CfReply reply : replies) {
                addView(getView(reply));
            }
        }
    }

    private void setViewBg() {
        setBackgroundResource(R.drawable.bg_second_reply);
    }

    /**
     * @param reply
     * @return
     */
    private View getView(CfReply reply) {
        View view = null;
        if (reply != null) {
            RichEmotionTextView tv = (RichEmotionTextView) inflate(mContext, R.layout.list_item_replyview, null);
            tv.setMovementMethod(MLLinkMovementMethod.getInstance());
            tv.setTextColor(0XFF2E2E2E);
            String name1 = reply.userInfo.getDisplayName(mContext);
            String name2 = mRemarkManager.getRemark(reply.targetUid, reply.targetReplyName);
            CharSequence content = mContext.getString(R.string.post_reply_content, name1, name2, reply.content);
            content = Utils.getColoredText(content, name1, mNameColorNormal, true);
            content = Utils.getColoredText(content, name2, mNameColorNormal, true);
            reply.replyContent = content;
            tv.setText(reply);
            tv.setTopicSpanClickble(true);
            
            tv.setTag(reply);
            tv.setOnClickListener(this);
            view = tv;
        } else {
            String showMore = mContext.getString(R.string.post_more_reply, mReplies.size() - maxCount);
            TextView tv = new TextView(mContext);
            tv.setGravity(Gravity.RIGHT);
            tv.setPadding(0, 20, 10, 20);
            tv.setTextColor(0XFFD0D0D0);
            tv.setOnClickListener(this);
            tv.setText(showMore);
            view = tv;
            mViewMore = view;
        }

        return view;
    }

    private void showMoreReplies() {
        for (int i = maxCount; i < mReplies.size(); i++) {
            addView(getView(mReplies.get(i)));
        }
        mViewMore.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        Object obj = v.getTag();
        if (obj == null) {
            showMoreReplies();
            return;
        }
        if (v.getTag() instanceof CfReply) {
            mCurrentReply = (CfReply) v.getTag();
            for (ReplyViewListener listener : viewListeners) {
                listener.onClickReplyView(mParentReply, mCurrentReply);
            }
        }
    }


}
