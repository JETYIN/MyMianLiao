package com.tjut.mianliao.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.text.TextUtils;

import com.tjut.mianliao.R;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.data.mycollege.NoteInfo;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.CfRecord;
import com.tjut.mianliao.forum.CfReply;
import com.tjut.mianliao.util.MsTaskListener.MsTaskType;

public class MsTaskManager {

    private static WeakReference<MsTaskManager> sInstanceRef;

    private Context mContext;
    private ArrayList<MsTaskListener> mListeners;

    public static synchronized MsTaskManager getInstance(Context context) {
        if (sInstanceRef != null && sInstanceRef.get() != null) {
            return sInstanceRef.get();
        }
        MsTaskManager instance = new MsTaskManager(context);
        sInstanceRef = new WeakReference<MsTaskManager>(instance);
        return instance;
    }

    private MsTaskManager(Context context) {
        mContext = context.getApplicationContext();
        mListeners = new ArrayList<MsTaskListener>();
    }

    public void registerListener(MsTaskListener listener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(0, listener);
        }
    }

    public void unregisterListener(MsTaskListener listener) {
        mListeners.remove(listener);
    }

    public void startForumPostTask(boolean isEdit,
            HashMap<String, String> parameters, HashMap<String, String> files) {
        new ForumPostTask(isEdit, parameters, files).executeLong();
    }

    public void startForumStickTask(CfPost post, boolean stick) {
        new ForumStickTask(post, stick).executeLong();
    }
    
    public void startForumStickTaskV4(CfPost post) {
        new ForumStickTaskV4(post).executeLong();
    }

    public void startForumRecommendTask(CfPost post, boolean recommend) {
        new ForumRecommendTask(post, recommend).executeLong();
    }

    public void startForumLikeTask(CfRecord record) {
        new ForumLikeTask(record).executeLong();
    }

    public void startForumHateTask(CfRecord record) {
        new ForumHateTask(record).executeLong();
    }

    public void startForumCommentTask(CfRecord record, String content, ArrayList<UserInfo> refUserInfos) {
        if (!TextUtils.isEmpty(content)) {
            new ForumCommentTask(record, content, refUserInfos).executeLong();
        }
    }

    public void startForumDeleteTask(CfRecord record) {
        new ForumDeleteTask(record).executeLong();
    }
    
    public void startForumDeleteTaskV4(CfRecord record) {
        new ForumDeleteTaskV4(record).executeLong();
    }

    public void startChannelPostReplyTask(CfReply parentReply, CfReply targetReply, String content, ArrayList<UserInfo> refUserInfos){
        new ChannelPostReplyTask(parentReply, targetReply, content, refUserInfos).executeLong();
    }
    
    public void startForumPostCollectTask(CfPost post) {
        new PostCollectTask(post).executeLong();
    }

    private void notifyPreExecute(MsTaskType type) {
        for (MsTaskListener listener : mListeners) {
            listener.onPreExecute(type);
        }
    }

    private void notifyPostExecute(MsTaskType type, MsResponse response) {
        for (MsTaskListener listener : mListeners) {
            listener.onPostExecute(type, response);
        }
    }

    private class ForumPostTask extends MsMhpTask {

        public ForumPostTask(boolean isEdit,
                HashMap<String, String> parameters, HashMap<String, String> files) {
            super(mContext, isEdit ? MsTaskType.FORUM_EDIT_POST
                    : MsTaskType.FORUM_PUBLISH_POST, parameters, files);
        }

        @Override
        protected void onPreExecute() {
            notifyPreExecute(getType());
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                response.value = CfPost.fromJson(response.getJsonObject());
            }
            notifyPostExecute(getType(), response);

        }
    }

    private class ForumRecommendTask extends MsTask {
        private CfPost mPost;
        private boolean mRecommend;

        public ForumRecommendTask(CfPost post, boolean recommend) {
            super(mContext, MsTaskType.FORUM_RECOMMEND_POST);
            mPost = post;
            mRecommend = recommend;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("thread_id=").append(mPost.postId)
                    .toString();
        }

        @Override
        protected void onPreExecute() {
            notifyPreExecute(getType());
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                response.value = mPost;
                mPost.setRecommend(mRecommend);
            } else {
                response.showFailInfo(getRefContext(), R.string.cf_recommend_thread_failed);
            }
            notifyPostExecute(getType(), response);
        }
    }

    private class ForumStickTask extends MsTask {
        private CfPost mPost;
        private boolean mStick;

        public ForumStickTask(CfPost post, boolean stick) {
            super(mContext, MsTaskType.FORUM_STICK_POST);
            mPost = post;
            mStick = stick;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("thread_id=").append(mPost.postId)
                    .append("&level=").append(mStick ? 1 : 0)
                    .toString();
        }

        @Override
        protected void onPreExecute() {
            notifyPreExecute(getType());
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                response.value = mPost;
                mPost.stickLvl = mStick ? 1 : 0;
            } else {
                response.showFailInfo(getRefContext(), R.string.cf_stick_thread_failed);
            }
            notifyPostExecute(getType(), response);
        }
    }
    
    private class ForumStickTaskV4 extends MsTask {
        private CfPost mPost;

        public ForumStickTaskV4(CfPost post) {
            super(mContext, MsTaskType.FORUM_STICK_POST_V4);
            mPost = post;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("thread_id=").append(mPost.postId)
                    .toString();
        }

        @Override
        protected void onPreExecute() {
            notifyPreExecute(getType());
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                response.value = mPost;
                mPost.stickLvl = 1;
            } else {
                response.showFailInfo(getRefContext(), R.string.cf_stick_thread_failed);
            }
            notifyPostExecute(getType(), response);
        }
    }

    private class ForumLikeTask extends MsTask {
        private CfRecord mRecord;

        public ForumLikeTask(CfRecord record) {
            super(mContext, record instanceof CfPost
                    ? MsTaskType.FORUM_LIKE_POST : MsTaskType.FORUM_LIKE_REPLY);
            mRecord = record;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder(mRecord.getIdName())
                    .append("=").append(mRecord.getId())
                    .toString();
        }

        @Override
        protected void onPreExecute() {
            mRecord.liking = true;
            notifyPreExecute(getType());
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mRecord.liking = false;
            if (response.isSuccessful()) {
                mRecord.myUp = response.getJsonObject().optBoolean("my_like");
                mRecord.upCount = response.getJsonObject().optInt("up_count");
                response.value = mRecord;
            }
            notifyPostExecute(getType(), response);
        }
    }

    private class ForumCommentTask extends MsTask {
        private CfRecord mRecord;
        private String mContent;
        private ArrayList<UserInfo> mRefFriends;

        public ForumCommentTask(CfRecord record, String content, ArrayList<UserInfo> refUserInfos) {
            super(mContext, record instanceof CfPost
                    ? MsTaskType.FORUM_COMMENT_POST : MsTaskType.FORUM_COMMENT_REPLY);
            mRecord = record;
            mContent = content;
            this.mRefFriends = refUserInfos;
        }

        @Override
        protected String buildParams() {
            StringBuilder sb = new StringBuilder("content=").append(Utils.urlEncode(mContent))
                    .append("&").append(mRecord.getIdName())
                    .append("=").append(mRecord.getId());
            if (getType() == MsTaskType.FORUM_COMMENT_REPLY) {
                sb.append("&thread_id=").append(mRecord.postId);
            }
            if (mRefFriends != null && mRefFriends.size() > 0) {
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < mRefFriends.size(); i++) {
                    json.append(i == 0 ? "{\"uid\":\"" : ",{\"uid\":\"")
                    .append(mRefFriends.get(i).userId).append("\",\"nick\":\"")
                    .append(mRefFriends.get(i).getDisplayName(mContext)).append("\"}");
                }
                json.append("]");
                sb.append("&at_users=").append(Utils.urlEncode(json.toString()));
            }
            return sb.toString();
        }

        @Override
        protected void onPreExecute() {
            mRecord.replying = true;
            notifyPreExecute(getType());
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mRecord.replying = false;
            if (response.isSuccessful()) {
                mRecord.replyCount++;
                response.value = mRecord;
            } else {
                response.showFailInfo(getRefContext(), R.string.cf_reply_failed);
            }
            notifyPostExecute(getType(), response);
        }
    }

    private class ChannelPostReplyTask extends MsTask {
        private CfReply parentReply,targetReply;
        private String mContent;
        private ArrayList<UserInfo> mRefFriends;

        public ChannelPostReplyTask(CfReply parentReply,CfReply targetReply, String content, ArrayList<UserInfo> refUserInfos) {
            super(mContext, MsTaskType.FORUM_COMMENT_REPLY);
            this.parentReply = parentReply;
            this.targetReply = targetReply;
            this.mContent = content;
            this.mRefFriends = refUserInfos;
        }

        @Override
        protected String buildParams() {
            StringBuilder sb = new StringBuilder("content=").append(Utils.urlEncode(mContent))
                    .append("&").append(parentReply.getIdName())
                    .append("=").append(parentReply.getId());
            if (getType() == MsTaskType.FORUM_COMMENT_REPLY) {
                sb.append("&thread_id=").append(parentReply.postId);
                sb.append("&reply_id=").append(targetReply.replyId);
            }
            if (mRefFriends != null && mRefFriends.size() > 0) {
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < mRefFriends.size(); i++) {
                    json.append(i == 0 ? "{\"uid\":\"" : ",{\"uid\":\"")
                    .append(mRefFriends.get(i).userId).append("\",\"nick\":\"")
                    .append(mRefFriends.get(i).nickname).append("\"}");
                }
                json.append("]");
                sb.append("&at_users=").append(Utils.urlEncode(json.toString()));
            }
            return sb.toString();
        }

        @Override
        protected void onPreExecute() {
            if (parentReply != null) {
                parentReply.replying = true;
            }
            notifyPreExecute(getType());
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (parentReply != null) {
                parentReply.replying = false;
            }
            if (response.isSuccessful()) {
                parentReply.targetPost.replyCount++;
                response.value = parentReply;
            } else {
                response.showFailInfo(getRefContext(), R.string.cf_reply_failed);
            }
            notifyPostExecute(getType(), response);
        }
    }

    private class ForumDeleteTask extends MsTask {
        private CfRecord mRecord;

        public ForumDeleteTask(CfRecord record) {
            super(mContext, record instanceof CfPost
                    ? MsTaskType.FORUM_DELETE_POST : MsTaskType.FORUM_DELETE_REPLY);
            mRecord = record;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder(mRecord.getIdName())
                    .append("=").append(mRecord.getId())
                    .toString();
        }

        @Override
        protected void onPreExecute() {
            notifyPreExecute(getType());
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                response.value = mRecord;
                mRecord.replyCount--;
                if (getType() == MsTaskType.FORUM_DELETE_REPLY) {
                    CfReply reply = ((CfReply) mRecord);
                    if (reply.targetPost != null) {
                        reply.targetPost.replyCount--;
                    }
                }
                response.showInfo(getRefContext(), R.string.cf_delete_success);
            } else {
                response.showFailInfo(getRefContext(), R.string.cf_delete_failed);
            }
            notifyPostExecute(getType(), response);
        }
    }
    
    private class ForumDeleteTaskV4 extends MsTask {
        private CfRecord mRecord;

        public ForumDeleteTaskV4(CfRecord record) {
            super(mContext, MsTaskType.FORUM_DELETE_POST_V4);
            mRecord = record;
        }
        
        @Override
        protected String buildParams() {
            return new StringBuilder(mRecord.getIdName())
            .append("=").append(mRecord.getId())
            .toString();
        }
        
        @Override
        protected void onPreExecute() {
            notifyPreExecute(getType());
        }
        
        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                response.value = mRecord;
                mRecord.replyCount--;
                if (getType() == MsTaskType.FORUM_DELETE_REPLY ||
                        (getType() == MsTaskType.FORUM_DELETE_POST_V4 && mRecord instanceof  CfReply)) {
                    CfReply reply = ((CfReply) mRecord);
                    if (reply.targetPost != null) {
                        reply.targetPost.replyCount--;
                    }
                }
                response.showInfo(getRefContext(), R.string.cf_delete_success);
            } else {
                response.showFailInfo(getRefContext(), R.string.cf_delete_failed);
            }
            notifyPostExecute(getType(), response);
        }
    }


    private class ForumHateTask extends MsTask{
        private CfRecord mRecord;

        public ForumHateTask(CfRecord record) {
            super(mContext, record instanceof CfPost
                    ? MsTaskType.FORUM_HATE_POST : MsTaskType.FORUM_HATE_REPLY);
            mRecord = record;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder(mRecord.getIdName())
                    .append("=").append(mRecord.getId())
                    .toString();
        }

        @Override
        protected void onPreExecute() {
            mRecord.replying = true;
            notifyPreExecute(getType());
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mRecord.hating = false;
            if (response.isSuccessful()) {
                response.value = mRecord;
            }
            notifyPostExecute(getType(), response);
        }
    }

	private class PostNoteTask extends MsMhpTask {

		public PostNoteTask(boolean isEdit, HashMap<String, String> parameters,
				HashMap<String, String> files) {
			super(mContext, isEdit ? MsTaskType.FORUM_EDIT_NOTE : MsTaskType.FORUM_NOTE_POST, parameters, files);
		}

		@Override
		protected void onPreExecute() {
			notifyPreExecute(getType());
		}
		
		@Override
		protected void onPostExecute(MsResponse response) {
			if (response.isSuccessful()) {
				NoteInfo note = NoteInfo.fromJson(response.getJsonObject());
			}
			notifyPostExecute(getType(), response);
		}
    }
	
	private class PostCollectTask extends MsTask {

	    private CfPost mCfPost;
	    
        public PostCollectTask(CfPost post) {
            super(mContext, MsTaskType.FORUM_COLLECT_POST);
            mCfPost = post;
        }
        
        @Override
        protected String buildParams() {
            return new StringBuilder("thread_id=").append(mCfPost.postId).toString();
        }
	    
        @Override
        protected void onPreExecute() {
            notifyPreExecute(getType());
        }
        
        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                mCfPost.collected = response.getJsonObject().optBoolean("collect");
                response.value = mCfPost;
                if (mCfPost.collected) {
                    Utils.toast(mContext.getString(R.string.rc_collect_success));
                } else {
                    Utils.toast(mContext.getString(R.string.rc_collection_cancel));
                }
            }
            notifyPostExecute(getType(), response);
        }
	}
	
	public void startPostNoteTask(boolean isEdit,
            HashMap<String, String> parameters, HashMap<String, String> files) {
        new PostNoteTask(isEdit , parameters, files).executeLong();
    }
}

