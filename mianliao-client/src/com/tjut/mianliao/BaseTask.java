package com.tjut.mianliao;

import android.content.Context;

import com.tjut.mianliao.util.AdvAsyncTask;
import com.tjut.mianliao.util.HttpUtil;
import com.tjut.mianliao.util.MsResponse;

public abstract class BaseTask extends AdvAsyncTask<Void, Void, MsResponse> {

    protected static final String PARAM_OFFSET = "offset=";
    protected static final String PARAM_LIMIT  = "limit=";

    protected Context mContext;
    protected int mType;
    protected int mOffset;
    protected int mLimit;
    protected TaskExecutionListener mListener;

    protected abstract boolean isGet();
    protected abstract String getApi();
    protected abstract String getAction();
    protected abstract String buildParams();
    protected abstract Object getResponseValue(MsResponse mr) throws Exception;

    public BaseTask(Context context, int type) {
        mContext = context;
        mType = type;
    }

    public BaseTask setOffset(int offset) {
        mOffset = offset;
        return this;
    }

    public BaseTask setLimit(int limit) {
        mLimit = limit;
        return this;
    }

    public BaseTask setListener(TaskExecutionListener listener) {
        mListener = listener;
        return this;
    }

    @Override
    protected void onPreExecute() {
        if (mListener != null) {
            mListener.onPreExecute(mType);
        }
    }

    @Override
    protected MsResponse doInBackground(Void... params) {
        String api = getApi();
        String action = getAction();
        MsResponse mr = isGet() ? HttpUtil.msGet(mContext, api, action, buildParams())
                : HttpUtil.msPost(mContext, api, action, buildParams());
        if (!isCancelled()) {
            try {
                mr.value = getResponseValue(mr);
            } catch (Exception e) {
                mr.code = MsResponse.MS_PARSE_FAILED;
            }
        }
        return mr;
    }

    @Override
    protected void onPostExecute(MsResponse mr) {
        if (mListener != null) {
            mListener.onPostExecute(mType, mr);
        }
    }

    public static class TaskType {
        public static final int NEWS_FETCH_LATEST = 0x01;
        public static final int NEWS_FETCH_HOT    = 0x02;
        public static final int NEWS_FETCH_FAV    = 0x03;
        public static final int NEWS_FETCH_COUNT  = 0x04;
        public static final int NEWS_FETCH_CMT    = 0x05;
        public static final int NEWS_FAVORITE     = 0x06;
        public static final int NEWS_LIKE         = 0x07;
        public static final int NEWS_COMMENT      = 0x08;
        public static final int NEWS_TICKET       = 0x09;
        public static final int NEWS_QR_CODE      = 0x0A;
        public static final int NEWS_DELETE_CMT   = 0x0B;
        public static final int NEWS_FETCH_SUGGESTED = 0x0C;
        public static final int NEWS_FETCH_SUGGESTED_COUNT  = 0x0D;
        public static final int NEWS_FETCH_MY_FAV  = 0x0E;

        public static final int NEWS_SOURCE_PROFILE   = 0x10;
        public static final int NEWS_SOURCE_FOLLOW    = 0x20;
        public static final int NEWS_SOURCE_NEWS      = 0x30;
        public static final int NEWS_SOURCES_FOLLOWED = 0x40;
        public static final int NEWS_SOURCES_SEARCH   = 0x50;

        public static final int NOTICE_TOUCH          = 0x100;

        public static final int FORUM_LIST_REPLY      = 0x1000;
        public static final int FORUM_LIKE            = 0x2000;
        public static final int FORUM_REPLY           = 0x3000;
        public static final int FORUM_DELETE          = 0x4000;
    }

    public interface TaskExecutionListener {
        void onPreExecute(int type);
        void onPostExecute(int type, MsResponse mr);
    }
}
