package com.tjut.mianliao.forum.nova;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.content.Context;

import com.tjut.mianliao.forum.CfReply;

public class ReplyViewManager {

    private static WeakReference<ReplyViewManager> sInstanceRef;

    public ArrayList<ReplyViewListener> mListeners;

    public static synchronized ReplyViewManager getInstance(Context context) {
        if (sInstanceRef != null && sInstanceRef.get() != null) {
            return sInstanceRef.get();
        }
        ReplyViewManager instance = new ReplyViewManager(context);
        sInstanceRef = new WeakReference<ReplyViewManager>(instance);
        return instance;
    }

    private ReplyViewManager(Context context) {
        mListeners = new ArrayList<>();
    }

    public void registerReplyViewListener(ReplyViewListener listener) {
        if (listener != null && !mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void unregisterReplyViewListener(ReplyViewListener listener) {
        mListeners.remove(listener);
    }

    public ArrayList<ReplyViewListener> getListeners() {
        return mListeners;
    }

    public interface ReplyViewListener {

        void onClickReplyView(CfReply parentReply, CfReply reply);
    }
}
