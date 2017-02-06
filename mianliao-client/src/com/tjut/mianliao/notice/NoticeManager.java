package com.tjut.mianliao.notice;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.BaseTask.TaskExecutionListener;
import com.tjut.mianliao.BaseTask.TaskType;
import com.tjut.mianliao.RedDot;
import com.tjut.mianliao.RedDot.RedDotType;
import com.tjut.mianliao.contact.ContactUpdateCenter;
import com.tjut.mianliao.contact.ContactUpdateCenter.ContactObserver;
import com.tjut.mianliao.contact.ContactUpdateCenter.UpdateType;
import com.tjut.mianliao.contact.SubscriptionHelper;
import com.tjut.mianliao.data.notice.NoticeSummary;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.MsTaskListener;

public class NoticeManager implements MsTaskListener, ContactObserver {

    private static WeakReference<NoticeManager> sInstanceRef;

    private Context mContext;
    private List<TaskExecutionListener> mListeners;
    private SparseArray<NoticeSummary> mSummaries;
    private boolean mTouchFlag;

    public static synchronized NoticeManager getInstance(Context context) {
        if (sInstanceRef != null && sInstanceRef.get() != null) {
            return sInstanceRef.get();
        }
        NoticeManager instance = new NoticeManager(context);
        sInstanceRef = new WeakReference<NoticeManager>(instance);
        return instance;
    }

    private NoticeManager(Context context) {
        ContactUpdateCenter.registerObserver(this);
        mContext = context.getApplicationContext();
        mListeners = new ArrayList<TaskExecutionListener>();
        mSummaries = new SparseArray<NoticeSummary>();
        initSummaries();
    }

    @Override
    public void onPreExecute(MsTaskType type) {
        for (TaskExecutionListener listener : mListeners) {
            listener.onPreExecute(TaskType.NOTICE_TOUCH);
        }
    }

    @Override
    public void onPostExecute(MsTaskType type, MsResponse response) {
        for (TaskExecutionListener listener : mListeners) {
            listener.onPostExecute(TaskType.NOTICE_TOUCH, response);
        }
    }

    @Override
    public void onContactsUpdated(UpdateType type, Object data) {
        SubscriptionHelper subHelper = SubscriptionHelper.getInstance(mContext);
        updateCount(NoticeSummary.SUBZONE_NF, subHelper.getCount());
    }

    public void startNoticeTouchTask() {
        new TouchTask().setTaskListener(this).executeLong();
    }

    public void registerTaskListener(TaskExecutionListener listener) {
        if (listener != null && !mListeners.contains(listener)) {
            mListeners.add(0, listener);
        }
    }

    public void unregisterTaskListener(TaskExecutionListener listener) {
        if (listener != null) {
            mListeners.remove(listener);
        }
    }

    public NoticeSummary getSummary(int subzone) {
        return mSummaries.get(subzone);
    }

    public List<NoticeSummary> getSummaries() {
        List<NoticeSummary> summaries = new ArrayList<NoticeSummary>();
        int size = mSummaries.size();
        for (int i = 0; i < size; i++) {
            summaries.add(mSummaries.valueAt(i));
        }
        return summaries;
    }

    public void clearTouchFlag() {
        mTouchFlag = false;
    }

    public void clear() {
        ContactUpdateCenter.removeObserver(this);
        mListeners.clear();
        mSummaries.clear();
        sInstanceRef.clear();
    }

    public boolean hasNewNotice() {
        if (mTouchFlag) {
            return true;
        }

        int size = mSummaries.size();
        for (int i = 0; i < size; i++) {
            if (mSummaries.valueAt(i).count > 0) {
                return true;
            }
        }

        return false;
    }

    public void updateCount(JSONArray array) {
        for (int i = 0; i < array.length(); i++) {
            JSONObject json = array.optJSONObject(i);
            if (json != null) {
                int subzone = json.optInt(NoticeSummary.SUBZONE);
                int count = json.optInt(NoticeSummary.COUNT);
                updateCount(subzone, count);
            }
        }
    }

    public void setNoticeViewed(Activity activity, int count) {
        activity.setResult(BaseActivity.RESULT_VIEWED,
                new Intent().putExtra(NoticeSummary.COUNT, count));
    }

    public void onNoticeViewed(NoticeSummary summary, Intent data) {
        int count = data.getIntExtra(NoticeSummary.COUNT, 0);
        updateCount(summary.subzone, count);
    }

    private void initSummaries() {}

    private void updateCount(int subzone, int count) {
        NoticeSummary summary = mSummaries.get(subzone);
        if (summary != null) {
            summary.count = count;
        }
        updateRedDot();
    }

    private void updateRedDot() {
        RedDot.getInstance().update(RedDotType.EXPLORE, hasNewNotice() ? 1 : 0);
    }

    private class TouchTask extends MsTask {

        public TouchTask() {
            super(mContext, MsRequest.NOTICE_TOUCH);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (MsResponse.isSuccessful(response)) {
                mTouchFlag = Boolean.valueOf(response.response);
                updateRedDot();
            }
            super.onPostExecute(response);
        }
    }
}
