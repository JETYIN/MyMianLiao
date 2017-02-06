package com.tjut.mianliao.forum.nova;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.SparseArray;

import com.tjut.mianliao.data.ChannelInfo;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

public class ChannelInfoManager {
    

    private static WeakReference<ChannelInfoManager> sInstanceRef;
    private Context mContext;
    private ArrayList<ChannelInfoListener> mListeners;
    private SparseArray<ChannelInfo> mChannelInfos;
    private boolean mIsNightMode;

    public static synchronized ChannelInfoManager getInstance(Context context) {
        if (sInstanceRef != null && sInstanceRef.get() != null) {
            return sInstanceRef.get();
        }
        ChannelInfoManager instance = new ChannelInfoManager(context);
        sInstanceRef = new WeakReference<ChannelInfoManager>(instance);
        return instance;
    }

    private ChannelInfoManager(Context context) {
        mContext = context.getApplicationContext();
        mListeners = new ArrayList<ChannelInfoListener>();
        mChannelInfos = new SparseArray<>();
        mIsNightMode = Settings.getInstance(context).isNightMode();
    }

    public void clear() {
        mListeners.clear();
        mChannelInfos.clear();
        sInstanceRef.clear();
    }
    
    public void registerChannelInfoListener(ChannelInfoListener listener) {
        if (listener != null && !mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void unregisterChannelInfoLister() {
        mListeners.clear();
    }

    public ChannelInfo getChannelInfoById(int id) {
        ChannelInfo channelInfo = mChannelInfos.get(id);
        if (channelInfo != null) {
            return channelInfo;
        } else {
            new GetChannelInfoById(id).executeLong();
        }
        return channelInfo;
    }
    
    public void getChannelList() {
        new GetChannelListTask().executeLong();
    }

    public void getNewChannelLists() {
        new GetNewChannelListTask().executeLong();
    }

    private class GetChannelListTask extends MsTask {

        public GetChannelListTask() {
            super(mContext, MsRequest.CHANNEL_LIST);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                JSONObject json = response.getJsonObject();
                try {
                    ArrayList<ChannelInfo> offcialChannels = JsonUtil.getArray(
                            json.getJSONArray("official"), ChannelInfo.TRANSFORMER);
                    ArrayList<ChannelInfo> userChannels = JsonUtil.getArray(
                            json.getJSONArray("user"), ChannelInfo.TRANSFORMER);
                    for (ChannelInfoListener listener : mListeners) {
                        listener.onGetChannelInfoSuccess(offcialChannels, userChannels);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private class GetNewChannelListTask extends MsTask {

        public GetNewChannelListTask() {
            super(mContext, MsRequest.NEW_LIST);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                ArrayList<ChannelInfo> channels = JsonUtil.getArray(
                        response.getJsonArray(), ChannelInfo.TRANSFORMER);
                for (ChannelInfoListener listener : mListeners) {
                    listener.onGetNewChannelListsSuccess(channels);
                }
            }
        }

    }
    
    private class GetChannelInfoById extends MsTask{

        private int mChannelId;
        
        public GetChannelInfoById(int id) {
            super(mContext, MsRequest.FIND_CHANNEL_BY_ID);
            mChannelId = id;
        }
        
        @Override
        protected String buildParams() {
            return "id=" + mChannelId;
        }
        
        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                ChannelInfo channel = ChannelInfo.fromJson(response.getJsonObject());
                ChannelInfo channelInfo = mChannelInfos.get(mChannelId);
                if (channelInfo == null) {
                    mChannelInfos.remove(mChannelId);
                    mChannelInfos.put(mChannelId, channel);
                }
            }
        }
        
    }

    public interface ChannelInfoListener {
        public void onGetChannelInfoSuccess(ArrayList<ChannelInfo> offcialChannelInfos,
                ArrayList<ChannelInfo> userChannelInfos);
        void onGetNewChannelListsSuccess(ArrayList<ChannelInfo> channels);
    }
}
