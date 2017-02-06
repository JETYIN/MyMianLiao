package com.tjut.mianliao.component;

import java.util.regex.Matcher;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils.TruncateAt;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.tjut.mianliao.MStaticInterface;
import com.tjut.mianliao.R;
import com.tjut.mianliao.data.ChannelInfo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.CfRecord;
import com.tjut.mianliao.forum.CfRecord.AtUser;
import com.tjut.mianliao.forum.CfReply;
import com.tjut.mianliao.forum.TopicPostActivity;
import com.tjut.mianliao.forum.nova.ForumPostDetailActivity;
import com.tjut.mianliao.profile.NewProfileActivity;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.util.EmotionManager;
import com.tjut.mianliao.util.Utils;
import com.umeng.analytics.MobclickAgent;

/**
 * @attr ref {@link com.tjut.mianliao.R.styleable#RichEmotionTextView_shouldMatchTopic}
 * @attr ref {@link com.tjut.mianliao.R.styleable#RichEmotionTextView_shouldMatchAtFriend}
 * @attr ref {@link com.tjut.mianliao.R.styleable#RichEmotionTextView_atSpanClickble}
 * @attr ref {@link com.tjut.mianliao.R.styleable#RichEmotionTextView_topicSpanClickble}
 */
public class RichEmotionTextView extends EmotionTextView {

    private static final int sChangedColor = 0xff7cbadb;
    
    /**
     * The {@code mClickable} will decide the {@link RichEmotionTextView} whether or
     * not can be clicked
     */
    private boolean mClickable;
    
    /**
     * The {@code mTopicSpanClickble} will decide the {@code #topic#} span whether or
     * not can be clicked
     */
    private boolean mTopicSpanClickble;
    
    /**
     * The {@code mAtSpanClickble} will decide the {@code @friend} span whether or
     * not can be clicked
     */
    private boolean mAtSpanClickble;
    
    /**
     * The {@code mShouldMatchTopic} will decide the {@link RichEmotionTextView} whether or
     * not to match {@code #topic#} 
     */
    private boolean mShouldMatchTopic ;
    
    /**
     * The {@code mShouldMatchAtFriend} will decide the {@link RichEmotionTextView} whether or
     * not to match {@code @friend}
     */
    private boolean mShouldMatchAtFriend;

    private Context mContext;
    public ColorStateList mTempColor;
    private ClickableText mClickableText;
    private CfRecord mCfPost;
    private int mTribeId;
    private RichEmotionTextView mView;
    private ChannelInfo mChannelInfo;
    private int mEmotionSize;
    private EmotionManager mEmotionManager;
    private boolean mIsFromTribe;
    private boolean mIsShowTribe;
    private boolean mIsShowSchoolName;
    private boolean mShowOtherSchoolTag;
    private boolean isShowSchool;

    public RichEmotionTextView(Context context) {
        this(context, null);
    }
    
