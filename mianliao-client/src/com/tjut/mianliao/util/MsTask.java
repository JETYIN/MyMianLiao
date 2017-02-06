package com.tjut.mianliao.util;

import android.content.Context;

import com.tjut.mianliao.util.MsTaskListener.MsTaskType;

public class MsTask extends AdvAsyncTask<Void, Void, MsResponse> {

    private Context mContext;
    private MsTaskType mType;
    private MsRequest mRequest;
    private MsTaskListener mListener;

    public MsTask() {
    }

    public MsTask(Context context, MsTaskType type) {
        mContext = context;
        mType = type;
        switch (type) {
            case FORUM_PUBLISH_POST:
                mRequest = MsRequest.CF_POST;
                break;
            case FORUM_EDIT_POST:
                mRequest = MsRequest.CF_EDIT_THREAD;
                break;
            case FORUM_STICK_POST:
                mRequest = MsRequest.CF_STICK_THREAD;
                break;
            case FORUM_STICK_POST_V4:
                mRequest = MsRequest.CF_STICK_THREAD_V4;
                break;
            case FORUM_RECOMMEND_POST:
                mRequest = MsRequest.CF_SUGGEST_THREAD;
                break;
            case FORUM_LIKE_POST:
            case FORUM_LIKE_REPLY:
                mRequest = MsRequest.CF_THUMB_LIKE;
                break;
            case FORUM_COMMENT_POST:
            case FORUM_COMMENT_REPLY:
                mRequest = MsRequest.CF_REPLY;
                break;
            case FORUM_DELETE_POST:
            case FORUM_DELETE_REPLY:
                mRequest = MsRequest.CF_DELETE;
                break;
            case FORUM_DELETE_POST_V4:
                mRequest = MsRequest.CF_DELETE_V4;
                break;
            case FORUM_HATE_POST:
            case FORUM_HATE_REPLY:
                mRequest = MsRequest.CF_THUMB_HATE;
                break;
            case FORUM_NOTE_POST:
                mRequest = MsRequest.NOTE_POST;
                break;
            case FORUM_EDIT_NOTE:
                mRequest = MsRequest.EDIT_NOTE_POST;
                break;
            case FORUM_DELETE_NOTE:
                mRequest = mRequest.DELETE_NOTE_POST;
                break;
            case FORUM_COLLECT_POST:
                mRequest = MsRequest.CF_COLLECT_THREAD;
                break;
            default:
                break;
        }
    }

    public MsTask(Context context, MsRequest request) {
        mContext = context;
        mRequest = request;
    }

    /**
     * Do not use getContext because it might have conflicts in some cases.
     */
    public Context getRefContext() {
        return mContext;
    }

    public MsTaskType getType() {
        return mType;
    }

    public MsRequest getRequest() {
        return mRequest;
    }

    public MsTaskListener getListener() {
        return mListener;
    }

    public MsTask setTaskListener(MsTaskListener listener) {
        mListener = listener;
        return this;
    }

    protected String buildParams() {
        return "version=" + Utils.getPackageInfo(mContext).versionName;
    }

    @Override
    protected void onPreExecute() {
        if (mListener != null) {
            mListener.onPreExecute(mType);
        }
    }

    @Override
    protected MsResponse doInBackground(Void... params) {
        /**将MsResponse的code以及response实例化**/
        return HttpUtil.msRequest(mContext, mRequest, buildParams());
    }

    @Override
    protected void onPostExecute(MsResponse response) {
        if (mListener != null) {
            mListener.onPostExecute(mType, response);
        }
    }
}