    public RichEmotionTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttr(context, attrs);
        setEllipsize(TruncateAt.END);
    }

    private void initAttr(Context context, AttributeSet attrs) {
        mContext = context;
        Resources res = context.getResources();
        mTempColor = res.getColorStateList(R.drawable.selector_span_color);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RichEmotionTextView);
        mShouldMatchTopic = ta.getBoolean(R.styleable.RichEmotionTextView_shouldMatchTopic, true);
        mShouldMatchAtFriend = ta.getBoolean(R.styleable.RichEmotionTextView_shouldMatchAtFriend, true);
        mTopicSpanClickble = ta.getBoolean(R.styleable.RichEmotionTextView_topicSpanClickble, false);
        mAtSpanClickble = ta.getBoolean(R.styleable.RichEmotionTextView_atSpanClickble, false);
        mEmotionManager = EmotionManager.getInstance(context);
        ta.recycle();
        mView = this;
    }
    

    public void setText(CfRecord post) {
        if (post == null) {
            setText("");
            return;
        }
        mCfPost = post;
        CharSequence content = post.content;
        if (post instanceof CfReply) {
            content = ((CfReply) post).replyContent;
        }
        if (content == null) {
        	content = "";  
        }
        SpannableStringBuilder ssb = new SpannableStringBuilder(content);
        content = matchAtFriends(content, ssb);
        content = matchTopicContent(content, ssb);
        content = matchURL(content, ssb);
//        setText(getParseText(ssb));
        setText(ssb);
    }
    

    private CharSequence matchTopicContent(CharSequence content, SpannableStringBuilder ssb) {
        int matcherIndex = 0; 
        if (mShouldMatchTopic) {
            Matcher matcher = Utils.TOPIC_MATCH_PATTERN.matcher(content);
            int lastIndex = 0;
            while (matcher.find()) {
                String ts = matcher.group();
                int index = content.toString().indexOf(ts, lastIndex);
                int length = ts.length();
                // if need del all the space, we can use this method
                // ts.replaceAll(" ", "");
                ts = ts.charAt(0) + ts.substring(1, length - 1).trim() + ts.charAt(length - 1);
                ClickedObject co = new ClickedObject();
                co.type = ClickedObject.TYPE_TOPIC;
                co.content =  ts;
                co.index =  matcherIndex;
                matcherIndex++;
                ssb.replace(index, index + length, getClickbleSpanText(co));
                content = ssb.toString();
                lastIndex = ts.length() + content.toString().indexOf(ts, lastIndex);
            }
        }
        if (content.toString().lastIndexOf("#") == content.length() - 1 ||
                content.toString().lastIndexOf("＃") == content.length() -1 ) {
            ssb.append(getClickbleSpanText(" "));
        }
        return content;
    }

    private CharSequence matchAtFriends(CharSequence content, SpannableStringBuilder ssb) {
        int matcherIndex = 0;
        if (mShouldMatchAtFriend) {
            Matcher mRefMatcher = Utils.REF_FRIEND_PATTERN.matcher(content);
            int refLastIndex = 0;
            while (mRefMatcher.find()) {
                String ts = mRefMatcher.group();
                int index = content.toString().indexOf(ts, refLastIndex);
                int length = ts.length();
                ts = ts.charAt(0) + ts.substring(1, length - 1).trim() + ts.charAt(length - 1);
                ClickedObject co = new ClickedObject();
                co.type = ClickedObject.TYPE_AT_FRIEND;
                co.content =  ts;
                co.index =  matcherIndex;
                matcherIndex++;
                ssb.replace(index, index + ts.length(), getClickbleSpanText(co));
                content = ssb.toString();
                refLastIndex = index + 1;
            }
        }
        return content;
    }

    private CharSequence matchURL (CharSequence content, SpannableStringBuilder ssb) {
        int matcherIndex = 0;
        Matcher mRefMatcher = Utils.URL_MATCH_PATTERN.matcher(content);
        int refLastIndex = 0;
        while (mRefMatcher.find()) {
            String ts = mRefMatcher.group();
            int index = content.toString().indexOf(ts, refLastIndex);
            int length = ts.length();
            ts = ts.charAt(0) + ts.substring(1, length - 1).trim() + ts.charAt(length - 1);
            ClickedObject co = new ClickedObject();
            co.type = ClickedObject.TYPE_URL;
            co.content = ts;
            co.index = matcherIndex;
            matcherIndex++;
            ssb.replace(index, index + ts.length(), getClickbleSpanText(co));
            content = ssb.toString();
            refLastIndex = index + 1;
        }
        return content;
    }

    public SpannableString getClickbleSpanText(ClickedObject co) {
        SpannableString spanableInfo = new SpannableString(co.content); 
        mClickableText = new ClickableText();
        mClickableText.setContent(co);
        spanableInfo.setSpan(mClickableText, 0, co.content.length(),  
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);  
        return spanableInfo;      
    }
    
    public SpannableString getClickbleSpanText(String content) {
        SpannableString spanableInfo = new SpannableString(content); 
        mClickableText = new ClickableText();
        mClickableText.setContent(content);
        spanableInfo.setSpan(mClickableText, 0, content.length(),  
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);  
        return spanableInfo;      
    }
    
    /**
     * set the textview clickable
     * @param clickable
     */
    public void setTextClickble(boolean clickable) {
        mClickable = clickable;
    }
    
    public void setIsTribePosts(boolean isTribe) {
        mIsFromTribe = isTribe;
    }
    
    public void setIsShowSchoolName(boolean isShow) {
        mIsShowSchoolName = isShow;
    }
    
    public void setIsShowSchool(boolean isShow) {
        isShowSchool = isShow;
    }
    
    public void setIsShowOtherSchoolTag(boolean isShow) {
        mShowOtherSchoolTag = isShow;
    }
    
    public void setIsShowTribeIndetail(boolean isShow) {
        mIsShowTribe = isShow;
    }
    
    /**
     * set the topic span view clickable
     * @param clickable
     */
    public void setTopicSpanClickble(boolean clickable) {
        mTopicSpanClickble = clickable;
    }
    
    /**
     * Call this method, you can set the {@code @friend} span
     * can clickable or not
     * @param clickble
     */
    public void setAtSpanClickble(boolean clickble) {
        mAtSpanClickble = clickble;
    }
    
    /**
     * Call this method, set the {@code @friend}} should be matched,
     * <p> if the {@code @friend} matched, this span will be colored,
     * or it will be normal<p>
     */
    public void setShouldMatchAtFriends() {
        mShouldMatchAtFriend = true;
    }

    /**
     * Call this method, set the {@code #topic#} should be matched,
     * <p> if the {@code #topic#} matched, this span will be colored,
     * or it will be normal<p>
     */
    public void setShouldMatchTopic() {
        mShouldMatchTopic = true;
    }
    
    public void setChannelInfo(ChannelInfo channelInfo) {
        mChannelInfo = channelInfo;
    }

    boolean isTouchSpan = false;
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = super.onTouchEvent(event);
        if (mCfPost == null) {
            return result;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (isTouchSpan) {
                isTouchSpan = false;
                if (mView.getTag() instanceof ClickedObject) {
                    Intent intent;
                    ClickedObject tag = (ClickedObject) mView.getTag();
                    if (mTopicSpanClickble && tag.type == ClickedObject.TYPE_TOPIC) {
                        intent = new Intent(mContext, TopicPostActivity.class);
                        int length = tag.content.length();
                        String mTopic = tag.content.substring(1, length - 1).trim();
                        intent.putExtra(TopicPostActivity.POST_ID, mCfPost.postId);
                        
                        intent.putExtra(TopicPostActivity.CHANNEL_ID, mChannelInfo == null ? 0 : mChannelInfo.forumId);
                        intent.putExtra(TopicPostActivity.TOPIC_NAME, mTopic);
                        mContext.startActivity(intent);
                    } else if (tag.type == ClickedObject.TYPE_AT_FRIEND){
                        if (mAtSpanClickble && mCfPost.atUsers != null && mCfPost.atUsers.size() > 0) {
                            intent = new Intent(mContext, NewProfileActivity.class);
                            AtUser atUser = mCfPost.atUsers.get(tag.index);
                            UserInfo userInfo = new UserInfo();
                            userInfo.userId = atUser.userId;
                            intent.putExtra(UserInfo.INTENT_EXTRA_INFO, userInfo);
                            mContext.startActivity(intent);
                        } else {
//                            Toast.makeText(mContext, "atUser == null", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (tag.type == ClickedObject.TYPE_URL) {
                            try{
                                String urlString;
                                if (tag.content.startsWith("www") || tag.content.startsWith("WWW")) {
                                    urlString = "http://" + tag.content;
                                } else {
                                    urlString = tag.content;
                                }
                                intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(urlString));
                                mContext.startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(mContext, "网络地址格式错误",  Toast.LENGTH_SHORT).show();
                                System.out.println(e);
                            }
                        }
                    }
                }
            } else {
                if (!mClickable) {
                    return result;
                }
                if (mCfPost instanceof CfPost) {
                    showForumPostDetail();
                    MobclickAgent.onEvent(mContext, MStaticInterface.DISCUSS);
                }
            }
        }
        return result;
    }
    
    private void showForumPostDetail() {
        Intent cpdIntent = new Intent(mContext, ForumPostDetailActivity.class);
        cpdIntent.putExtra(ForumPostDetailActivity.EXTRL_POST_DATA, mCfPost);
        cpdIntent.putExtra(ForumPostDetailActivity.EXTRL_CHANNEL_INFO, mChannelInfo);
        cpdIntent.putExtra(ForumPostDetailActivity.EXTRL_SHOW_OTHER_SCHOOL_TAG, mShowOtherSchoolTag);
        cpdIntent.putExtra(ForumPostDetailActivity.EXTRL_ISSHOW_SCHOOL_TOPIC, isShowSchool);
        cpdIntent.putExtra(ForumPostDetailActivity.EXTRL_ISSHOW_SCHOOL_NAME, mIsShowSchoolName);
        cpdIntent.putExtra(ForumPostDetailActivity.EXTRL_IS_FORM_TRIBE, mIsFromTribe);
        cpdIntent.putExtra(ForumPostDetailActivity.EXTRL_IS_SHOW_TRIBE, mIsShowTribe);
        mContext.startActivity(cpdIntent);
    }

    
    class ClickableText extends ClickableSpan implements OnClickListener {

        private ClickedObject cObject;
        
        private String content;

        public void setContent(ClickedObject co) {
            this.cObject = co;
        }

        public void setContent(String content) {
            this.content = content;
        }
        
        @SuppressLint("NewApi")
        @Override
        public void onClick(View v) {
            isTouchSpan = true;
            mView.setTag(cObject);
            TextView tv = (TextView) v;
            tv.setHighlightColor(Color.TRANSPARENT);
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setColor(sChangedColor);
            ds.setUnderlineText(false);
        }
    }
    
    public class ClickedObject {
        
        public static final int TYPE_TOPIC = 1; // topic
        
        public static final int TYPE_AT_FRIEND = 2; // @friend

        public static final int TYPE_URL = 3; // @friend
        
        /**
         * type : 1 -- > 话题 ;2 -- > @好友
         */
        public int type;  
        
        public String content;
        
        public int index;
    }

}
